# EventPlanner (Webeng2Projekt)

  

## Beschreibung

EventPlanner ist ein Event-Planer, mit dem Nutzer über die Ticketmaster API nach

Konzerten und Veranstaltungen suchen, diese in ihrem persönlichen Profil speichern

und in einer übersichtlichen Kachelansicht mit Countdown bis zum Event verwalten

können. Nutzer können sich außerdem gegenseitig als Freunde hinzufügen und

gespeicherte Konzerte kommentieren.

  

- Was macht das Projekt?

  -> Konzerte/Events suchen, speichern, mit Countdown anzeigen, als Kalenderdatei

  exportieren, mit anderen Nutzern befreunden und Konzerte kommentieren.

- Für wen ist es gedacht?

  -> Für Konzertbesucher, die ihre geplanten Events an einem Ort sammeln, Vorfreude

  aufbauen und sich mit Freunden über anstehende Konzerte austauschen wollen.

- Welches Problem wird gelöst?

  -> Konzerttermine sind über viele Plattformen verstreut. EventPlanner bündelt die

  persönliche Eventplanung an einem Ort, macht sie in geläufige Kalender exportierbar

  und ergänzt sie um eine soziale Komponente.

  

## Umfang

### Funktionen

- [x] Nutzerauthentifizierung über Keycloak (Login / Logout)

- [x] Selbstregistrierung neuer Accounts über die Keycloak-Anmeldeseite

- [x] Konzertsuche über die Ticketmaster Discovery API

- [x] Live-Suchvorschläge (debounced, ab 3 Zeichen)

- [x] Erweiterte Suchfilter (Stadt, Zeitraum von/bis)

- [x] Server-seitiges Caching der API-Ergebnisse über Redis (sparsamer Umgang mit dem Ticketmaster-API-Limit)

- [x] Konzerte zum eigenen Profil hinzufügen

- [x] Gespeicherte Konzerte wieder entfernen

- [x] Kachelansicht der gespeicherten Konzerte mit Countdown

- [x] Trennung in "Search" und "Saved concerts" Tabs

- [x] Export aller gespeicherten Konzerte als iCalendar-Datei (.ics)

- [x] Rollenbasierter Zugriff (USER / ADMIN) mit Admin-Panel

- [x] Automatisches Anlegen des Users in der Datenbank beim ersten Login (JIT Provisioning)

- [x] Freundschaftssystem: Anfrage senden, annehmen/ablehnen, Freundesliste, eigene Profilseite

- [x] Kommentare zu gespeicherten Konzerten (sichtbar für alle, die das jeweilige Konzert ebenfalls gespeichert haben)

- [x] Logging wichtiger Aktionen im Backend (SLF4J)

- [x] Automatisierte Tests (Backend-Unit-Tests und Vue-Komponententests)

- [x] Vollständige Containerisierung aller Dienste über eine zentrale Docker-Compose-Datei

- [x] Realistische, automatisch gesäte Testdaten (relative Termine, echte Test-Accounts, Platzhalterbilder)

  

### Optionale Funktionen

- [ ] Event-Notizen (private Notizen zu einzelnen Konzerten)

- [ ] Lösen der bekannten Einschränkungen (siehe unten)

  

## Eingesetzte Technologien

- Frontend: Vue 3 mit Vite, Vue Router, keycloak-js

- Backend: Spring Boot 3.5 (Java 21)

- Datenbank / Speicherung: MySQL 8.0, Schema-Versionierung über Liquibase

- Caching: Redis 7

- Authentifizierung: Keycloak 26 (OAuth2 / OIDC)

- Externe API: Ticketmaster Discovery API v2

- Framework(s): Spring Security (OAuth2 Resource Server), Spring Data JPA, Spring Cache

- Logging: SLF4J / Logback (in Spring Boot enthalten)

- Tests: JUnit 5, Mockito, AssertJ (Backend); Vitest, Vue Test Utils (Frontend)

- Containerisierung: Docker, Docker Compose, nginx (liefert das gebaute Frontend im Container aus)

- Weitere Bibliotheken / Tools: MySQL Workbench (DB-Inspektion)

  

## Projektstruktur

Das Projekt ist in drei Hauptbereiche plus Infrastruktur aufgeteilt.

  

- `frontend/` – Vue 3 + Vite Single Page Application

  - `Dockerfile`, `nginx.conf` – Build- und Auslieferungs-Setup für den Frontend-Container

  - `src/components/` – Wiederverwendbare Komponenten (ConcertCard, SearchBar, UserMenu, ConcertCommentPopUp, ...)

  - `src/components/__tests__/` – Vitest-Komponententests

  - `src/views/` – Seiten (DashboardView, AdminPanelView, LoginView, ProfileView)

  - `src/auth/` – Reaktiver Auth-State, synchronisiert mit Keycloak

  - `src/api/` – secureFetch-Helper, der automatisch den JWT-Token anhängt

  - `src/keycloak/` – Keycloak-JS Adapter-Konfiguration

  - `src/router/` – Vue Router inkl. Auth-Guards

