import Vue from 'vue'
import VueRouter from 'vue-router'
import Vuetify from 'vuetify'

import 'vuetify/dist/vuetify.min.css'
import App from './App.vue'
import router from './router'

import store from './store'

Vue.use(Vuetify)
Vue.use(VueRouter)

const app = new Vue({
    ...App,
    store,
    router
}).$mount("#mount-point")
