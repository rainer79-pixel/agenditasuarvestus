import axios from 'axios'

export default {
  sendGetSellers(userId) {
    return axios.get('/api/seller/user/' + userId)
  },
  sendGetSeller(sellerId) {
    return axios.get('/api/seller/' + sellerId)
  },
  sendGetSellerContacts(sellerId) {
    return axios.get('/api/seller/' + sellerId + '/contacts')
  },
  sendGetSellerRegions(sellerId) {
    return axios.get('/api/seller/' + sellerId + '/regions')
  },
  sendGetSellerCommissionRates(sellerId) {
    return axios.get('/api/seller/' + sellerId + '/commission-rates')
  },
  sendPostSeller(userId, sellerData) {
    return axios.post('/api/seller/user/' + userId, sellerData)
  },
  sendPutSeller(sellerId, sellerData) {
    return axios.put('/api/seller/' + sellerId, sellerData)
  },
  sendPutSellerStatus(sellerId, statusData) {
    return axios.put('/api/seller/' + sellerId + '/status', statusData)
  },
  sendDeleteSellerContact(sellerId, contactId, userId) {
    return axios.delete('/api/seller/' + sellerId + '/contacts/' + contactId + '?userId=' + userId)
  },
  sendPostSellerContact(sellerId, userId, contactData) {
    return axios.post('/api/seller/' + sellerId + '/contacts?userId=' + userId, contactData)
  },
}
