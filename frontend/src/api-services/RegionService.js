import axios from 'axios'

export default {
  sendGetRegions() {
    return axios.get('/api/region')
  },
}
