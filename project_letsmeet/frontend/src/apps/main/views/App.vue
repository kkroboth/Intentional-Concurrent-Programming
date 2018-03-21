<template>
    <v-app>
        <v-content>
            <v-container fluid full-height>
                <!--Location map selection-->
                <div>
                    <v-alert class="selection-confirm"
                             color="success"
                             :value="selected"
                             transition="scale-transition">
                        <template v-if="selected">
                            Selected <strong>{{ selected.place }}</strong> at <strong>{{ selected.country}}</strong>
                            <div style="float: right">
                                <v-btn @click="selected = null">Cancel</v-btn>
                                <v-btn color="primary" @click="confirmSelection">Travel!</v-btn>
                            </div>
                        </template>
                    </v-alert>
                    <div :class="{minimized: confirmSelected != null}">
                        <locationMap :locations="locations" class="location-map"
                                     :showSelected="confirmSelected != null"
                                     @selected="locationSelected"
                                     ref="locationMap"></locationMap>
                    </div>
                </div>

                <!-- Information and events of location -->
                <div>

                </div>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    import LocationMap from '../components/LocationMap.vue'
    import { mapPoints, retrievePoint } from '../api'

    export default {
        components: {LocationMap},

        created() {
            mapPoints().then(locations => this.locations = locations)
        },

        data() {
            return {
                locations: [],
                selected: null,
                confirmSelected: null
            }
        },

        methods: {
            locationSelected(locationId) {
                // fetch info about location and prompt user
                // to select it
                retrievePoint(locationId).then(info => {
                    this.selected = info
                })
            },

            confirmSelection() {
                this.confirmSelected = this.selected
                this.selected = null

                // Invalidate map
                this.$refs.locationMap.leafletMap.invalidateSize()
                this.$refs.locationMap.centerSelected()
            }
        }
    }
</script>

<style scoped>
    .location-map {
        /* Full screen minus vuetify container padding */
        height: calc(100vh - 32px)
    }

    .minimized {
        float: right;
        height: 250px;
        width: 400px;
    }

    .selection-confirm {
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translate(-50%);
        width: 700px;
        z-index: 1000;
    }
</style>