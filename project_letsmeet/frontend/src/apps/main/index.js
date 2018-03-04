import Vue from 'vue'
// Use vuetify framework
import Vuetify from 'vuetify'

import 'vuetify/dist/vuetify.min.css'

import App from './views/App.vue'


Vue.use(Vuetify)

const app = new Vue({
    ...App
}).$mount("#mount-point")
