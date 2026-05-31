<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EventSearchPanel from '../components/EventSearchPanel.vue'
import ConcertCard from '../components/ConcertCard.vue'
import { authState } from '../auth/authState.js'
import { secureFetch } from '../api/api.js'
import ConcertCommentPopUp from '../components/ConcertCommentPopUp.vue'

const route = useRoute()
const router = useRouter()

// Active tab is encoded in the URL as ?tab=saved so it survives page refresh
const activeTab = computed(() => route.query.tab === 'saved' ? 'saved' : 'search')

function setTab(tab) {
  router.replace({ query: { tab } })
}

const concerts = ref([])

const savedEventIds = computed(() => new Set(concerts.value.map(c => c.id)))

async function loadSavedConcerts() {
  if (!authState.authenticated) {
    concerts.value = []
    return
  }
  try {
    const response = await secureFetch('/api/user/events')
    if (response.ok) {
      concerts.value = await response.json()
    }
  } catch (e) {
    console.error('Failed to load saved concerts:', e)
  }
}

function onConcertSaved() {
  loadSavedConcerts()
  setTab('saved')
}

async function deleteConcert(eventId) {
  concerts.value = concerts.value.filter(c => c.id !== eventId)
  try {
    const response = await secureFetch(`/api/user/events/${eventId}`, { method: 'DELETE' })
    if (!response.ok) throw new Error(`${response.status}`)
  } catch (e) {
    console.error('Failed to delete concert:', e)
    loadSavedConcerts()
  }
}
  const selectedConcert = ref(null)
  const showConcertCommentPopUp = ref(false)

  function openComments(concert) {
    selectedConcert.value = concert
    showConcertCommentPopUp.value = true
  }
onMounted(loadSavedConcerts)

watch(() => authState.authenticated, loadSavedConcerts)
</script>

<template>
  <section class="dashboard">
    <nav class="tabs" role="tablist">
      <button
        role="tab"
        class="tab"
        :class="{ 'tab--active': activeTab === 'search' }"
        :aria-selected="activeTab === 'search'"
        @click="setTab('search')"
      >
        Search
      </button>
      <button
        role="tab"
        class="tab"
        :class="{ 'tab--active': activeTab === 'saved' }"
        :aria-selected="activeTab === 'saved'"
        @click="setTab('saved')"
      >
        Saved concerts
      </button>
    </nav>
      
    <ConcertCommentPopUp
      v-if="showConcertCommentPopUp && selectedConcert"
      :concert="selectedConcert"
      @close="showConcertCommentPopUp = false"
    />

    <div v-show="activeTab === 'search'" class="tab-panel">
      <EventSearchPanel
        :saved-event-ids="savedEventIds"
        @concert-saved="onConcertSaved"
      />
    </div>

    <div v-show="activeTab === 'saved'" class="tab-panel">
      <div v-if="!authState.authenticated" class="empty-hint">
        Log in to save concerts to your profile.
      </div>
      <div v-else-if="concerts.length > 0" class="concert-grid">
        <ConcertCard
          v-for="concert in concerts"
          :key="concert.id"
          :concert="concert"
          @delete="deleteConcert"
          @comments="openComments"
        />
      </div>
      <p v-else class="empty-hint">
        No saved concerts yet. Search for something and hit "Save to profile".
      </p>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  padding: 1.5rem;
}

.tabs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--border);
}

.tab {
  padding: 0.6rem 1.1rem;
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--text-3);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: -1px;
  border-radius: 8px 8px 0 0;
  transition: color 0.15s ease, border-color 0.15s ease;
}

.tab:hover {
  color: var(--text-1);
}

.tab--active {
  color: var(--text-1);
  border-bottom-color: var(--accent-blue);
}

.tab-panel {
  min-height: 200px;
}

.concert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 1.2rem;
}

.empty-hint {
  color: var(--text-3);
  font-size: 0.95rem;
}
</style>
