<template>
  <div class="modal d-block">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Lisa kontakt</h5>
          <button type="button" class="btn-close" @click="$emit('event-modal-closed')"></button>
        </div>

        <div class="modal-body">
          <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <div class="mb-3">
            <label class="form-label">Eesnimi</label>
            <input type="text" class="form-control" v-model="firstName" />
          </div>
          <div class="mb-3">
            <label class="form-label">Teine nimi</label>
            <input type="text" class="form-control" v-model="middleName" />
          </div>
          <div class="mb-3">
            <label class="form-label">Perekonnanimi</label>
            <input type="text" class="form-control" v-model="lastName" />
          </div>
          <div class="mb-3">
            <label class="form-label">Telefon</label>
            <input type="text" class="form-control" v-model="phone" />
          </div>
          <div class="mb-3">
            <label class="form-label">E-mail</label>
            <input type="text" class="form-control" v-model="email" />
          </div>

          <div class="mb-3">
            <label class="form-label">Rollid</label>
            <div v-for="role in availableRoles" :key="role.code" class="form-check">
              <input
                type="checkbox"
                class="form-check-input"
                :id="'role' + role.code"
                :value="role.code"
                v-model="roles"
              />
              <label class="form-check-label" :for="'role' + role.code">
                {{ role.code }} — {{ role.label }}
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="$emit('event-modal-closed')">Tühista</button>
          <button class="btn btn-success" @click="saveContact()">Salvesta</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>

<script>
import AuthService from '@/auth/AuthService.js'
import SellerService from '@/api-services/SellerService.js'

export default {
  name: 'SellerSettingsContactModal',
  props: {
    sellerId: Number,
  },
  emits: ['event-modal-closed', 'event-contact-saved'],
  data() {
    return {
      firstName: '',
      middleName: '',
      lastName: '',
      phone: '',
      email: '',
      roles: [],
      availableRoles: [
        { code: 'L', label: 'Lepinguline kontakt' },
        { code: 'A', label: 'Aruannete kontakt' },
        { code: 'R', label: 'Raamatupidamise kontakt' },
        { code: 'T', label: 'Tehniline kontakt' },
      ],
      errorMessage: '',
    }
  },
  methods: {
    saveContact() {
      const userId = AuthService.getUserId()
      const contactData = {
        firstName: this.firstName,
        middleName: this.middleName,
        lastName: this.lastName,
        phone: this.phone,
        email: this.email,
        roles: this.roles,
      }
      SellerService.sendPostSellerContact(this.sellerId, userId, contactData)
        .then(() => this.$emit('event-contact-saved'))
        .catch((error) => {
          this.errorMessage = error.response.data.message
        })
    },
  },
}
</script>
