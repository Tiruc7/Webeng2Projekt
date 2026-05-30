<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { authState } from '../auth/authState.js'
import { secureFetch } from '../api/api.js'

const props = defineProps({
  savedEventIds: {
    type: Set,
    default: () => new Set()
  }
})

const emit = defineEmits(['concert-saved'])

const keyword = ref('')
const city = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const suggestions = ref([])
const showSuggestions = ref(false)
const isLoadingSuggestions = ref(false)
const suggestionError = ref('')

const searchResults = ref([])
const hasSearched = ref(false)
const displayedCount = ref(20)
const displayedResults = computed(() => searchResults.value.slice(0, displayedCount.value))
const canLoadMore = computed(() => displayedCount.value < searchResults.value.length)
const isSearching = ref(false)
const searchError = ref('')
// Tracks which event IDs are currently being saved (for loading state per button)
const savingIds = reactive({})
const saveErrors = reactive({})

const canSearch = computed(() => keyword.value.trim().length >= 3)

let debounceTimer = null
let activeController = null
let lastRequestKey = ''

function resetSuggestions() {
  suggestions.value = []
  showSuggestions.value = false
  isLoadingSuggestions.value = false
  suggestionError.value = ''
}

async function fetchSuggestions() {
  const trimmedKeyword = keyword.value.trim()
  const trimmedCity = city.value.trim()

  if (trimmedKeyword.length < 3) {
    resetSuggestions()
    return
  }

  const requestKey = `${trimmedKeyword.toLowerCase()}|${trimmedCity.toLowerCase()}`
  if (requestKey === lastRequestKey) return
  lastRequestKey = requestKey

  if (activeController) activeController.abort()
  activeController = new AbortController()
  isLoadingSuggestions.value = true
  suggestionError.value = ''

  const suggestionParams = new URLSearchParams({ keyword: trimmedKeyword })
  if (trimmedCity) suggestionParams.set('city', trimmedCity)

  try {
    const response = await fetch(
      `/api/search/suggestions?${suggestionParams}`,
      { signal: activeController.signal }
    )
    if (!response.ok) throw new Error(`Suggestion request failed (${response.status})`)
    const data = await response.json()
    suggestions.value = Array.isArray(data) ? data.slice(0, 3) : []
    showSuggestions.value = suggestions.value.length > 0
  } catch (error) {
    if (error.name === 'AbortError') return
    suggestions.value = []
    showSuggestions.value = false
    suggestionError.value = error.message || 'Suggestions could not be loaded.'
  } finally {
    isLoadingSuggestions.value = false
  }
}

watch([keyword, city], () => {
  const trimmedKeyword = keyword.value.trim()

  if (trimmedKeyword.length < 3) {
    lastRequestKey = ''
    if (debounceTimer) clearTimeout(debounceTimer)
    if (activeController) activeController.abort()
    resetSuggestions()
    return
  }

  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(fetchSuggestions, 700)
})

function applySuggestion(item) {
  keyword.value = item.title ?? ''
  if (!city.value && item.city) city.value = item.city
  showSuggestions.value = false
}

async function submitSearch() {
  if (!canSearch.value) return
  showSuggestions.value = false

  const params = new URLSearchParams()
  params.set('keyword', keyword.value.trim())
  if (city.value.trim()) params.set('city', city.value.trim())
  if (dateFrom.value) params.set('dateFrom', dateFrom.value)
  if (dateTo.value) params.set('dateTo', dateTo.value)
  params.set('size', '100')

  isSearching.value = true
  searchError.value = ''
  displayedCount.value = 20

  try {
    const response = await fetch(`/api/search/events?${params}`)
    if (!response.ok) throw new Error(`Search failed (${response.status})`)
    searchResults.value = await response.json()
  } catch (e) {
    searchError.value = e.message || 'Search failed.'
    searchResults.value = []
  } finally {
    isSearching.value = false
    hasSearched.value = true
  }
}

function loadMore() {
  displayedCount.value = Math.min(displayedCount.value + 20, searchResults.value.length)
}

async function saveEvent(event) {
  if (!authState.authenticated) return
  savingIds[event.id] = true
  delete saveErrors[event.id]

  try {
    const response = await secureFetch('/api/user/events', {
      method: 'POST',
      body: JSON.stringify(event)
    })
    if (!response.ok) {
      const text = await response.text().catch(() => '')
      throw new Error(`${response.status}${text ? ': ' + text : ''}`)
    }
    emit('concert-saved')
  } catch (e) {
    console.error('Failed to save event:', e)
    saveErrors[event.id] = e.message || 'Save failed'
  } finally {
    delete savingIds[event.id]
  }
}

