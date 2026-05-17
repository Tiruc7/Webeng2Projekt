import keycloak from '../keycloak/keycloak.js'

export async function secureFetch(url, options = {}) {
  // check for expired token
  try {
    await keycloak.updateToken(30); //30s
  } catch (error) {
    console.error("Session timed out", error);
    keycloak.login();
  }

  // add token to header
  const authOptions = {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${keycloak.token}`,
      'Content-Type': 'application/json'
    }
  };

  return fetch(url, authOptions);
}
