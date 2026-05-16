
export default {
  isLoggedIn() {
    return localStorage.getItem('userId') !== null
  },
  getUserId() {
    return localStorage.getItem('userId')
  },
  getFirstName() {
    return localStorage.getItem('firstName')
  },
  getMiddleName() {
    return localStorage.getItem('middleName')
  },
  getLastName() {
    return localStorage.getItem('lastName')
  },
  getRole() {
    return localStorage.getItem('role')
  },
  saveUserInfo(data) {
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('firstName', data.firstName)
    localStorage.setItem('middleName', data.middleName)
    localStorage.setItem('lastName', data.lastName)
    localStorage.setItem('role', data.role)
  },
  logOut() {
    localStorage.clear()
  },
}
