<script setup>
import { ref, onMounted } from 'vue'

const message = ref('Loading...')

onMounted(async () => {
  const response = await fetch('/api/hello')
  message.value = await response.text()
})
</script>

<template>
  <main>
    <h1>{{ message }}</h1>
  </main>
</template>