- `backend/` – Spring Boot Anwendung

  - `Dockerfile` – Multi-Stage-Build (Maven-Build, schlankes JRE-Image)

  - `controller/` – REST-Endpunkte (Search, User, Event, Admin, Friends, Comments)

  - `service/` – Geschäftslogik (TMService, ICalService, UserEventService, UserService, FriendshipService, CommentService)

  - `helper/` – TMQueryBuilder für den Aufbau der Ticketmaster-URLs

  - `dto/` – Datentransferobjekte (EventDTO, UserDTO, CommentDTO, UserFriendshipDTO, ...)

  - `entity/` + `repository/` – JPA-Entities und Repositories

  - `enums/` – z. B. `FriendshipStatus` (PENDING, ACCEPTED, BLOCKED)

  - `config/` – Cache-Konfiguration (Redis) und RestTemplate-Bean

  - `Keycloak/` – SecurityConfig, Rollen-Konverter, zentraler OwnershipGuard

  - `resources/db/changelog/` – Liquibase Changelogs (Schema, Testdaten, Friends-/Comments-Tabellen)

  - `src/test/` – Backend-Tests

- `Keycloak/imports/` – Realm-Import (Rollen, Test-Accounts)

- `docker-compose.yml` – zentrale Compose-Datei für das gesamte Projekt (Projekt-Root)

  

## Setup

### Variante A: Start mit Docker (empfohlen)

Das ist der einfachste Weg und der, mit dem das Projekt abgegeben wird. Es startet

das komplette System mit einem einzigen Befehl: MySQL, Redis, Keycloak, Backend und

Frontend.

  

**Voraussetzung:** Docker Desktop (mit aktivierter Virtualisierung).

  

1. Repository klonen

   ```

   git clone https://github.com/Tiruc7/Webeng2Projekt.git

   cd Webeng2Projekt

   ```

  

2. (Nur falls der Token irgendwann mal abgelaufen oder nicht sein sollte)`.env`-Datei anlegen (Kopie von `.env.example`) und einen gültigen Ticketmaster-API-Key eintragen:

   ```

   TMKey=DEIN_TICKETMASTER_KEY

   ```


3. Alles starten

   ```

   docker compose up --build

   ```

  

4. Im Browser öffnen: http://localhost:5173

  

Folgende Dienste laufen danach: Frontend (Port 5173), Backend (Port 8090), Keycloak

(Port 8080), MySQL (Port 3307), Redis (Port 6379).

  

Zum Stoppen: `docker compose down`

Zum vollständigen Zurücksetzen (inkl. Datenbank, Testdaten werden beim nächsten Start neu gesät): `docker compose down -v`

  

### Variante B: Lokale Entwicklung ohne Docker

Für die aktive Weiterentwicklung im Team wurde überwiegend so gearbeitet. Diese

Variante eignet sich, wenn man direkt am Code arbeiten und Hot-Reload nutzen möchte.

  

**Voraussetzungen:**

- Java 21 (JDK)

- Node.js 20 oder neuer (inkl. npm)

- IntelliJ IDEA (Backend) und VSCode (Frontend) empfohlen

- Docker Desktop (für MySQL, Redis, Keycloak als Container)

- MySQL Workbench (optional, zur DB-Inspektion)

  

1. Redis starten

   ```

   docker run -d --name redis-dev -p 6379:6379 redis:7-alpine

   ```

  

2. MySQL-Container starten

   ```

   docker run -d --name mysql-dev \

     -e MYSQL_ROOT_PASSWORD=root \

     -e MYSQL_DATABASE=event_db \

     -p 3307:3306 \

     mysql:8.0

   ```

  

3. Keycloak starten

   ```

   cd Keycloak

   docker compose up -d

   cd ..

   ```

  

4. Backend starten

   - In IntelliJ den `backend/` Ordner öffnen

   - Umgebungsvariable `TMKey` (Ticketmaster API-Key) in der Run Configuration setzen

   - Für ausführliche Logs und automatisch gesäte Testdaten das Profil `dev` aktivieren

     (Run Configuration, Active profiles: dev)

   - `BackendApplication` starten (läuft auf Port 8090)

  

5. Frontend starten

   ```

   cd frontend

   npm install

   npm run dev

   ```

   Frontend läuft auf Port 5173.

  

## Tests

Geprüft werden die kritischen und komplexen Bereiche des Projekts. Dafür gibt es

automatisierte Tests sowie nachvollziehbare manuelle Testfälle. Eine vollständige

Testabdeckung ist nicht das Ziel.

  

### Automatisierte Tests

Backend (JUnit 5, Mockito, AssertJ; unter `backend/src/test/`):

- `ICalServiceTest` – Erzeugung der Kalenderdatei (Aufbau, ganztägige vs. terminierte

  Events, Escaping von Sonderzeichen, leere Liste).

- `TMServiceTest` – Parsen der Ticketmaster-Antwort (alle Felder, fehlende Felder,

  leere Antwort, fehlerhaftes JSON).

- `TMServiceCachingTest` – Caching greift bei identischer Anfrage, unterschiedliche

  Anfragen erzeugen getrennte Cache-Einträge.

