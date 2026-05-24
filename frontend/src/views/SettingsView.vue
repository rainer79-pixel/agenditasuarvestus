<template>
  <div class="container pt-5 text-center">
    <div class="mt-5">
      <h2 class="text-muted">🚧 Arenduses</h2>
      <p class="text-muted mt-3">See vaade on hetkel arendamisel.</p>
    </div>
    <div class="mt-4 d-flex flex-row gap-4 align-items-start justify-content-center">
      <img :src="settingsImg" alt="Seadistused" class="rounded shadow-sm" style="width: 55%" />
      <img :src="settingsModalImg" alt="Kasutaja modal" class="rounded shadow-sm" style="width: 15%" />
    </div>
  </div>
</template>

<script>
import settingsImg from '@/views/demoviews/SettingsView.jpg'
import settingsModalImg from '@/views/demoviews/SettingsUserModal.jpg'

export default {
  name: 'SettingsView',
  data() {
    return {
      settingsImg,
      settingsModalImg,
    }
  },
}
</script>