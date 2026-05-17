import { reactive } from 'vue'
import keycloak from '../keycloak/keycloak'
import { secureFetch } from '../api/api.js'

export const authState = reactive({
  initialized: false,
  authenticated: false,
  username: 'Not logged in',
  roles: [],
  isAdmin: false,
  userId: null,
})

export async function syncAuthState() {
  authState.initialized = true
  authState.authenticated = !!keycloak.authenticated

  if (!keycloak.authenticated) {
    authState.username = 'Not logged in'
    authState.roles = []
    authState.isAdmin = false
    authState.userId = null
    return
  }

  const parsed = keycloak.tokenParsed ?? keycloak.idTokenParsed ?? {}

  authState.username =
    parsed.preferred_username ||
    parsed.name ||
    parsed.email ||
    'Logged in'

  const realmRoles = parsed.realm_access?.roles ?? []
  authState.roles = realmRoles
  authState.isAdmin = realmRoles.includes('ADMIN')

  try {
      const response = await secureFetch('/api/user/sync')
      const userData = await response.json()
      if(authState.userId == null){
        authState.userId = userData.id
      }
    } catch (error) {
      console.error('Failed to sync user with backend', error)
    }

  if (authState.username === 'Logged in') {
    try {
      const profile = await keycloak.loadUserProfile()
      authState.username =
        profile.username ||
        profile.firstName ||
        profile.lastName ||
        'Logged in'
    } catch (error) {
      console.warn('User-Profil could not be loaded', error)
    }
  }
}
window.authState = authState;
