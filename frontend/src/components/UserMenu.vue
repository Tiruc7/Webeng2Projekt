<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const isOpen = ref(false)
const menuRef = ref(null)

const menuItems = [
  { id: 1, label: 'Dashboard', routeName: 'dashboard' },
  { id: 2, label: 'Login', routeName: 'login' },
  { id: 3, label: 'Admin Panel', routeName: 'admin' },
  { id: 4, label: 'Settings', routeName: null },
  { id: 5, label: 'Logout', routeName: null },
]

function toggleMenu() {
  isOpen.value = !isOpen.value
}

function closeMenu() {
  isOpen.value = false
}

function handleClickOutside(event) {
  if (!menuRef.value) {
    return
  }

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
      <div class="user-menu__avatar">C</div>

      <div class="user-menu__text">
        <span class="user-menu__title">ConcertOrganizer</span>
        <span class="user-menu__subtitle">Demo User</span>
      </div>
    </button>

    <div v-if="isOpen" id="user-menu-dropdown" class="user-menu__dropdown" role="menu">
      <button
        v-for="item in menuItems"
        :key="item.id"
        class="user-menu__button"
        type="button"
      >
        {{ item.label }}
      </button>
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
  color: var(--text-3);
  font-size: 0.85rem;
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