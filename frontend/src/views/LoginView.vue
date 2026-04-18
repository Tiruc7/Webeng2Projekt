<template>
  <main class="placeholder-view">
    <section class="placeholder-card">
      <p class="placeholder-tag">LOGIN</p>
      <h1>Login</h1>
      <p>
        Placeholder view. Later, the real login form will be placed here.
      </p>

      <form class="login-form">
        <input type="text" placeholder="Username" disabled />
        <input type="password" placeholder="Password" disabled />
        <button type="button" disabled>Login</button>
      </form>
    </section>
  </main>
</template>

<style scoped>
.placeholder-view {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem;
  background: var(--bg-app);
}

.placeholder-card {
  width: min(520px, 100%);
  padding: 2rem;
  border-radius: var(--radius-xl);
  background: var(--bg-surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-soft);
}

.placeholder-tag {
  margin: 0 0 0.75rem;
  color: var(--accent-blue);
  font-weight: 800;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0 0 1rem;
  color: var(--text-1);
  font-size: 3rem;
  letter-spacing: -0.04em;
}

p {
  color: var(--text-3);
}

.login-form {
  margin-top: 1.5rem;
  display: grid;
  gap: 1rem;
}

input,
button {
  width: 100%;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  padding: 1rem;
  background: var(--bg-surface-2);
  color: var(--text-1);
}

button {
  background: linear-gradient(135deg, var(--accent-pink), var(--accent-blue));
  font-weight: 800;
  cursor: not-allowed;
  opacity: 0.75;
}
</style>