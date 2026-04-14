import { ref, onMounted, onUnmounted } from 'vue'

function formatCountdown(diffMs) {
  if (diffMs <= 0) {
    return {
      expired: true,
      days: 0,
      hours: 0,
      minutes: 0,
      seconds: 0,
      text: 'Started / Passed',
    }
  }

  const totalSeconds = Math.floor(diffMs / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  return {
    expired: false,
    days,
    hours,
    minutes,
    seconds,
    text: `${days}d ${hours}h ${minutes}m ${seconds}s`,
  }
}

export function useCountdown(targetDate) {
  const countdown = ref(formatCountdown(new Date(targetDate) - new Date()))
  let intervalId = null

  const updateCountdown = () => {
    countdown.value = formatCountdown(new Date(targetDate) - new Date())
  }

  onMounted(() => {
    updateCountdown()
    intervalId = setInterval(updateCountdown, 1000)
  })

  onUnmounted(() => {
    if (intervalId) {
      clearInterval(intervalId)
    }
  })

  return {
    countdown,
  }
}