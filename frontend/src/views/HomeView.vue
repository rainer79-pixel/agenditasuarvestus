<template>
  <div class="d-flex flex-column min-vh-100">
    <header class="text-start py-3" style="background-color: #e8eef4">
      <h1>Edasimüüja Teenustasu Arvutussüsteem (ETAS)</h1>
    </header>
    <main
      class="flex-grow-1 d-flex flex-column align-items-center justify-content-start pt-4"
      style="background-color: #f4f7fa"
    >
      <img
        src="@/assets/teenustasu_kalkulaator_logo.svg"
        alt="ETAS logo"
        class="mb-5"
        style="width: 1200px; margin-left: 200px"
      />
      <p class="fs-5">
        Sisekasutuseks mõeldud tööriist edasimüüjate teenustasude arvutamiseks ja arvete
        kontrollimiseks.
      </p>
      <button class="btn btn-primary mt-3" @click="navigateToLogin">Logi sisse</button>
    </main>

    <footer class="text-center py-2" style="background-color: #e8eef4">
      <p>© 2026 ETAS</p>
    </footer>
  </div>
</template>

<script>
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'HomeView',
  methods: {
    navigateToLogin() {
      NavigationService.navigateToLoginView()
    },
  },
}
</script>
