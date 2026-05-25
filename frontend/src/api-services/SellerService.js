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
  sendPutSeller(sellerId, userId, sellerData) {
    return axios.put('/api/seller/' + sellerId + '?userId=' + userId, sellerData)
  },
  sendPutSellerStatus(sellerId, userId, statusData) {
    return axios.put('/api/seller/' + sellerId + '/status?userId=' + userId, statusData)
  },
  sendDeleteSellerContact(sellerId, contactId, userId) {
    return axios.delete('/api/seller/' + sellerId + '/contacts/' + contactId + '?userId=' + userId)
  },
  sendPostSellerContact(sellerId, userId, contactData) {
    return axios.post('/api/seller/' + sellerId + '/contacts?userId=' + userId, contactData)
  },

  sendPostSellerRegion(sellerId, userId, regionData) {
    return axios.post('/api/seller/' + sellerId + '/regions?userId=' + userId, regionData)
  },

  sendPutSellerRegion(sellerId, regionId, userId, regionData) {
    return axios.put(
      '/api/seller/' + sellerId + '/regions/' + regionId + '?userId=' + userId,
      regionData,
    )
  },

  sendDeleteSellerRegion(sellerId, regionId, userId) {
    return axios.delete('/api/seller/' + sellerId + '/regions/' + regionId + '?userId=' + userId)
  },

  sendPostSellerCommissionRate(sellerId, userId, commissionData) {
    return axios.post(
      '/api/seller/' + sellerId + '/commission-rates?userId=' + userId,
      commissionData,
    )
  },

  sendPutSellerCommissionRate(sellerId, commissionRateId, userId, commissionData) {
    return axios.put(
      '/api/seller/' + sellerId + '/commission-rates/' + commissionRateId + '?userId=' + userId,
      commissionData,
    )
  },

  sendDeleteSellerCommissionRate(sellerId, commissionRateId, userId) {
    return axios.delete(
      '/api/seller/' + sellerId + '/commission-rates/' + commissionRateId + '?userId=' + userId,
    )
  },
}
