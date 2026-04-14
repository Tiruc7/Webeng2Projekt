<script setup>
const menuItems = [
  { id: 1, label: 'Profile' },
  { id: 2, label: 'Saved Concerts' },
  { id: 3, label: 'Calendar Export' },
  { id: 4, label: 'Settings' },
  { id: 5, label: 'Logout' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar__top">
      <div class="avatar">V</div>
      <div>
        <p class="sidebar__name">VForConcert</p>
        <p class="sidebar__sub">Demo User</p>
      </div>
    </div>

    <nav class="sidebar__nav">
      <button
        v-for="item in menuItems"
        :key="item.id"
        class="sidebar__button"
        type="button"
      >
        {{ item.label }}
      </button>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  height: 100%;
  background: var(--bg-surface);
  border-left: 1px solid var(--border);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.sidebar__top {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 1.5rem;
  color: white;
  background: linear-gradient(135deg, var(--accent-pink), var(--accent-blue));
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.18);
}

.sidebar__name {
  margin: 0;
  color: var(--text-1);
  font-weight: 700;
  font-size: 1.15rem;
}

.sidebar__sub {
  margin: 0.2rem 0 0;
  color: var(--text-3);
  font-size: 0.95rem;
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 0.95rem;
}

.sidebar__button {
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  padding: 1rem 1rem;
  border-radius: 18px;
  text-align: left;
  cursor: pointer;
  transition: 0.18s ease;
}

.sidebar__button:hover {
  transform: translateY(-2px);
  border-color: rgba(225, 29, 141, 0.45);
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.18);
}
</style>