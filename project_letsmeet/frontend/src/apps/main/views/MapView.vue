<template>
    <v-container fluid full-height class="map-container">
        <!--Location map selection-->
        <div>
            <v-alert class="selection-confirm"
                     color="success"
                     :value="selected"
                     transition="scale-transition">
                <template v-if="selected">
                    Selected <strong>{{ selected.city }}</strong> at <strong>{{ selected.country}}</strong>
                    <div style="float: right">
                        <v-btn @click="selected = null">Cancel</v-btn>
                        <v-btn color="primary" @click="confirmSelection">Travel!</v-btn>
                    </div>
                </template>
            </v-alert>
            <div>
                <locationMap :locations="locations" class="location-map"
                             :showSelected="confirmSelected != null"
                             @selected="locationSelected"
                             ref="locationMap"></locationMap>
            </div>
        </div>

        <!-- Help container -->
        <div class="help-panel">
            <v-card>
                <v-card-text>Choose a city to travel to by clicking on a marker!</v-card-text>
            </v-card>
        </div>
    </v-container>
</template>

<script>
    import LocationMap from '../components/LocationMap.vue'
    import { mapPoints } from '../api'

    export default {
        components: {LocationMap},

        beforeRouteEnter(to, from, next) {
            document.documentElement.style.overflow = 'hidden'
            next()
        },

        beforeRouteLeave(to, from, next) {
            document.documentElement.style.overflow = null
            next()
        },

        created() {
            mapPoints().then(locations => this.locations = locations)
        },

        data() {
            return {
                locations: [],
                selected: null,
                confirmSelected: null,
                sources: null
            }
        },

        methods: {
            locationSelected(location) {
                this.selected = location
            },

            confirmSelection() {
                this.confirmSelected = this.selected
                this.selected = null
                this.$router.push({name: 'location', params: {id: this.confirmSelected.id}})
            }
        }
    }
</script>

<style scoped>
    .location-map {
        /* Full screen minus vuetify container padding */
        height: 100vh
    }

    .selection-confirm {
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translate(-50%);
        width: 700px;
        z-index: 1000;
    }

    .map-container {
        padding: 0;
        margin: 0;
    }

    .help-panel {
        position: absolute;
        left: 25px;
        bottom: 25px;
        z-index: 999;
    }

</style>

