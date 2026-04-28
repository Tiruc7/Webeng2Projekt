import { reactive } from 'vue'
import keycloak from '../keycloak/keycloak'

export const authState = reactive({
  initialized: false,
  authenticated: false,
  username: 'Not logged in',
  roles: [],
  isAdmin: false,
})

export async function syncAuthState() {
  authState.initialized = true
  authState.authenticated = !!keycloak.authenticated

  if (!keycloak.authenticated) {
    authState.username = 'Not logged in'
    authState.roles = []
    authState.isAdmin = false
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