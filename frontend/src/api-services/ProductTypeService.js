import axios from 'axios'

export default {
  sendGetProductTypes() {
    return axios.get('/api/product-type')
  },
}