function clearFilters() {
  keyword.value = ''
  city.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  searchResults.value = []
  hasSearched.value = false
  displayedCount.value = 20
  searchError.value = ''
  lastRequestKey = ''
  if (debounceTimer) clearTimeout(debounceTimer)
  if (activeController) activeController.abort()
  resetSuggestions()
}

onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (activeController) activeController.abort()
})
</script>

<template>
  <section class="search-panel">
    <div class="search-panel__header">
      <h2>Find concerts</h2>
    </div>

    <div class="search-panel__grid">
      <div class="search-panel__main-input">
        <label for="keyword">Keyword</label>
        <input
          id="keyword"
          v-model="keyword"
          type="text"
          placeholder="Search artist, event, or venue"
          @keydown.enter.prevent="submitSearch"
        />

        <div
          v-if="showSuggestions && suggestions.length > 0"
          class="search-panel__suggestions"
        >
          <button
            v-for="item in suggestions"
            :key="item.id || item.title + item.venue + item.city"
            type="button"
            class="search-panel__suggestion"
            @click="applySuggestion(item)"
          >
            <span class="search-panel__suggestion-title">{{ item.title }}</span>
            <span class="search-panel__suggestion-meta">
              {{ item.venue }} · {{ item.city }}
              <template v-if="item.date"> · {{ item.date }}</template>
            </span>
          </button>
        </div>

        <p v-else-if="isLoadingSuggestions" class="search-panel__hint">
          Loading suggestions...
        </p>
        <p v-else-if="suggestionError" class="search-panel__hint">
          {{ suggestionError }}
        </p>
        <p v-else-if="keyword.trim().length >= 3" class="search-panel__hint">
          No suggestions found.
        </p>
      </div>

      <div>
        <label for="city">City</label>
        <input
          id="city"
          v-model="city"
          type="text"
          placeholder="e.g. Berlin"
          @keydown.enter.prevent="submitSearch"
        />
      </div>

      <div>
        <label for="dateFrom">Date from</label>
        <input
          id="dateFrom"
          v-model="dateFrom"
          type="date"
          @keydown.enter.prevent="submitSearch"
        />
      </div>

      <div>
        <label for="dateTo">Date to</label>
        <input
          id="dateTo"
          v-model="dateTo"
          type="date"
          @keydown.enter.prevent="submitSearch"
        />
      </div>
    </div>

    <div class="search-panel__actions">
      <button
        type="button"
        class="search-panel__button search-panel__button--primary"
        :disabled="!canSearch || isSearching"
        @click="submitSearch"
      >
        {{ isSearching ? 'Searching...' : 'Search' }}
      </button>

      <button
        type="button"
        class="search-panel__button"
        @click="clearFilters"
      >
        Clear
      </button>
    </div>

    <p v-if="searchError" class="search-panel__error">{{ searchError }}</p>

    <div v-if="searchResults.length > 0" class="search-results">
      <h3 class="search-results__heading">
        Results
        <span class="search-results__count">{{ displayedResults.length }} / {{ searchResults.length }}</span>
      </h3>

      <div class="search-results__grid">
        <article
          v-for="event in displayedResults"
          :key="event.id"
          class="result-card"
        >
          <img
            v-if="event.imageUrl"
            :src="event.imageUrl"
            :alt="event.title"
            class="result-card__image"
          />
          <div v-else class="result-card__image result-card__image--placeholder" />

          <div class="result-card__body">
            <h4 class="result-card__title">{{ event.title }}</h4>
            <p class="result-card__meta">{{ event.venue }} · {{ event.city }}</p>
            <p class="result-card__date">{{ event.date }}<template v-if="event.time"> · {{ event.time }}</template></p>
          </div>

          <div class="result-card__footer">
            <button
              type="button"
              class="result-card__save"
              :class="{ 'result-card__save--saved': savedEventIds.has(event.id) }"
              :disabled="!authState.authenticated || savedEventIds.has(event.id) || savingIds[event.id]"
              @click="saveEvent(event)"
            >
              <template v-if="savingIds[event.id]">Saving...</template>
              <template v-else-if="savedEventIds.has(event.id)">Saved</template>
              <template v-else-if="!authState.authenticated">Log in to save</template>
              <template v-else>Save to profile</template>
            </button>
            <p v-if="saveErrors[event.id]" class="result-card__save-error">{{ saveErrors[event.id] }}</p>
          </div>
        </article>
      </div>

      <div v-if="canLoadMore" class="search-results__more">
        <button
          type="button"
          class="search-panel__button"
          @click="loadMore"
        >
          Load more
        </button>
      </div>
    </div>

    <p v-else-if="hasSearched && !isSearching && searchResults.length === 0 && searchError === ''" class="search-panel__hint">
      No results found.
    </p>
  </section>
