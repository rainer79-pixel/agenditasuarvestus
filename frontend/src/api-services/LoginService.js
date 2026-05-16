import axios from 'axios'

export default {
  sendPostLogin(loginData) {
    return axios.post('/api/login', loginData)
  },
}
