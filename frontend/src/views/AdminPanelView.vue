<template>
  <main class="placeholder-view">
    <section class="placeholder-card">
      <p class="placeholder-tag">ADMIN PANEL</p>
      <h1>Administrative tasks</h1>
      <p>
        Placeholder view. Later, administrative tools can be placed here.
      </p>

      <div class="admin-grid">
        <div class="admin-box">User management</div>
        <div class="admin-box">Event management</div>
        <div class="admin-box">API settings</div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.placeholder-view {
  min-height: 100vh;
  padding: 2rem;
  background: var(--bg-app);
}

.placeholder-card {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
  border-radius: var(--radius-xl);
  background: var(--bg-surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-soft);
}

.placeholder-tag {
  margin: 0 0 0.75rem;
  color: var(--accent-pink);
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

.admin-grid {
  margin-top: 1.5rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.admin-box {
  padding: 1.25rem;
  border-radius: var(--radius-lg);
  background: var(--bg-surface-2);
  border: 1px solid var(--border);
  color: var(--text-1);
  font-weight: 700;
}
</style>