</template>

<style scoped>
.search-panel {
  position: relative;
  margin-bottom: 2rem;
  padding: 1.25rem;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: rgba(15, 23, 42, 0.88);
  box-shadow: var(--shadow-soft);
}

.search-panel__header {
  margin-bottom: 1rem;
}

.search-panel__header h2 {
  margin: 0;
}

.search-panel__grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 1rem;
}

.search-panel__main-input {
  position: relative;
}

.search-panel label {
  display: block;
  margin-bottom: 0.45rem;
  font-size: 0.9rem;
  color: var(--text-2);
}

.search-panel input {
  width: 100%;
  padding: 0.85rem 0.95rem;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
}

.search-panel__actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
  justify-content: flex-end;
}

.search-panel__button {
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  padding: 0.8rem 1rem;
  border-radius: 14px;
  cursor: pointer;
}

.search-panel__button--primary {
  background: var(--accent-pink-soft);
  border-color: rgba(225, 29, 141, 0.5);
}

.search-panel__button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.search-panel__suggestions {
  position: absolute;
  top: calc(100% + 0.45rem);
  left: 0;
  right: 0;
  z-index: 20;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.98);
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.35);
}

.search-panel__suggestion {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  text-align: left;
  padding: 0.85rem 1rem;
  border: none;
  background: transparent;
  color: var(--text-1);
  cursor: pointer;
}

.search-panel__suggestion:hover {
  background: var(--accent-pink-soft);
}

.search-panel__suggestion-title {
  font-weight: 700;
}

.search-panel__suggestion-meta {
  font-size: 0.85rem;
  color: var(--text-3);
}

.search-panel__hint {
  margin-top: 0.55rem;
  color: var(--text-3);
  font-size: 0.9rem;
}

.search-panel__error {
  margin-top: 0.75rem;
  color: #f87171;
  font-size: 0.9rem;
}

/* Search results */

.search-results {
  margin-top: 1.5rem;
  border-top: 1px solid var(--border);
  padding-top: 1.25rem;
}

.search-results__heading {
  margin: 0 0 1rem;
  font-size: 1.1rem;
  color: var(--text-1);
}

.search-results__count {
  color: var(--text-3);
  font-weight: 400;
  font-size: 0.95rem;
}

.search-results__more {
  display: flex;
  justify-content: center;
  margin-top: 1.25rem;
}

.search-results__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.result-card {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border);
  border-radius: 18px;
  overflow: hidden;
  background: var(--bg-surface);
  transition: border-color 0.18s ease;
}

.result-card:hover {
  border-color: rgba(37, 99, 235, 0.45);
}

.result-card__image {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.result-card__image--placeholder {
  background: var(--bg-surface-2);
}

.result-card__body {
  padding: 0.85rem 1rem 0.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.result-card__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-1);
  line-height: 1.3;
}

.result-card__meta,
.result-card__date {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-3);
}

.result-card__footer {
  padding: 0.75rem 1rem;
}

.result-card__save {
  width: 100%;
  padding: 0.6rem 1rem;
  border-radius: 12px;
  border: 1px solid rgba(225, 29, 141, 0.5);
  background: var(--accent-pink-soft);
  color: var(--text-1);
  font-size: 0.875rem;
  cursor: pointer;
  transition: filter 0.15s ease;
}

.result-card__save:hover:not(:disabled) {
  filter: brightness(1.15);
}

.result-card__save--saved {
  background: transparent;
  border-color: var(--border);
  color: var(--text-3);
  cursor: default;
  filter: none;
}

.result-card__save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.result-card__save-error {
  margin: 0.4rem 0 0;
  font-size: 0.78rem;
  color: #f87171;
}

@media (max-width: 980px) {
  .search-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
