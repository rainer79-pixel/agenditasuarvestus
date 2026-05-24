import axios from 'axios'

export default {
  sendPostImportReport(userId, file) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/api/import/user/' + userId, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  sendDeleteImportReport(userId, period) {
    return axios.delete('/api/import/user/' + userId + '/' + period)
  },
}
