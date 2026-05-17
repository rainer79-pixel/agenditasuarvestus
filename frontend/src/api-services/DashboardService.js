import axios from 'axios'

export default {
  sendGetDashboard() {
    return axios.get('/api/dashboard')
  },
}
