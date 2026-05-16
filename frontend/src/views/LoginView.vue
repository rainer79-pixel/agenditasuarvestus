<template>
  <div class="d-flex justify-content-center align-items-center min-vh-100">
    <div class="card p-5 position-relative" style="width: 400px; background-color: #e8eef4">
      <div class="d-flex justify-content-end">
        <button
          class="btn-close position-absolute top-0 end-0 m-2"
          @click="navigateToHome"
        ></button>
      </div>
      <h2 class="text-center">ETAS</h2>
      <p class="text-danger text-center fs-5 fw-bold" v-if="errorMessage">{{ errorMessage }}</p>
      <p class="text-center text-muted">Palun logige sisse</p>
      <div class="mb-3">
        <label>E-mail</label>
        <input
          type="text"
          class="form-control"
          placeholder="Sisestage e-mail"
          v-model="loginData.email"
          style="background-color: #ffffff"
        />
      </div>
      <div class="mb-3">
        <label>Parool</label>
        <input
          type="password"
          class="form-control"
          placeholder="Sisestage parool"
          autocomplete="off"
          v-model="loginData.password"
          style="background-color: #ffffff"
        />
      </div>
      <button class="btn btn-primary w-100 mt-2" @click="login" :disabled="showSpinner">
        <span v-if="showSpinner" class="spinner-border spinner-border-sm me-2"></span>
        Logi sisse
      </button>
      <p class="text-center text-muted mt-3">Edasimüüja Teenustasu Arvutussüsteem</p>
    </div>
  </div>
</template>

<script>
import NavigationService from '@/navigation/NavigationService.js'
import AuthService from '@/auth/AuthService.js'
import LoginService from '@/api-services/LoginService.js'

export default {
  name: 'LoginView',
  data() {
    return {
      loginData: {
        email: '',
        password: '',
      },
      errorMessage: '',
      showSpinner: false,
    }
  },
  methods: {
    login() {
      if (!this.loginData.email || !this.loginData.password) {
        this.errorMessage = 'Palun täitke kõik väljad'
        return
      }
      this.showSpinner = true
      LoginService.sendPostLogin(this.loginData)
        .then((response) => this.handleLoginResponse(response.data))
        .catch((error) => this.handleLoginError(error))
        .finally(() => {
          this.showSpinner = false
        })
    },

    handleLoginResponse(data) {
      AuthService.saveUserInfo(data)
      NavigationService.navigateToDashboardView()
    },

    handleLoginError(error) {
      this.errorMessage = error.response.data.message
    },

    navigateToHome() {
      NavigationService.navigateToHomeView()
    },
  },
}
</script>
