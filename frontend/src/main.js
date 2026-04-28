import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import keycloak from './keycloak/keycloak';
import { syncAuthState } from './auth/authState'

keycloak.onReady = async () => {
  await syncAuthState()
}

keycloak.onAuthSuccess = async () => {
  await syncAuthState()
}

keycloak.onAuthRefreshSuccess = async () => {
  await syncAuthState()
}

keycloak.onAuthLogout = async () => {
  await syncAuthState()
}

keycloak.init({
  onLoad: 'check-sso',
  pkceMethod: 'S256'
}).then(async () => {
  await syncAuthState()

  const app = createApp(App)

  app.config.globalProperties.$keycloak = keycloak
  app.use(router)
  app.mount('#app')
}).catch(err => {
  console.error("Keycloak konnte nicht initialisiert werden", err)
})
