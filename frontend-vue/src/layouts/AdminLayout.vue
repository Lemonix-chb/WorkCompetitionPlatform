<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-container">
      <div class="content-wrapper fade-in">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-container {
  flex: 1;
  margin-left: var(--sidebar-width);
  background: var(--color-bg-main);
  transition: margin-left var(--transition-base);
  min-height: 100vh;
}

@media (max-width: 1024px) {
  .main-container {
    margin-left: var(--sidebar-collapsed-width);
  }
}

@media (max-width: 768px) {
  .main-container {
    margin-left: 0;
  }
}

.content-wrapper {
  min-height: 100vh;
  padding: 0;
}
</style>