<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import keycloak from '../keycloak/keycloak.js'
import { authState } from '../auth/authState.js'
import { secureFetch } from '../api/api.js'

const router = useRouter()
const isOpen = ref(false)
const menuRef = ref(null)

const menuItems = computed(() => {
  const items = [
    { id: 1, label: 'Dashboard', routeName: 'dashboard' },
  ]

  if (authState.authenticated) {
    if (authState.isAdmin) {
      items.push({ id: 3, label: 'Admin Panel', routeName: 'admin' })
    }

    items.push({ id: 5, label: 'Logout', action: 'logout' })
    items.push({ id: 6, label: 'Profile', routeName: 'profile' })
  } else {
    items.push({ id: 2, label: 'Login', action: 'login' })
  }

  return items
})

const displayName = computed(() => {
  return authState.authenticated ? authState.username : 'Not logged in'
})

const avatarLetter = computed(() => {
  if (!authState.authenticated || !authState.username) {
    return 'C'
  }

  return authState.username.charAt(0).toUpperCase()
})

function toggleMenu() {
  isOpen.value = !isOpen.value
}

function closeMenu() {
  isOpen.value = false
}

async function navigate(item) {
  if (item.action === 'login') {
    await keycloak.login({ redirectUri: window.location.origin })
    return
  }

  if (item.action === 'logout') {
    authState.authenticated = false
    authState.username = 'Not logged in'
    authState.roles = []
    authState.isAdmin = false

    await keycloak.logout({ redirectUri: window.location.origin })
    return
  }

  if (!item.routeName) return

  router.push({ name: item.routeName })
  closeMenu()
}

const isExporting = ref(false)
const exportError = ref('')

async function exportToCalendar() {
  isExporting.value = true
  exportError.value = ''
  try {
    const response = await secureFetch('/api/user/events/export/ical')
    if (!response.ok) throw new Error(`Export failed (${response.status})`)

    // Trigger a file download via a temporary object URL
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'concerts.ics'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Calendar export failed:', e)
    exportError.value = e.message || 'Export failed'
  } finally {
    isExporting.value = false
  }
}

function handleClickOutside(event) {
  if (!menuRef.value) return

  if (!menuRef.value.contains(event.target)) {
    closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="menuRef" class="user-menu">
    <button
      class="user-menu__trigger"
      type="button"
      :aria-expanded="isOpen"
      aria-controls="user-menu-dropdown"
      @click.stop="toggleMenu"
    >
      <div class="user-menu__avatar">{{ avatarLetter }}</div>

      <div class="user-menu__text">
        <span class="user-menu__title">EventPlanner</span>
        <span
          class="user-menu__subtitle"
          :class="{
            'user-menu__subtitle--online': authState.authenticated,
            'user-menu__subtitle--offline': !authState.authenticated
          }"
        >
          {{ displayName }}
        </span>
      </div>
    </button>

    <div v-if="isOpen" id="user-menu-dropdown" class="user-menu__dropdown" role="menu">
      <button
        v-for="item in menuItems"
        :key="item.id"
        class="user-menu__button"
        :class="{ 'user-menu__button--disabled': !item.routeName && !item.action }"
        type="button"
        role="menuitem"
        :disabled="!item.routeName && !item.action"
        @click="navigate(item)"
      >
        {{ item.label }}
      </button>

      <template v-if="authState.authenticated">
        <button
          type="button"
          class="user-menu__button"
          :disabled="isExporting"
          @click="exportToCalendar"
        >
          {{ isExporting ? 'Exporting...' : 'Export to Calendar' }}
        </button>
        <p v-if="exportError" class="user-menu__error">{{ exportError }}</p>
      </template>
    </div>
  </div>
</template>

<style scoped>
.user-menu {
  position: fixed;
  top: 1.5rem;
  right: 1.5rem;
  z-index: 1000;
}

.user-menu__trigger {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  border: 1px solid var(--border);
  background: rgba(15, 23, 42, 0.9);
  color: var(--text-1);
  padding: 0.65rem 0.85rem;
  border-radius: 22px;
  cursor: pointer;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(14px);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    background 0.18s ease;
}

.user-menu__trigger:hover {
  transform: translateY(-2px);
  border-color: rgba(225, 29, 141, 0.5);
  background: rgba(22, 32, 51, 0.95);
}

.user-menu__avatar {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 1.25rem;
  font-weight: 800;
  color: white;
  background: linear-gradient(135deg, var(--accent-pink), var(--accent-blue));
}

.user-menu__text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding-right: 0.25rem;
}

.user-menu__title {
  font-size: 1rem;
  font-weight: 800;
  line-height: 1.1;
}

.user-menu__subtitle {
  margin-top: 0.2rem;
  font-size: 0.85rem;
}

.user-menu__subtitle--online {
  color: #7dd3fc;
}

.user-menu__subtitle--offline {
  color: var(--text-3);
}

.user-menu__dropdown {
  position: absolute;
  top: calc(100% + 0.8rem);
  right: 0;
  width: 260px;
  padding: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1px solid var(--border);
  border-radius: 22px;
  background: rgba(15, 23, 42, 0.96);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(16px);
}

.user-menu__button {
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  padding: 0.95rem 1rem;
  border-radius: 16px;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    background 0.18s ease;
}

.user-menu__button:hover {
  transform: translateY(-1px);
  background: var(--accent-pink-soft);
  border-color: rgba(225, 29, 141, 0.5);
}

.user-menu__button--disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.user-menu__button--disabled:hover {
  transform: none;
  background: var(--bg-surface-2);
  border-color: var(--border);
}


.user-menu__error {
  margin: 0;
  font-size: 0.82rem;
  color: #f87171;
  padding: 0 0.25rem;
}

@media (max-width: 720px) {
  .user-menu {
    top: 1rem;
    right: 1rem;
  }

  .user-menu__text {
    display: none;
  }

  .user-menu__dropdown {
    width: 230px;
  }
}
</style>
