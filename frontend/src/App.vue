<template>
  <nav v-if="showNavbar" class="navbar navbar-dark bg-dark px-3 d-flex justify-content-between">
    <div class="d-flex gap-4">
      <router-link class="nav-link text-white" :to="{ name: 'dashboardRoute' }"
        >Töölaud</router-link
      >
      <router-link class="nav-link text-white" :to="{ name: 'sellersRoute' }"
        >Edasimüüjad</router-link
      >
      <router-link class="nav-link text-white" :to="{ name: 'reportsRoute' }">Aruanded</router-link>
      <router-link class="nav-link text-white" :to="{ name: 'invoiceControlRoute' }"
        >Arvete kontroll</router-link
      >
      <router-link class="nav-link text-white" v-if="isAdmin" :to="{ name: 'settingsRoute' }"
        >Seaded</router-link
      >
    </div>
    <div class="d-flex align-items-center gap-5">
      <span class="text-white"
        >{{ firstName }} <span v-if="middleName">{{ middleName }}&nbsp;</span>{{ lastName }}</span
      >
      <button class="btn btn-danger" @click="logOut">Logi välja</button>
    </div>
  </nav>
  <RouterView />
</template>

<script>
import { RouterView } from 'vue-router'
import AuthService from '@/auth/AuthService.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'App',
  components: { RouterView },
  data() {
    return {
      isLoggedIn: AuthService.isLoggedIn(),
      isAdmin: AuthService.getRole() === 'A',
      firstName: AuthService.getFirstName(),
      middleName: AuthService.getMiddleName(),
      lastName: AuthService.getLastName(),
    }
  },
  computed: {
    showNavbar() {
      return (
        this.isLoggedIn && this.$route.name !== 'loginRoute' && this.$route.name !== 'homeRoute'
      )
    },
  },
  methods: {
    logOut() {
      AuthService.logOut()
      this.isLoggedIn = false
      NavigationService.navigateToHomeView()
    },
  },
}
</script>

<style>
body {
  background-color: #fafaf8 !important;
}

.router-link-exact-active {
  border-bottom: 2px solid white;
}
</style>