- `UserEventServiceTest` – Speichern, Duplikat-Schutz, unbekannter Nutzer, Löschen

  verwaister Events, Behalten eines Events mit weiteren Besitzern.

- `UserServiceTest` – Login eines bestehenden Nutzers, JIT-Provisioning beim ersten Login.

- `BackendApplicationTests` – Smoke-Test, dass die Anwendung vollständig hochfährt.

  

Frontend (Vitest, Vue Test Utils; unter `frontend/src/components/__tests__/`):

- `ConcertCard.spec.js` – Anzeige der Konzertdaten, Löschen bestätigen und abbrechen.

- `SearchBar.spec.js` – Suche beim Tippen, Anzeige der Vorschläge, Auswahl eines Vorschlags.

  

Hinweis: Für die neueren Features Freundschaften und Kommentare existieren aktuell

keine automatisierten Tests, sie sind ausschließlich über die manuellen Testfälle

unten abgedeckt (siehe auch "Bekannte Einschränkungen").

  

### Tests ausführen

Backend (im Ordner `backend/`):

```

mvnw test          (Linux/Mac: ./mvnw test)

```

Hinweis: Die Service-Unit-Tests laufen ohne Infrastruktur. Der Smoke-Test

`BackendApplicationTests` fährt die komplette Anwendung hoch und benötigt daher eine

laufende MySQL-Datenbank.

  

Frontend (im Ordner `frontend/`):

```

npm install        (einmalig, falls noch nicht geschehen)

npm run test:unit

```

Die Vue-Tests laufen vollständig im Speicher und benötigen keine Infrastruktur.

  

### Manuelle Testfälle

| Aktion | Erwartetes Ergebnis |

| --- | --- |

| Als `user` einloggen | Dashboard lädt, drei vorbelegte Konzerte sind bereits gespeichert |

| Als `test` einloggen | Dashboard lädt, drei andere vorbelegte Konzerte sind bereits gespeichert |

| Neuen Account über die Keycloak-Seite registrieren | Login möglich, beim ersten Login Log "JIT provisioning" |

| Nach einem Konzert suchen | Ergebnisse als Kacheln; genau ein "Ticketmaster API call" im Log |

| Dieselbe Suche erneut ausführen | Kein neuer API-Call (Ergebnis aus dem Redis-Cache) |

| Konzert speichern | Erscheint im "Saved"-Tab mit laufendem Countdown |

| Konzert entfernen | Verschwindet aus "Saved" |

| Als `user` eine Freundschaftsanfrage an `test` senden, als `test` annehmen | Beide erscheinen gegenseitig in der Freundesliste |

| Als `user` einen seiner gespeicherten Konzerte kommentieren | Kommentar erscheint; für andere Nutzer sichtbar, die dasselbe Konzert gespeichert haben |

| Kalenderexport auslösen | .ics-Datei wird heruntergeladen und ist in einen Kalender importierbar |

| Fremdes, nicht selbst gespeichertes Konzert über die API löschen (z. B. per Browser-Devtools) | Antwort 403 Forbidden |

| Als normaler USER einen /api/admin-Endpunkt aufrufen | Antwort 403 |

| Als `admin` das Admin-Panel öffnen | Nutzerliste wird angezeigt |

  

## Users

| Name | Passwort | Funktion |

| --- | --- | --- |

| admin | admin | Rolle ADMIN, Zugriff auf das Admin-Panel |

| user | user | Standard-Account (Rolle USER), kommt mit drei vorbelegten gespeicherten Konzerten |

| test | test | Standard-Account (Rolle USER), kommt mit drei anderen vorbelegten gespeicherten Konzerten; zusammen mit `user` ideal, um Freundschafts- und Kommentarfunktionen direkt auszuprobieren |

  

Login erfolgt über die Keycloak-Anmeldeseite des Frontends bzw. direkt unter

http://localhost:8080 (Realm `EventKC`).

  

## Bekannte Einschränkungen

- Die Backend-Tests setzen für den Smoke-Test eine laufende MySQL-Datenbank voraus.

- Die Zeitzone beim Kalenderexport wird vereinfacht als UTC behandelt.

- Das Ticketmaster API-Limit von 500 Anfragen pro Tag wird durch Caching abgefedert,

  bleibt aber eine harte Obergrenze.

- Die Live-Konzertsuche benötigt zwangsläufig eine Internetverbindung und einen

  gültigen Ticketmaster-API-Key (Kernkonzept der Anwendung). Alle übrigen Teile der

  Anwendung (Login, gespeicherte Testkonzerte, Freunde, Kommentare, Admin-Panel)

  laufen vollständig lokal in eigenen Containern, ohne Abhängigkeit von externen

  Cloud-Diensten.

- Event-Notizen (private Notizen zu einzelnen Konzerten) sind nicht umgesetzt.

  

## Contributors

<!-- PFLICHTANGABE LAUT ABGABE-ANFORDERUNG – VOR ABGABE AUSFÜLLEN -->

| Name | Github | Matrikelnummer |

| --- | --- |

| [David Bischof] | [Tiruc7]| [6524123] |

| [Damya Hennige] | [Damya761] | [9618477] |

| [Simon Ortlieb] | [Das-Nugget] | [2250100] |
