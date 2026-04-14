<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  suggestions: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['search', 'select'])

const query = ref('')
const isOpen = ref(false)

const filteredSuggestions = computed(() => {
  const value = query.value.trim().toLowerCase()

  if (!value) {
    return props.suggestions.slice(0, 5)
  }

  return props.suggestions.filter((item) =>
    item.toLowerCase().includes(value),
  )
})

function handleInput() {
  isOpen.value = true
  emit('search', query.value)
}

function selectSuggestion(item) {
  query.value = item
  isOpen.value = false
  emit('select', item)
}

function closeDropdown() {
  setTimeout(() => {
    isOpen.value = false
  }, 120)
}
</script>

<template>
  <div class="search">
    <input
      v-model="query"
      class="search__input"
      type="text"
      placeholder="Search metal concerts, bands or cities..."
      @input="handleInput"
      @focus="isOpen = true"
      @blur="closeDropdown"
      @keyup.enter="$emit('search', query)"
    />

    <div
      v-if="isOpen && filteredSuggestions.length"
      class="search__dropdown"
    >
      <button
        v-for="item in filteredSuggestions"
        :key="item"
        type="button"
        class="search__item"
        @mousedown.prevent="selectSuggestion(item)"
      >
        {{ item }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.search {
  position: relative;
  width: min(820px, 100%);
}

.search__input {
  width: 100%;
  border: 1px solid var(--border);
  background: var(--bg-surface);
  color: var(--text-1);
  padding: 1rem 1.15rem;
  border-radius: 20px;
  outline: none;
  font-size: 1rem;
  box-shadow: var(--shadow-soft);
}

.search__input::placeholder {
  color: var(--text-3);
}

.search__input:focus {
  border-color: rgba(37, 99, 235, 0.7);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.18);
}

.search__dropdown {
  position: absolute;
  top: calc(100% + 0.7rem);
  left: 0;
  right: 0;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  overflow: hidden;
  z-index: 20;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.search__item {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--text-2);
  text-align: left;
  padding: 0.95rem 1rem;
  cursor: pointer;
}

.search__item:hover {
  background: var(--accent-pink-soft);
  color: var(--text-1);
}
</style>