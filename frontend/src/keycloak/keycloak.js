import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8080',
  realm: 'EventKC',
  clientId: 'EventKC_Frontend'
});

export default keycloak;
