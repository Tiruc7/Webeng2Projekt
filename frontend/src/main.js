import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import keycloak from './keycloak/keycloak';


keycloak.init({
  onLoad: 'check-sso',
  pkceMethod: 'S256'
}).then((_authenticated) => {
  const app = createApp(App)

  app.config.globalProperties.$keycloak = keycloak
  app.use(router)
  app.mount('#app')
}).catch(err => {
  console.error("Keycloak konnte nicht initialisiert werden", err)
})
