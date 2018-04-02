import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        'payload': null
    },

    mutations: {
        setPayload(state, payload) {
            state.payload = payload
        }
    }
})