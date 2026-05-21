# SellerSettingsView — raamistik + kontaktid

**Vaade:** `SellerSettingsView.vue`
**Tüüp:** Frontend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` on edasimüüja haldusvaade kus Admin saab muuta kontakte, piirkondi ja teenustasusid.
See task loob vaate raamistiku (route, navigatsioon, üldandmete kuvamine) ja rakendab **kontaktide sektsiooni** täielikult.
Tiimikaaslane teeb paralleelselt piirkondade ja teenustasude sektsioonid eraldi failidena — lõpus liidetakse.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

---

## Kaasatud failid

| Fail | Tegevus |
|------|---------|
| `frontend/src/views/SellerSettingsView.vue` | LOO — peamine vaade |
| `frontend/src/components/modals/SellerSettingsContactModal.vue` | LOO — kontakti lisamise modal |
| `frontend/src/api-services/SellerService.js` | UUENDA — lisa kontaktide POST ja DELETE meetodid |
| `frontend/src/navigation/NavigationService.js` | UUENDA — lisa `navigateToSellerSettingsView(sellerId)` |
| `frontend/src/router/index.js` | UUENDA — lisa `sellerSettingsRoute` |

---

## API endpointid

| Meetod | URL | Kirjeldus |
|--------|-----|-----------|
| GET | `/api/seller/{sellerId}` | Üldandmed (companyName, orgId, status, contractStart jne) |
| GET | `/api/seller/{sellerId}/contacts` | Kontaktide nimekiri |
| POST | `/api/seller/{sellerId}/contacts?userId=` | Lisa uus kontakt |
| DELETE | `/api/seller/{sellerId}/contacts/{contactId}?userId=` | Kustuta kontakt |

### SellerContactResponseDto väljaed (GET vastus)
```json
{
  "contactId": 0,
  "firstName": "string",
  "middleName": "string",
  "lastName": "string",
  "phone": "string",
  "email": "string",
  "roles": []
}
```

### SellerContactDto väljaed (POST body)

Kontrolli `docs/dtos/schema/` kaustast — schema fail `SellerContactDto_schema.json` (kui puudub, loo see).

Vajalikud väljad vaadates mock-i: eesnimi, (eesnimi), perenimi, telefon, email, rollid (multiselect).

---

## Funktsionaalsus

### Route ja navigatsioon

- Route: `/seller/:sellerId/settings` → `sellerSettingsRoute`
- `sellerId` loetakse route parameetrist: `this.$route.params.sellerId`
- "Seaded" nupp `SellerView.vue`-s ja `SellersView.vue`-s peab navigeerima siia — lisa `NavigationService.navigateToSellerSettingsView(sellerId)`

### Üldandmed sektsiooni (ainult kuvamine)

- Kuva: **Ettevõtte nimi**, **Reg.nr**, **Staatus** (`ACTIVE` → "Aktiivne", `INACTIVE` → "Peatatud"), **Lepingu algus**, **Lepingu lõpp** (kui puudub → "Tähtajatu"), **Märkused**
- Pliiats-ikoon (`✏`) nupp — navigeerib `SellerFormView`-sse muutmisrežiimis: `NavigationService.navigateToSellerFormView(sellerId)`

### Kontaktid sektsiooni

- Kuva kontaktide tabel: **Nimi** (firstName + middleName + lastName), **Telefon**, **E-mail**, **Rollid** (massiiv, kuva komadega eraldatult), **Kustuta** nupp
- "Lisa kontakt" roheline nupp — avab `SellerSettingsContactModal`
- Kustuta nupp — kutsub DELETE endpoint, laeb kontaktide nimekirja uuesti

### SellerSettingsContactModal

- Avab/sulgeb: `isContactModalOpen` boolean andmetes
- Väljad: eesnimi, keskmisnimi (valikuline), perenimi, telefon, email, rollid (checkbox-id: L, A, R, T)
- Salvestamine: POST → sulge modal → laadi kontaktide nimekiri uuesti
- Veakäsitlus: kuva `errorMessage` modali sees
- Sündmus modali sulgemisel: `@event-modal-closed="isContactModalOpen = false"`
- Sündmus salvestamisel: `@event-contact-saved="handleContactSaved"`

---

## SellerService.js — lisa meetodid

```javascript
sendPostSellerContact(sellerId, userId, contactData) {
  return axios.post('/api/seller/' + sellerId + '/contacts?userId=' + userId, contactData)
},
sendDeleteSellerContact(sellerId, contactId, userId) {
  return axios.delete('/api/seller/' + sellerId + '/contacts/' + contactId + '?userId=' + userId)
},
```

---

## Vastuvõtu kriteeriumid

- [ ] Route `/seller/:sellerId/settings` töötab ja kuvab SellerSettingsView
- [ ] "Seaded" nupp navigeerib õigesti sellesse vaatesse
- [ ] Üldandmed on kuvatud (ettevõtte nimi, reg.nr, staatus eesti keeles, kuupäevad, märkused)
- [ ] Pliiats-nupp viib SellerFormView muutmisrežiimi
- [ ] Kontaktide nimekiri laadib ja kuvab kõik kontaktid koos rollidega
- [ ] "Lisa kontakt" nupp avab modali
- [ ] Kontakti lisamine õnnestub — modal sulgub, nimekiri uuendatakse
- [ ] Kontakti kustutamine eemaldab rea nimekirjast
- [ ] 403 viga (pole Admin) kuvab veateate
- [ ] `SellerSettingsView.vue` template on struktureeritud nii et piirkondade ja teenustasude sektsioonid saab hiljem lisada (jäta `<!-- TODO: piirkonnad -->` ja `<!-- TODO: teenustasud -->` kohahoidjad)

---

## Märkus paralleelse töö kohta

Tiimikaaslane loob samal ajal piirkondade ja teenustasude modaalid eraldi failidena.
Lõpuks lisab üks inimene need modaalid `SellerSettingsView.vue`-sse:

```html
<!-- Tiimikaaslase komponentid tulevad siia -->
<SellerSettingsRegionModal ... />
<SellerSettingsProductModal ... />
```

Merge konflikt on tõenäoline ainult `SellerSettingsView.vue` template'i lõpus.
