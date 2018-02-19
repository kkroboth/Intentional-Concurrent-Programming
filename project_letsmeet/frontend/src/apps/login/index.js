import Vue from 'vue'
// Use vuetify framework
import Vuetify from 'vuetify'
import LoginView from './views/Login.vue'

import 'vuetify/dist/vuetify.min.css'


Vue.use(Vuetify)

const app = new Vue({
    ...LoginView
}).$mount("#mount-point")
