<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { secureFetch } from '../api/api.js'
import { authState } from '../auth/authState.js'

// --- State ---
const loading = ref(false)
const errorMessage = ref('')
const showFriendsPanel = ref(false)
const activeTab = ref('friends') // 'friends' | 'pending' | 'add'

const addFriendId = ref('')
const addFriendError = ref('')
const addFriendSuccess = ref('')
const addFriendLoading = ref(false)

const user = ref({ id: '', username: '' })
const friends = ref([])
const pendingRequests = ref([])

// Confirm delete
const confirmFriendshipId = ref(null)
const confirmUsername = ref('')

// Friend profile modal
const friendModal = ref(null) // { profile, events, loading, error }

// Tracks which event IDs are currently being saved (for loading state per button)
const savingIds = reactive({})
const saveErrors = reactive({})

// --- Load ---
async function loadProfile() {
  loading.value = true
  try {
    const userId = authState.userId
    const response = await secureFetch('/api/user/' + userId + '/profile')
    if (!response.ok) throw new Error(`Profile could not be loaded (${response.status})`)
    user.value = await response.json()
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}

async function loadFriends() {
  try {
    const res = await secureFetch('/api/friends')
    if (!res.ok) throw new Error()
    friends.value = await res.json()
  } catch {
    // silently fail
  }
}

async function loadPending() {
  try {
    const res = await secureFetch('/api/friends/pending')
    if (!res.ok) throw new Error()
    pendingRequests.value = await res.json()
  } catch {
    // silently fail
  }
}

function openFriendsPanel(tab = 'friends') {
  activeTab.value = tab
  showFriendsPanel.value = true
}

// --- Friend request ---
async function sendFriendRequest() {
  addFriendError.value = ''
  addFriendSuccess.value = ''
  const id = parseInt(addFriendId.value)

  if (!id || isNaN(id)) { addFriendError.value = 'Please enter a valid user ID.'; return }
  if (id === user.value.id) { addFriendError.value = "That's your own ID."; return }

  addFriendLoading.value = true
  try {
    const res = await secureFetch(`/api/friends/request/${id}`, { method: 'POST' })
    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Failed (${res.status})`)
    }
    addFriendSuccess.value = `Friend request sent to user #${id}.`
    addFriendId.value = ''
  } catch (e) {
    addFriendError.value = e.message
  } finally {
    addFriendLoading.value = false
  }
}

// --- Respond to request ---
async function respondToRequest(friendshipId, accept) {
  try {
    const res = await secureFetch(
      `/api/friends/${friendshipId}/respond?accept=${accept}`,
      { method: 'PUT' }
    )
    if (!res.ok) throw new Error()
    pendingRequests.value = pendingRequests.value.filter(r => r.friendshipId !== friendshipId)
    if (accept) await loadFriends()
  } catch { /* show toast */ }
}

// --- Remove friend (with confirm) ---
function askRemoveFriend(friendshipId, username) {
  confirmFriendshipId.value = friendshipId
  confirmUsername.value = username
}

function cancelRemove() {
  confirmFriendshipId.value = null
  confirmUsername.value = ''
}

async function confirmRemove() {
  const id = confirmFriendshipId.value
  cancelRemove()
  try {
    const res = await secureFetch(`/api/friends/${id}`, { method: 'DELETE' })
    if (!res.ok) throw new Error()
    friends.value = friends.value.filter(f => f.friendshipId !== id)
    if (friendModal.value) friendModal.value = null
  } catch { /* show toast */ }
}

// --- Friend profile modal ---
async function openFriendProfile(friendUser) {
  friendModal.value = { profile: null, events: null, loading: true, error: null, user: friendUser }
  try {
    const [profileRes, eventsRes] = await Promise.all([
      secureFetch(`/api/user/${friendUser.id}/profile`),
      secureFetch(`/api/user/${friendUser.id}/events`)
    ])
    if (!profileRes.ok) throw new Error('Could not load profile')
    if (!eventsRes.ok) throw new Error('Could not load events')
    friendModal.value = {
      user: friendUser,
      profile: await profileRes.json(),
      events: await eventsRes.json(),
      loading: false,
      error: null
    }
  } catch (e) {
    friendModal.value = { ...friendModal.value, loading: false, error: e.message }
  }
}

function closeFriendModal() {
  friendModal.value = null
}

// --- Add Friend's Event to User Profile ---
async function saveFriendEvent(event) {
  if (!authState.authenticated) return
  savingIds[event.id] = true
  delete saveErrors[event.id]

  try {
    const response = await secureFetch('/api/user/events', {
      method: 'POST',
      body: JSON.stringify(event)
    })
    if (!response.ok) {
      const text = await response.text().catch(() => '')
      throw new Error(`${response.status}${text ? ': ' + text : ''}`)
    }
    // Optionally trigger a local state modification or alert
    alert(`"${event.title}" saved successfully to your profile!`)
  } catch (e) {
    console.error('Failed to save event:', e)
    saveErrors[event.id] = e.message || 'Save failed'
  } finally {
    delete savingIds[event.id]
  }
}

// Derive the friend's user from the DTO
function getFriendUser(friendship) {
  return friendship.requester.id === user.value.id
    ? friendship.addressee
    : friendship.requester
}

const pendingCount = computed(() => pendingRequests.value.length)

onMounted(() => {
  loadProfile()
  loadFriends()
  loadPending()
})
</script>

<template>
  <main class="profile-layout">
    <div class="profile-container">

      <header class="profile-header">
        <p class="profile-tag">User Profile</p>
        <h1>{{ user.username || '—' }}</h1>
        <p class="profile-subtext">Manage your account and social connections.</p>
      </header>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
      <div v-if="loading" class="loading-state">Loading profile…</div>

      <div v-else class="profile-body">

        <section class="profile-card info-card">
          <div class="card-header">
            <h2>Personal Info</h2>
          </div>

          <ul class="info-list">
            <li class="info-item">
              <label>User ID</label>
              <p><span class="id-value">{{ user.id }}</span></p>
            </li>
            <li class="info-item">
              <label>Username</label>
              <p>{{ user.username }}</p>
            </li>
          </ul>

          <div class="card-actions">
            <button class="edit-btn" disabled title="Coming soon">Edit Profile</button>
            <div class="social-btns">
              <button class="friends-btn" @click="openFriendsPanel('friends')">
                Friends <span class="btn-count">{{ friends.length }}</span>
              </button>
              <button
                class="friends-btn"
                :class="{ 'has-pending': pendingCount > 0 }"
                @click="openFriendsPanel('pending')"
              >
                Requests
                <span class="btn-count" :class="{ highlight: pendingCount > 0 }">{{ pendingCount }}</span>
              </button>
            </div>
          </div>
        </section>

        <Transition name="drawer">
          <section v-if="showFriendsPanel" class="profile-card friends-card">

            <div class="card-header">
              <div class="tab-row">
                <div class="info-item">
                  <h2>Friends</h2>
                </div>
                <button class="tab-btn add-tab" :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">
                  + Add
                </button>
              </div>
              <button class="close-btn" @click="showFriendsPanel = false" title="Close">✕</button>
            </div>

            <div v-if="activeTab === 'friends'">
              <p class="list-heading">{{ friends.length }} friend{{ friends.length !== 1 ? 's' : '' }}</p>
              <ul class="social-list">
                <li v-if="friends.length === 0" class="empty-state">No friends yet — add one.</li>
                <li
                  v-for="f in friends"
                  :key="f.friendshipId"
                  class="social-item clickable"
                  @click="openFriendProfile(getFriendUser(f))"
                >
                  <div class="social-avatar">{{ getFriendUser(f).username.charAt(0).toUpperCase() }}</div>
                  <div class="social-info">
                    <span class="social-name">{{ getFriendUser(f).username }}</span>
                    <span class="social-id">#{{ getFriendUser(f).id }}</span>
                  </div>
                  <button
                    class="remove-btn"
                    @click.stop="askRemoveFriend(f.friendshipId, getFriendUser(f).username)"
                    title="Remove friend"
                  >✕</button>
                </li>
              </ul>
            </div>

            <div v-if="activeTab === 'pending'">
              <p class="list-heading">{{ pendingCount }} pending request{{ pendingCount !== 1 ? 's' : '' }}</p>
              <ul class="social-list">
                <li v-if="pendingRequests.length === 0" class="empty-state">No pending requests.</li>
                <li v-for="r in pendingRequests" :key="r.friendshipId" class="social-item">
                  <div class="social-avatar">{{ r.requester.username.charAt(0).toUpperCase() }}</div>
                  <div class="social-info">
                    <span class="social-name">{{ r.requester.username }}</span>
                    <span class="social-id">#{{ r.requester.id }}</span>
                  </div>
                  <div class="respond-btns">
                    <button class="accept-btn" @click="respondToRequest(r.friendshipId, true)">✓</button>
                    <button class="decline-btn" @click="respondToRequest(r.friendshipId, false)">✕</button>
                  </div>
                </li>
              </ul>
            </div>

            <div v-if="activeTab === 'add'" class="add-friend-section">
              <p class="section-hint">Enter a user ID to send a friend request.</p>
              <div class="add-friend-row">
                <input
                  v-model="addFriendId"
                  type="number"
                  placeholder="User ID"
                  class="friend-input"
                  @keyup.enter="sendFriendRequest"
                />
                <button class="send-btn" :disabled="addFriendLoading" @click="sendFriendRequest">
                  {{ addFriendLoading ? '…' : 'Send' }}
                </button>
              </div>
              <p v-if="addFriendError" class="inline-error">{{ addFriendError }}</p>
              <p v-if="addFriendSuccess" class="inline-success">{{ addFriendSuccess }}</p>
            </div>

          </section>
        </Transition>
      </div>
    </div>

    <Transition name="fade">
      <div v-if="confirmFriendshipId" class="modal-backdrop" @click.self="cancelRemove">
        <div class="modal-box">
          <h3>Remove friend?</h3>
          <p>Are you sure you want to remove <strong>{{ confirmUsername }}</strong> from your friends?</p>
          <div class="modal-actions">
            <button class="modal-cancel" @click="cancelRemove">Cancel</button>
            <button class="modal-confirm" @click="confirmRemove">Remove</button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="friendModal" class="modal-backdrop" @click.self="closeFriendModal">
        <div class="modal-box friend-modal">

          <button class="modal-close" @click="closeFriendModal">✕</button>

          <div v-if="friendModal.loading" class="modal-loading">Loading…</div>
          <div v-else-if="friendModal.error" class="modal-error">{{ friendModal.error }}</div>

          <template v-else>
            <div class="friend-modal-header">
              <div class="friend-modal-avatar">
                {{ friendModal.user.username.charAt(0).toUpperCase() }}
              </div>
              <div>
                <h3>{{ friendModal.profile?.username }}</h3>
                <span class="social-id">#{{ friendModal.profile?.id }}</span>
              </div>
            </div>

            <div class="friend-events-section">
              <p class="list-heading">Saved Events</p>
              <ul v-if="friendModal.events?.length" class="friend-event-list">
                <li v-for="e in friendModal.events" :key="e.id" class="friend-event-item">

                  <div class="friend-event-layout">
                    <img v-if="e.imageUrl" :src="e.imageUrl" :alt="e.title" class="friend-event-thumb" />
                    <div v-else class="friend-event-thumb placeholder" />

                    <div class="friend-event-details">
                      <span class="friend-event-title">{{ e.title }}</span>
                      <span class="friend-event-meta">{{ e.venue }} · {{ e.city }}</span>
                      <span class="friend-event-date">{{ e.date }}<template v-if="e.time"> · {{ e.time }}</template></span>
                    </div>

                    <div class="friend-event-action">
                      <button
                        type="button"
                        class="compact-add-btn"
                        title="Copy to my profile"
                        :disabled="savingIds[e.id]"
                        @click="saveFriendEvent(e)"
                      >
                        {{ savingIds[e.id] ? '…' : '+' }}
                      </button>
                    </div>
                  </div>

                  <p v-if="saveErrors[e.id]" class="friend-event-error">{{ saveErrors[e.id] }}</p>
                </li>
              </ul>
              <p v-else class="empty-state">No saved events.</p>
            </div>
          </template>

        </div>
      </div>
    </Transition>

  </main>
</template>

<style scoped>
.profile-layout {
  min-height: 100vh;
  background: var(--bg-app);
  padding: 2rem;
}

.profile-container {
  max-width: 860px;
  margin: 0 auto;
}

/* Header */
.profile-header { margin-bottom: 3rem; }

.profile-tag {
  color: var(--accent-blue);
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  font-size: 0.85rem;
}

h1 {
  color: var(--text-1);
  font-size: clamp(2.5rem, 4vw, 4rem);
  letter-spacing: -0.04em;
  font-weight: 800;
  margin-bottom: 1rem;
}

.profile-subtext { color: var(--text-3); font-size: 1.1rem; }

.profile-body { display: flex; flex-direction: column; gap: 1.25rem; }

/* Cards */
.profile-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 2rem;
  box-shadow: var(--shadow-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.75rem;
}

h2 { font-size: 1.25rem; font-weight: 700; color: var(--text-1); margin: 0; }

/* Info list */
.info-list { list-style: none; padding: 0; margin: 0; display: grid; gap: 1.5rem; }

.info-item label {
  display: block;
  color: var(--text-3);
  font-size: 0.8rem;
  margin-bottom: 0.3rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.info-item p { color: var(--text-1); font-size: 1.05rem; font-weight: 500; margin: 0; }

.id-value {
  font-family: monospace;
  background: var(--bg-surface-2);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.95rem;
}

.card-actions {
  margin-top: 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.social-btns { display: flex; gap: 0.5rem; }

/* Base button */
button {
  cursor: pointer;
  border: none;
  border-radius: var(--radius-lg);
  font-weight: 600;
  transition: opacity 0.15s, transform 0.1s;
}

button:active { transform: scale(0.97); }
button:disabled { opacity: 0.45; cursor: not-allowed; }

.edit-btn {
  padding: 0.65rem 1.25rem;
  background: var(--bg-surface-2);
  color: var(--text-1);
  border: 1px solid var(--border);
  font-size: 0.9rem;
}

.friends-btn {
  padding: 0.6rem 1rem;
  background: var(--bg-surface-2);
  color: var(--text-1);
  border: 1px solid var(--border);
  font-size: 0.88rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: background 0.15s, border-color 0.15s;
}

.friends-btn:hover { background: var(--accent-blue); color: #fff; border-color: var(--accent-blue); }
.friends-btn.has-pending { border-color: var(--accent-pink); color: var(--accent-pink); }
.friends-btn.has-pending:hover { background: var(--accent-pink); color: #fff; }

.btn-count {
  background: rgba(0,0,0,0.12);
  border-radius: 20px;
  padding: 0.05rem 0.45rem;
  font-size: 0.78rem;
  font-weight: 700;
}

.btn-count.highlight { background: var(--accent-pink); color: #fff; }

/* Friends panel */
.friends-card { padding: 1.5rem 2rem; }

.close-btn {
  padding: 0.3rem 0.65rem;
  background: transparent;
  color: var(--text-3);
  border: 1px solid var(--border);
  font-size: 0.85rem;
  flex-shrink: 0;
}

.close-btn:hover { color: var(--text-1); border-color: var(--text-1); }

.tab-row { display: flex; gap: 0.4rem; flex: 1; }

.tab-btn {
  padding: 0.45rem 0.85rem;
  background: var(--bg-surface-2);
  color: var(--text-3);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: background 0.15s, color 0.15s;
}

.tab-btn.active { background: var(--accent-blue); color: #fff; border-color: var(--accent-blue); }
.add-tab { margin-left: auto; }

/* List heading */
.list-heading {
  color: var(--text-3);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 700;
  margin-bottom: 0.75rem;
}

/* Social list */
.social-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 280px;
  overflow-y: auto;
}

.social-item {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.6rem 0.75rem;
  border-radius: var(--radius-lg);
  background: var(--bg-surface-2);
  border: 1px solid var(--border);
  transition: border-color 0.15s;
}

.social-item.clickable { cursor: pointer; }
.social-item.clickable:hover { border-color: var(--accent-blue); }

.social-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--accent-blue);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.social-info { flex: 1; display: flex; flex-direction: column; gap: 0.1rem; }
.social-name { color: var(--text-1); font-weight: 600; font-size: 0.92rem; }
.social-id { color: var(--text-3); font-size: 0.78rem; font-family: monospace; }

.remove-btn {
  padding: 0.25rem 0.55rem;
  background: transparent;
  color: var(--text-3);
  font-size: 0.82rem;
  border: 1px solid transparent;
}

.remove-btn:hover { background: var(--accent-pink-soft); color: var(--accent-pink); border-color: var(--accent-pink); }

.respond-btns { display: flex; gap: 0.4rem; }

.accept-btn {
  padding: 0.25rem 0.55rem;
  background: transparent;
  color: var(--accent-cyan);
  border: 1px solid var(--accent-cyan);
  border-radius: var(--radius-lg);
  font-size: 0.88rem;
}

.accept-btn:hover { background: var(--accent-cyan); color: #fff; }

.decline-btn {
  padding: 0.25rem 0.55rem;
  background: transparent;
  color: var(--accent-pink);
  border: 1px solid var(--accent-pink);
  border-radius: var(--radius-lg);
  font-size: 0.88rem;
}

.decline-btn:hover { background: var(--accent-pink); color: #fff; }

.empty-state { color: var(--text-3); font-size: 0.88rem; padding: 1rem 0; text-align: center; }

/* Add friend */
.add-friend-section { padding-top: 0.25rem; }
.section-hint { color: var(--text-3); font-size: 0.88rem; margin-bottom: 0.85rem; }
.add-friend-row { display: flex; gap: 0.6rem; }

.friend-input {
  flex: 1;
  padding: 0.65rem 1rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  background: var(--bg-surface-2);
  color: var(--text-1);
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.15s;
}

.friend-input:focus { border-color: var(--accent-blue); }
.friend-input::-webkit-outer-spin-button,
.friend-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  appearance: none;
  margin: 0;
}

.friend-input[type=number] {
  -moz-appearance: textfield;
  appearance: textfield;
}

.send-btn { padding: 0.65rem 1.25rem; background: var(--accent-blue); color: #fff; font-size: 0.95rem; white-space: nowrap; }
.send-btn:hover:not(:disabled) { opacity: 0.85; }

.inline-error  { color: var(--accent-pink);  font-size: 0.85rem; margin-top: 0.5rem; }
.inline-success { color: var(--accent-cyan); font-size: 0.85rem; margin-top: 0.5rem; }

/* Error / loading */
.error-banner {
  background: var(--accent-pink-soft);
  color: var(--accent-pink);
  border: 1px solid var(--accent-pink);
  border-radius: var(--radius-lg);
  padding: 1rem 1.25rem;
  margin-bottom: 2rem;
  font-weight: 600;
}

.loading-state { color: var(--text-3); padding: 2rem 0; }

/* Modals */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}

.modal-box {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 2rem;
  width: 100%;
  max-width: 420px;
  position: relative;
  box-shadow: 0 24px 64px rgba(0,0,0,0.4);
}

.modal-box h3 { font-size: 1.2rem; font-weight: 700; color: var(--text-1); margin-bottom: 0.75rem; }
.modal-box p  { color: var(--text-3); font-size: 0.95rem; margin-bottom: 1.5rem; }
.modal-box p strong { color: var(--text-1); }

.modal-actions { display: flex; gap: 0.75rem; justify-content: flex-end; }

.modal-cancel {
  padding: 0.6rem 1.25rem;
  background: var(--bg-surface-2);
  color: var(--text-1);
  border: 1px solid var(--border);
  font-size: 0.9rem;
}

.modal-confirm {
  padding: 0.6rem 1.25rem;
  background: var(--accent-pink);
  color: #fff;
  font-size: 0.9rem;
}

.modal-confirm:hover { opacity: 0.85; }

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.25rem 0.6rem;
  background: transparent;
  color: var(--text-3);
  border: 1px solid var(--border);
  font-size: 0.85rem;
}

.modal-close:hover { color: var(--text-1); }

/* Friend profile modal */
.friend-modal { max-width: 520px; }

.friend-modal-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-right: 2rem;
}

.friend-modal-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--accent-blue);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.friend-modal-header h3 { font-size: 1.15rem; font-weight: 700; color: var(--text-1); margin: 0 0 0.15rem; }

.friend-events-section { margin-top: 1rem; }

/* Enhanced Friend Event Layout list classes */
.friend-event-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  max-height: 260px;
  overflow-y: auto;
}

.friend-event-item {
  display: flex;
  flex-direction: column;
  background: var(--bg-surface-2);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 0.65rem 0.85rem;
}

.friend-event-layout {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  width: 100%;
}

.friend-event-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.friend-event-thumb.placeholder {
  background: var(--bg-surface);
  border: 1px solid var(--border);
}

.friend-event-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  min-width: 0;
}

.friend-event-title {
  color: var(--text-1);
  font-size: 0.92rem;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-event-meta,
.friend-event-date {
  color: var(--text-3);
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-event-action {
  flex-shrink: 0;
}

.compact-add-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-surface);
  border: 1px solid rgba(225, 29, 141, 0.4);
  color: var(--text-1);
  border-radius: 50%;
  font-size: 1.1rem;
  font-weight: 700;
  transition: all 0.15s ease;
}

.compact-add-btn:hover:not(:disabled) {
  background: var(--accent-pink-soft);
  border-color: rgba(225, 29, 141, 0.8);
  transform: scale(1.05);
}

.friend-event-error {
  margin: 0.35rem 0 0;
  font-size: 0.78rem;
  color: #f87171;
}

.modal-loading { color: var(--text-3); padding: 2rem 0; text-align: center; }
.modal-error   { color: var(--accent-pink); padding: 1rem 0; }

/* Transitions */
.drawer-enter-active { transition: all 0.2s ease; }
.drawer-leave-active { transition: all 0.15s ease; }
.drawer-enter-from, .drawer-leave-to { opacity: 0; transform: translateY(-8px); }

.fade-enter-active { transition: opacity 0.18s ease; }
.fade-leave-active { transition: opacity 0.12s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 600px) {
  .card-actions { flex-direction: column; align-items: stretch; }
  .social-btns  { justify-content: stretch; }
  .friends-btn  { flex: 1; justify-content: center; }
}
</style>
