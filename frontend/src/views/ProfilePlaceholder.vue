<script setup>
import { ref, onMounted } from 'vue'
import { secureFetch } from '../api/api.js'
import { authState } from '../auth/authState.js'

const loading = ref(false)
const errorMessage = ref('')

const user = ref({
  id: '',
  username: ''
})


async function loadProfile() {
  loading.value = true
  try {
    const userId = authState.userId;
    const response = await secureFetch('/api/user/profile/' + userId)
    if (!response.ok) {
      throw new Error(`Profile could not be loaded (${response.status})`)
    }
    user.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <main class="profile-layout">
    <div class="profile-container">
      <header class="profile-header">
        <p class="profile-tag">USER PROFILE</p>
        <h1>Welcome, {{ user.username }}</h1>
        <p class="profile-subtext">
          Manage your personal information and account security settings here.
        </p>
      </header>
    </div>
  </main>
</template>


<style scoped>
.profile-layout {
  min-height: 100vh;
  background: var(--bg-app);
  padding: 2rem;
}

.profile-container {
  max-width: 1100px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 3rem;
}

.profile-tag {
  color: var(--accent-blue); /* Analog zum Login-Design */
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
}

h1 {
  color: var(--text-1);
  font-size: clamp(2.5rem, 4vw, 4rem);
  letter-spacing: -0.04em;
  font-weight: 800;
  margin-bottom: 1rem;
}

.profile-subtext {
  color: var(--text-3);
  font-size: 1.1rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.profile-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 2rem;
  box-shadow: var(--shadow-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

h2 {
  font-size: 1.5rem;
  color: var(--text-1);
}

.info-list {
  display: grid;
  gap: 1.5rem;
}

.info-item label {
  display: block;
  color: var(--text-3);
  font-size: 0.85rem;
  margin-bottom: 0.4rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-item p {
  color: var(--text-1);
  font-size: 1.1rem;
  font-weight: 500;
}

.id-value {
  font-family: monospace;
  background: var(--bg-surface-2);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.role-badge {
  display: inline-block;
  background: var(--accent-pink-soft);
  color: var(--accent-pink);
  padding: 0.2rem 0.8rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

/* Button Styling analog zum Login/Admin-Tab */
button {
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  cursor: not-allowed;
  opacity: 0.6;
  transition: 0.2s;
}

.edit-btn {
  background: var(--bg-surface-2);
  color: var(--text-1);
}

.password-btn {
  width: 100%;
  margin-top: 2rem;
  background: linear-gradient(135deg, var(--accent-pink), var(--accent-blue));
  color: white;
  font-weight: 700;
}

.placeholder {
  opacity: 0.7;
}

.stats-list {
  margin-top: 1.5rem;
}

.stat-entry {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border);
  font-size: 0.95rem;
}

.status-active {
  color: var(--accent-cyan);
  font-weight: 700;
}

@media (max-width: 850px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
