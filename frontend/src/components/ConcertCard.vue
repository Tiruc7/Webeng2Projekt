<script setup>
import { computed } from 'vue'
import { useCountdown } from '../composables/useCountdown'

const props = defineProps({
  concert: {
    type: Object,
    required: true,
  },
})

const { countdown } = useCountdown(props.concert.dateTime)

const formattedDate = computed(() => {
  return new Date(props.concert.dateTime).toLocaleString('de-DE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
})

const statusLabel = computed(() => {
  return countdown.value.expired ? 'Past' : 'Upcoming'
})
</script>

<template>
  <article class="concert-card">
    <img
      :src="concert.image"
      :alt="concert.title"
      class="concert-card__image"
    />

    <div class="concert-card__body">
      <div class="concert-card__header">
        <span class="concert-card__status" :class="{ past: countdown.expired }">
          {{ statusLabel }}
        </span>
      </div>

      <h3 class="concert-card__title">{{ concert.title }}</h3>
      <p class="concert-card__venue">{{ concert.venue }} · {{ concert.city }}</p>
      <p class="concert-card__date">{{ formattedDate }}</p>
      <p class="concert-card__notes">{{ concert.notes }}</p>

      <div class="concert-card__countdown">
        <span class="label">Countdown</span>
        <strong>{{ countdown.text }}</strong>
      </div>
    </div>
  </article>
</template>

<style scoped>
.concert-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-soft);
  min-height: 430px;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.concert-card:hover {
  transform: translateY(-3px);
  border-color: rgba(37, 99, 235, 0.45);
}

.concert-card__image {
  width: 100%;
  height: 190px;
  object-fit: cover;
}

.concert-card__body {
  padding: 1.1rem 1.1rem 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
}

.concert-card__header {
  display: flex;
  justify-content: flex-end;
}

.concert-card__status {
  padding: 0.38rem 0.8rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-cyan));
}

.concert-card__status.past {
  background: linear-gradient(135deg, #475569, #334155);
}

.concert-card__title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--text-1);
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.concert-card__venue,
.concert-card__date,
.concert-card__notes {
  margin: 0;
  color: var(--text-2);
  font-size: 0.98rem;
}

.concert-card__countdown {
  margin-top: auto;
  padding: 0.95rem 1rem;
  border-radius: 16px;
  background: var(--accent-pink-soft);
  border: 1px solid rgba(225, 29, 141, 0.24);
}

.label {
  display: block;
  color: var(--text-3);
  font-size: 0.85rem;
  margin-bottom: 0.35rem;
}

.concert-card__countdown strong {
  color: var(--text-1);
  font-size: 1.02rem;
}
</style>