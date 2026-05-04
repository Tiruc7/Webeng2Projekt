<script setup>
import { computed, ref } from 'vue'
import EventSearchPanel from '../components/EventSearchPanel.vue'
import ConcertCard from '../components/ConcertCard.vue'
import { mockConcerts } from '../data/mockConcerts'

const concerts = ref(mockConcerts)
const lastSubmittedSearch = ref(null)

const apiSearchQuery = ref('')
const selectedSuggestion = ref('')

const apiSuggestions = computed(() => [
  'Architects',
  'Bring Me The Horizon',
  'Sleep Token',
  'Bad Omens',
  'Lorna Shore',
])

function handleSearch(payload) {
  lastSubmittedSearch.value = payload
  console.log('Phase 1 search payload:', payload)
}

function handleSelect(value) {
  selectedSuggestion.value = value
  apiSearchQuery.value = value
}
</script>

<template>
  <section class="dashboard">
    <EventSearchPanel
      :seed-suggestions="concerts"
      @search="handleSearch"
    />

    <div v-if="lastSubmittedSearch" class="submitted-search-box">
      <h3>Last submitted search</h3>
      <p><strong>Keyword:</strong> {{ lastSubmittedSearch.keyword }}</p>
      <p><strong>City:</strong> {{ lastSubmittedSearch.city || '—' }}</p>
      <p><strong>Date from:</strong> {{ lastSubmittedSearch.dateFrom || '—' }}</p>
      <p><strong>Date to:</strong> {{ lastSubmittedSearch.dateTo || '—' }}</p>
      <p><strong>Size:</strong> {{ lastSubmittedSearch.size }}</p>
    </div>

    <div class="concert-grid">
      <ConcertCard
        v-for="concert in concerts"
        :key="concert.title + concert.venue"
        :concert="concert"
      />
    </div>
  </section>
</template>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background: var(--bg-app);
}

.dashboard-main {
  padding: 2rem;
  overflow-y: auto;
}

.hero {
  max-width: 1280px;
  margin: 0 auto 2rem;
  padding-right: var(--user-menu-offset);
}

.hero__tag {
  display: inline-block;
  margin: 0 0 0.75rem;
  color: #f9a8d4;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.88rem;
}

.hero h1 {
  margin: 0 0 0.9rem;
  color: var(--text-1);
  font-size: clamp(2.8rem, 5vw, 4.8rem);
  line-height: 1.02;
  letter-spacing: -0.04em;
  font-weight: 800;
  max-width: 900px;
}

.hero__text {
  margin: 0 0 1.5rem;
  color: var(--text-3);
  max-width: 760px;
  line-height: 1.65;
  font-size: 1.05rem;
}

.search-info {
  margin: 0.95rem 0 0;
  color: var(--text-3);
  font-size: 0.95rem;
}

.search-info span {
  color: var(--accent-cyan);
  font-weight: 700;
}

.concert-section {
  max-width: 1280px;
  margin: 0 auto;
}

.concert-section__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.concert-section__top h2 {
  color: var(--text-1);
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.concert-section__top span {
  color: var(--text-3);
  font-weight: 500;
}

.concert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(290px, 1fr));
  gap: 1.2rem;
}

@media (max-width: 980px) {
  .hero {
    padding-right: 0;
    padding-top: 5rem;
  }
}
<style scoped>
.submitted-search-box {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.82);
  color: var(--text-1);
}

.submitted-search-box h3 {
  margin-top: 0;
}

</style>