<script setup>
import { computed, ref } from 'vue'
import UserMenu from '../components/UserMenu.vue'
import SearchBar from '../components/SearchBar.vue'
import ConcertCard from '../components/ConcertCard.vue'
import { mockConcerts } from '../data/mockConcerts'

const concerts = ref(mockConcerts)

const apiSearchQuery = ref('')
const selectedSuggestion = ref('')

const apiSuggestions = computed(() => [
  'Architects',
  'Bring Me The Horizon',
  'Sleep Token',
  'Bad Omens',
  'Lorna Shore',
])

function handleSearch(value) {
  apiSearchQuery.value = value
}

function handleSelect(value) {
  selectedSuggestion.value = value
  apiSearchQuery.value = value
}
</script>

<template>
  <div class="dashboard-layout">
    <main class="dashboard-main">
      <header class="hero">
        <p class="hero__tag">CONCERT DASHBOARD</p>
        <h1>Your saved events at a glance</h1>
        <p class="hero__text">
          Search for new concerts via the future API search and keep your saved events below.
        </p>

        <SearchBar
          :suggestions="apiSuggestions"
          @search="handleSearch"
          @select="handleSelect"
        />

        <p class="search-info">
          API search preview:
          <span v-if="apiSearchQuery">{{ apiSearchQuery }}</span>
          <span v-else>nothing entered yet</span>
        </p>
      </header>

      <section class="concert-section">
        <div class="concert-section__top">
          <h2>Saved concerts</h2>
          <span>{{ concerts.length }} items</span>
        </div>

        <div class="concert-grid">
          <ConcertCard
            v-for="concert in concerts"
            :key="concert.id"
            :concert="concert"
          />
        </div>
      </section>
    </main>

    <UserMenu />
  </div>
</template>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background: var(--bg-app);
}

.dashboard-main {
  padding: 2rem;
  padding-right: 2rem;
  overflow-y: auto;
}

.hero {
  max-width: 1280px;
  margin: 0 auto 2rem;
  padding-right: 280px;
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

</style>