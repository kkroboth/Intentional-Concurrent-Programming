<template>
    <v-app>
        <v-content>
            <v-container fluid full-height>
                <locationMap :locations="locations" class="location-map"></locationMap>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    import LocationMap from '../components/LocationMap.vue'
    import { mapPoints } from '../api'

    export default {
        components: {LocationMap},

        created() {
            mapPoints().then(locations => this.locations = locations)
        },

        data() {
            return {
                locations: []
            }
        }
    }
</script>

<style scoped>
    .location-map {
        /* Full screen minus vuetify container padding */
        height: calc(100vh - 32px)
    }
</style>