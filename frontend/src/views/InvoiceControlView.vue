<template>
  <div class="container pt-5 text-center">
    <div class="mt-5">
      <h2 class="text-muted">🚧 Arenduses</h2>
      <p class="text-muted mt-3">See vaade on hetkel arendamisel.</p>
    </div>
    <div class="mt-4">
      <img :src="invoiceImg" alt="Arvete kontroll" class="rounded shadow-sm" style="width: 55%" />
    </div>
  </div>
</template>

<script>
import invoiceImg from '@/views/demoviews/InvoiceControllView.jpg'

export default {
  name: 'InvoiceControlView',
  data() {
    return {
      invoiceImg,
    }
  },
}
</script>