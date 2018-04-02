import VueRouter from 'vue-router'

import MapView from '../views/MapView.vue'
import LocationView from '../views/LocationView.vue'
import Restaurants from '../views/nested/Restaurants.vue'
import Events from '../views/nested/Events.vue'

export default new VueRouter({
    mode: 'history',
    routes: [
        {path: '/', redirect: {name: 'map'}},
        {path: '/map', name: 'map', component: MapView},
        {
            path: '/location/:id/', name: 'location', component: LocationView,
            children: [
                {path: 'restaurants', name: 'location-restaurants', component: Restaurants},
                {path: 'events', name: 'location-events', component: Events}
            ]
        }
    ]
})