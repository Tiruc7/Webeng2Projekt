<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const emit = defineEmits(['search'])

const keyword = ref('')
const city = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const suggestions = ref([])
const showSuggestions = ref(false)
const isLoadingSuggestions = ref(false)
const suggestionError = ref('')

const canSearch = computed(() => keyword.value.trim().length >= 3)

const suggestionPreviewUrl = computed(() => {
  const params = new URLSearchParams()

  if (keyword.value.trim()) {
    params.set('keyword', keyword.value.trim())
  }

  if (city.value.trim()) {
    params.set('city', city.value.trim())
  }

  return `/api/search/suggestions?${params.toString()}`
})

const eventsPreviewUrl = computed(() => {
  const params = new URLSearchParams()

  if (keyword.value.trim()) {
    params.set('keyword', keyword.value.trim())
  }

  if (city.value.trim()) {
    params.set('city', city.value.trim())
  }

  if (dateFrom.value) {
    params.set('dateFrom', dateFrom.value)
  }

  if (dateTo.value) {
    params.set('dateTo', dateTo.value)
  }

  params.set('size', '10')

  return `/api/search/events?${params.toString()}`
})

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

  if (requestKey === lastRequestKey) {
    return
  }

  lastRequestKey = requestKey

  if (activeController) {
    activeController.abort()
  }

  activeController = new AbortController()
  isLoadingSuggestions.value = true
  suggestionError.value = ''

  try {
    const response = await fetch(
      `/api/search/suggestions?keyword=${encodeURIComponent(trimmedKeyword)}&city=${encodeURIComponent(trimmedCity)}`,
      {
        signal: activeController.signal,
      }
    )

    if (!response.ok) {
      throw new Error(`Suggestion request failed (${response.status})`)
    }

    const data = await response.json()
    suggestions.value = Array.isArray(data) ? data.slice(0, 3) : []
    showSuggestions.value = suggestions.value.length > 0
  } catch (error) {
    if (error.name === 'AbortError') {
      return
    }

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

    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }

    if (activeController) {
      activeController.abort()
    }

    resetSuggestions()
    return
  }

  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  debounceTimer = setTimeout(() => {
    fetchSuggestions()
  }, 500)
})

function applySuggestion(item) {
  keyword.value = item.title ?? ''

  if (!city.value && item.city) {
    city.value = item.city
  }

  showSuggestions.value = false
}

function submitSearch() {
  if (!canSearch.value) return

  emit('search', {
    keyword: keyword.value.trim(),
    city: city.value.trim(),
    dateFrom: dateFrom.value,
    dateTo: dateTo.value,
    size: 10,
  })

  showSuggestions.value = false
}

function clearFilters() {
  keyword.value = ''
  city.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  lastRequestKey = ''

  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  if (activeController) {
    activeController.abort()
  }

  resetSuggestions()
}

onBeforeUnmount(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  if (activeController) {
    activeController.abort()
  }
})
</script>

<template>
  <section class="search-panel">
    <div class="search-panel__header">
      <h2>Find concerts</h2>
      <p>Phase 2: live suggestions via backend, no live event result list yet.</p>
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

        <p
          v-else-if="isLoadingSuggestions"
          class="search-panel__empty"
        >
          Loading suggestions...
        </p>

        <p
          v-else-if="suggestionError"
          class="search-panel__empty"
        >
          {{ suggestionError }}
        </p>

        <p
          v-else-if="keyword.trim().length >= 3"
          class="search-panel__empty"
        >
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
        :disabled="!canSearch"
        @click="submitSearch"
      >
        Search
      </button>

      <button
        type="button"
        class="search-panel__button"
        @click="clearFilters"
      >
        Clear
      </button>
    </div>

    <div class="search-panel__preview">
      <h3>Request preview</h3>
      <p><strong>Suggestions:</strong> {{ suggestionPreviewUrl }}</p>
      <p><strong>Events:</strong> {{ eventsPreviewUrl }}</p>
      <p class="search-panel__hint">
        Suggestions start after 3+ characters, use 500 ms debounce, and show max. 3 entries.
      </p>
    </div>
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
  margin: 0 0 0.35rem 0;
}

.search-panel__header p {
  margin: 0;
  color: var(--text-3);
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

.search-panel__empty {
  margin-top: 0.55rem;
  color: var(--text-3);
  font-size: 0.9rem;
}

.search-panel__preview {
  margin-top: 1.25rem;
  padding: 1rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
}

.search-panel__preview h3 {
  margin-top: 0;
  margin-bottom: 0.65rem;
}

.search-panel__preview p {
  margin: 0.35rem 0;
  word-break: break-word;
}

.search-panel__hint {
  color: var(--text-3);
  font-size: 0.9rem;
}

@media (max-width: 980px) {
  .search-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>