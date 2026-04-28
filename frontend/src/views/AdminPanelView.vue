<script setup>
import { ref } from 'vue'
import { secureFetch } from '../api/api.js'

const activeSection = ref('')
const loading = ref(false)
const errorMessage = ref('')

const users = ref([])
const events = ref([])
const userEvents = ref([])

async function openSection(section) {
  if (activeSection.value === section) {
    activeSection.value = ''
    return
  }

  activeSection.value = section
  errorMessage.value = ''

  if (section === 'users' && users.value.length === 0) {
    await loadUsers()
  }

  if (section === 'events' && events.value.length === 0) {
    await loadEvents()
  }

  if (section === 'userEvents' && userEvents.value.length === 0) {
    await loadUserEvents()
  }
}

async function loadUsers() {
  loading.value = true
  try {
    const response = await secureFetch('/api/admin/users')
    if (!response.ok) {
      throw new Error(`Users could not be loaded (${response.status})`)
    }
    users.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

async function loadEvents() {
  loading.value = true
  try {
    const response = await secureFetch('/api/admin/events')
    if (!response.ok) {
      throw new Error(`Events could not be loaded (${response.status})`)
    }
    events.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

async function loadUserEvents() {
  loading.value = true
  try {
    const response = await secureFetch('/api/admin/user-events')
    if (!response.ok) {
      throw new Error(`User-Event-Assignment could not be loaded (${response.status})`)
    }
    userEvents.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="admin-panel">
    <header class="admin-panel__header">
      <h1>Admin Panel</h1>
      <p>Manage Users,Events from the database</p>
    </header>

    <div class="admin-panel__tabs">
      <button class="admin-tab" @click="openSection('users')">
        User Management
      </button>

      <button class="admin-tab" @click="openSection('events')">
        Event Management
      </button>

      <button class="admin-tab" @click="openSection('userEvents')">
        API Settings
      </button>
    </div>

    <div class="admin-panel__content">
      <p v-if="loading" class="admin-info">Lade Daten...</p>
      <p v-if="errorMessage" class="admin-error">{{ errorMessage }}</p>

      <div v-if="activeSection === 'users'" class="admin-card">
        <h2>Users</h2>
        <table v-if="users.length > 0" class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else>Keine User gefunden.</p>
      </div>

      <div v-if="activeSection === 'events'" class="admin-card">
        <h2>Events</h2>
        <table v-if="events.length > 0" class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="event in events" :key="event.id">
              <td>{{ event.id }}</td>
              <td>{{ event.name }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else>Keine Events gefunden.</p>
      </div>

    </div>
  </section>
</template>

<style scoped>
.admin-panel {
  padding: 2rem;
  color: var(--text-1);
}

.admin-panel__header {
  margin-bottom: 1.5rem;
}

.admin-panel__header h1 {
  margin-bottom: 0.5rem;
}

.admin-panel__header p {
  color: var(--text-3);
}

.admin-panel__tabs {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.admin-tab {
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  padding: 1rem 1.2rem;
  border-radius: 16px;
  cursor: pointer;
  transition: 0.2s ease;
}

.admin-tab:hover {
  border-color: rgba(225, 29, 141, 0.5);
  background: var(--accent-pink-soft);
}

.admin-panel__content {
  margin-top: 1rem;
}

.admin-card {
  border: 1px solid var(--border);
  background: rgba(15, 23, 42, 0.92);
  border-radius: 18px;
  padding: 1.25rem;
  box-shadow: var(--shadow-soft);
}

.admin-card h2 {
  margin-bottom: 1rem;
}

.admin-note {
  margin-bottom: 1rem;
  color: var(--text-3);
}

.admin-info {
  margin-bottom: 1rem;
  color: #7dd3fc;
}

.admin-error {
  margin-bottom: 1rem;
  color: #fda4af;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  border-bottom: 1px solid var(--border);
  padding: 0.85rem;
  text-align: left;
}

.admin-table th {
  color: var(--text-2);
}
</style>