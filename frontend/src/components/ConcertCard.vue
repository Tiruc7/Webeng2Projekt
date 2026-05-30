<script setup>
import { computed, ref } from 'vue'
import { useCountdown } from '../composables/useCountdown'

const props = defineProps({
  concert: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['delete'])

const showConfirm = ref(false)

const dateTimeStr = computed(() => {
  if (!props.concert.date) return null
  const time = props.concert.time || '00:00:00'
  return `${props.concert.date}T${time}`
})

const { countdown } = useCountdown(dateTimeStr.value)

const formattedDate = computed(() => {
  if (!dateTimeStr.value) return ''
  return new Date(dateTimeStr.value).toLocaleString('de-DE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
})

const statusLabel = computed(() => {
  return countdown.value.expired ? 'Past' : 'Upcoming'
})

// Persist the initial remaining time per event so the ring doesn't reset on page reload.
// On first render: compute and store. On subsequent renders: read from localStorage.
const targetMs = dateTimeStr.value ? new Date(dateTimeStr.value).getTime() : 0
const storageKey = props.concert.id ? `ring_initial_${props.concert.id}` : null

let initialRemainingMs = 0
if (targetMs > 0) {
  const stored = storageKey ? localStorage.getItem(storageKey) : null
  if (stored !== null) {
    initialRemainingMs = parseFloat(stored)
  } else {
    initialRemainingMs = Math.max(0, targetMs - Date.now())
    if (storageKey) localStorage.setItem(storageKey, String(initialRemainingMs))
  }
}

const RING_RADIUS = 50
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS

const ringProgress = computed(() => {
  if (!countdown.value || countdown.value.expired || initialRemainingMs === 0) return 0
  const currentMs = (
    countdown.value.days * 86400
    + countdown.value.hours * 3600
    + countdown.value.minutes * 60
    + countdown.value.seconds
  ) * 1000
  return Math.min(currentMs / initialRemainingMs, 1)
})

const ringDashoffset = computed(() =>
  RING_CIRCUMFERENCE * (1 - ringProgress.value)
)
</script>

<template>
  <article class="concert-card">
    <img
      v-if="concert.imageUrl"
      :src="concert.imageUrl"
      :alt="concert.title"
      class="concert-card__image"
    />
    <div v-else class="concert-card__image concert-card__image--placeholder" />

    <div class="concert-card__body">
      <div class="concert-card__header">
        <span class="concert-card__status" :class="{ past: countdown.expired }">
          {{ statusLabel }}
        </span>
        <div v-if="showConfirm" class="concert-card__confirm">
          <button type="button" class="concert-card__confirm-yes" @click="emit('delete', concert.id)">Delete</button>
          <button type="button" class="concert-card__confirm-no" @click="showConfirm = false">Cancel</button>
        </div>
        <button
          v-else
          type="button"
          class="concert-card__delete"
          title="Remove from profile"
          @click="showConfirm = true"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="15" height="15">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
            <path d="M10 11v6M14 11v6"/>
            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
          </svg>
        </button>
      </div>

      <h3 class="concert-card__title">{{ concert.title }}</h3>
      <p class="concert-card__venue">{{ concert.venue }} · {{ concert.city }}</p>
      <p class="concert-card__date">{{ formattedDate }}</p>

      <div class="concert-card__ring-wrap">
        <svg
          class="concert-card__ring"
          viewBox="0 0 120 120"
          aria-hidden="true"
        >
          <circle
            class="ring-track"
            cx="60"
            cy="60"
            :r="RING_RADIUS"
            fill="none"
            stroke-width="8"
          />
          <circle
            class="ring-progress"
            :class="{ 'ring-progress--expired': countdown.expired }"
            cx="60"
            cy="60"
            :r="RING_RADIUS"
            fill="none"
            stroke-width="8"
            :stroke-dasharray="RING_CIRCUMFERENCE"
            :stroke-dashoffset="ringDashoffset"
            transform="rotate(-90 60 60)"
          />
        </svg>

        <div class="concert-card__ring-text">
          <template v-if="countdown.expired">
            <span class="ring-label">Past</span>
          </template>
          <template v-else>
            <span class="ring-value">{{ countdown.days }}</span>
            <span class="ring-label">days</span>
            <span class="ring-sub">{{ countdown.hours }}h {{ countdown.minutes }}m</span>
          </template>
        </div>
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

.concert-card__image--placeholder {
  background: var(--bg-surface-2);
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
  justify-content: space-between;
  align-items: center;
}

.concert-card__delete {
  background: transparent;
  border: none;
  color: var(--text-3);
  cursor: pointer;
  padding: 0.2rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: color 0.15s ease, background 0.15s ease;
}

.concert-card__delete:hover {
  color: #f87171;
  background: rgba(248, 113, 113, 0.1);
}

.concert-card__confirm {
  display: flex;
  gap: 0.4rem;
  align-items: center;
}

.concert-card__confirm-yes,
.concert-card__confirm-no {
  border: none;
  border-radius: 6px;
  padding: 0.25rem 0.6rem;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s ease;
}

.concert-card__confirm-yes {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
}

.concert-card__confirm-no {
  background: var(--bg-surface-2);
  color: var(--text-3);
}

.concert-card__confirm-yes:hover,
.concert-card__confirm-no:hover {
  opacity: 0.8;
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
.concert-card__date {
  margin: 0;
  color: var(--text-2);
  font-size: 0.98rem;
}

/* Countdown ring */

.concert-card__ring-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: auto;
  padding: 0.75rem 0 0.5rem;
}

.concert-card__ring {
  width: 110px;
  height: 110px;
  flex-shrink: 0;
}

.ring-track {
  stroke: var(--bg-surface-2);
}

.ring-progress {
  stroke: var(--accent-blue);
  stroke-linecap: round;
  transition: stroke-dashoffset 0.6s ease;
}

.ring-progress--expired {
  stroke: #475569;
}

.concert-card__ring-text {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.15;
  pointer-events: none;
}

.ring-value {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--text-1);
  line-height: 1;
}

.ring-label {
  font-size: 0.78rem;
  color: var(--text-3);
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.ring-sub {
  font-size: 0.72rem;
  color: var(--text-3);
  margin-top: 0.1rem;
}
</style>
