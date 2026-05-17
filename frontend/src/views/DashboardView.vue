<template>
  <div class="container pt-5 mt-5">
    <h1 class="mb-5">
      Tere tulemast, {{ firstName }} <span v-if="middleName">{{ middleName }}&nbsp;</span
      >{{ lastName }}!
    </h1>
    <div class="row mt-3 align-items-stretch">
      <div class="col-6">
        <div class="card h-100" style="background-color: #e8eef4">
          <div class="card-body text-center">
            <p>Edasimüüjaid süsteemis</p>
            <h2 class="text-primary">{{ dashboard.sellerCount }}</h2>
          </div>
        </div>
      </div>
      <div class="col-6">
        <div class="card h-100" style="background-color: #e8eef4">
          <div class="card-body text-center">
            <p>Viimane import</p>
            <h2 class="text-primary">{{ dashboard.lastImport }}</h2>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import DashboardService from '@/api-services/DashboardService.js'

export default {
  name: 'DashboardView',
  data() {
    return {
      firstName: AuthService.getFirstName(),
      middleName: AuthService.getMiddleName(),
      lastName: AuthService.getLastName(),
      dashboard: {
        sellerCount: 0,
        lastImport: null,
      },
    }
  },
  methods: {
    getDashboard() {
      DashboardService.sendGetDashboard()
        .then((response) => {
          this.dashboard = response.data
        })
        .catch((error) => {
          console.log(error)
        })
    },
  },
  beforeMount() {
    this.getDashboard()
  },
}
</script>
