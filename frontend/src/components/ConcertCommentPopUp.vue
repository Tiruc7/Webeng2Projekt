<script setup>
import { onMounted, ref } from 'vue'

const props = defineProps({
  concert: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['close'])

const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const error = ref('')

async function loadComments() {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch(`/api/events/${props.concert.id}/comments`)

    if (!response.ok) {
      throw new Error('Kommentare konnten nicht geladen werden')
    }

    comments.value = await response.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return

  try {
    const response = await fetch(`/api/events/${props.concert.id}/comments?userId=3`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    content: newComment.value,
  }),
})
    if (!response.ok) {
      throw new Error('Kommentar konnte nicht gespeichert werden')
    }

    const savedComment = await response.json()
    comments.value.push(savedComment)
    newComment.value = ''
  } catch (err) {
    error.value = err.message
  }
}

onMounted(loadComments)
</script>

<template>
  <div class="comment-popup__backdrop">
    <div class="comment-popup">
      <div class="comment-popup__header">
        <h2>Comments</h2>
        <button type="button" @click="emit('close')">×</button>
      </div>

      <p class="comment-popup__event">
        {{ concert.title }}
      </p>

      <p v-if="loading">Loading comments...</p>
      <p v-if="error" class="comment-popup__error">{{ error }}</p>

      <div class="comment-popup__list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-popup__item"
        >
          <strong>{{ comment.username }}</strong>
          <p>{{ comment.content }}</p>
          <small>{{ comment.createdAt }}</small>
        </div>

        <p v-if="!loading && comments.length === 0">
          Noch keine Kommentare.
        </p>
      </div>

      <div class="comment-popup__form">
        <textarea
          v-model="newComment"
          placeholder="Kommentar schreiben..."
        />
        <button type="button" @click="submitComment">
          Send
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-popup__backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.comment-popup {
  width: min(520px, 92vw);
  max-height: 80vh;
  overflow: auto;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: 24px;
  padding: 1.25rem;
  box-shadow: var(--shadow-soft);
}

.comment-popup__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-popup__header h2 {
  margin: 0;
  color: var(--text-1);
}

.comment-popup__header button {
  background: transparent;
  border: none;
  color: var(--text-2);
  font-size: 1.5rem;
  cursor: pointer;
}

.comment-popup__event {
  color: var(--text-2);
  margin-bottom: 1rem;
}

.comment-popup__list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin: 1rem 0;
}

.comment-popup__item {
  background: var(--bg-surface-2);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 0.8rem;
}

.comment-popup__item strong {
  color: var(--text-1);
}

.comment-popup__item p {
  margin: 0.35rem 0;
  color: var(--text-2);
}

.comment-popup__item small {
  color: var(--text-3);
}

.comment-popup__form {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.comment-popup__form textarea {
  min-height: 90px;
  resize: vertical;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  padding: 0.8rem;
}

.comment-popup__form button {
  align-self: flex-end;
  border: none;
  border-radius: 999px;
  padding: 0.55rem 1rem;
  background: var(--accent-blue);
  color: white;
  font-weight: 700;
  cursor: pointer;
}

.comment-popup__error {
  color: #f87171;
}
</style>