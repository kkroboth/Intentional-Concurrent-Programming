<template>
    <div>
        <v-toolbar dark color="primary">
            <v-btn icon @click="$router.replace('/map')">
                <v-icon>arrow_back</v-icon>
            </v-btn>
            <v-toolbar-title>{{this.location ? this.location.city : ""}}</v-toolbar-title>
            <v-spacer></v-spacer>
        </v-toolbar>
        <v-container fluid full-height>
            <div v-if="payload == null" class="loader"></div>
            <div v-else>
                <v-layout row wrap>
                    <v-flex xs6 md2 class="source-item">
                        <SourceMap :location="location"></SourceMap>
                    </v-flex>
                    <v-flex xs12 md5 lg4 class="source-item">
                        <WeatherSource :source="payload.weather"></WeatherSource>
                    </v-flex>
                    <v-flex xs12 md5 lg4 class="source-item">
                        <CountrySource :source="payload.country" :city="location.city"></CountrySource>
                    </v-flex>
                    <v-flex xs12 align-center>
                        <v-layout class="source-grid" :class="{'hide': $route.name != 'location'}" row wrap
                                  style="width: 70%; margin: 0 auto;">
                            <v-flex xs6>
                                <div @click="$router.push(`/location/${$route.params.id}/restaurants`)"
                                     class="blur-overlay restaurants">
                                    <div class="container">
                                        <h2>Restaurants</h2>
                                    </div>
                                </div>
                            </v-flex>
                            <v-flex xs6>
                                <div @click="$router.push(`/location/${$route.params.id}/events`)"
                                     class="blur-overlay events">
                                    <div class="container">
                                        <h2>Events</h2>
                                    </div>
                                </div>
                            </v-flex>
                        </v-layout>
                        <router-view></router-view>
                    </v-flex>
                </v-layout>
            </div>
        </v-container>
    </div>
</template>

<script>
    import { retrieveAggregation, retrievePoint } from "../api"
    import CountrySource from '../components/CountrySource.vue'
    import SourceMap from '../components/SourceMap.vue'
    import WeatherSource from '../components/WeatherSource.vue'
    import EventSource from '../components/EventSource.vue'

    export default {
        components: {CountrySource, SourceMap, WeatherSource, EventSource},

        created() {
            retrievePoint(this.$route.params.id)
                .then(location => this.location = location)
                .then(() => retrieveAggregation(this.$route.params.id))
                .then(sources => {
                    this.payload = sources
                    this.$store.commit('setPayload', sources)
                })
        },

        data() {
            return {
                location: null,
                payload: null
            }
        }
    }
</script>

<style scoped>

    .source-grid.hide {
        display: none;
    }

    .source-item {
        margin: 24px;
    }

    .blur-overlay {
        position: relative;
        overflow: hidden;
        height: 170px;
        cursor: pointer;
        margin: 20px;
    }

    .blur-overlay .container {
        position: relative;
        z-index: 2;
        color: #ffffff;
    }

    .blur-overlay h2 {
        position: relative;
        top: calc((170px / 2) - 15px);
        text-align: center;
    }

    .blur-overlay:hover h2 {
        text-decoration: underline;
    }

    .blur-overlay:after {
        content: "";
        position: absolute;
        z-index: 1;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-repeat: no-repeat;
        width: 100%;
        transform: scale(1.1);
        background-size: cover;

        transition: 0.5s filter ease-in
    }

    .blur-overlay:hover:after {
        filter: blur(4px);
    }

    .blur-overlay.restaurants:after {
        background-image: linear-gradient(
                rgba(0, 0, 0, 0.5),
                rgba(0, 0, 0, 0.5)
        ),
        url("/static/img/restaurant.jpg");
    }

    .blur-overlay.events:after {
        background-image: linear-gradient(
                rgba(0, 0, 0, 0.5),
                rgba(0, 0, 0, 0.5)
        ),
        url("/static/img/events.jpg");
    }

    /**
    https://github.com/lukehaas/css-loaders
     */
    .loader,
    .loader:after {
        border-radius: 50%;
        width: 10em;
        height: 10em;
    }

    .loader {
        margin: 60px auto;
        font-size: 10px;
        position: relative;
        text-indent: -9999em;
        border-top: 1.1em solid rgba(255, 69, 0, 0.2);
        border-right: 1.1em solid rgba(255, 69, 0, 0.2);
        border-bottom: 1.1em solid rgba(255, 69, 0, 0.2);
        border-left: 1.1em solid #ff4500;
        -webkit-transform: translateZ(0);
        -ms-transform: translateZ(0);
        transform: translateZ(0);
        -webkit-animation: load8 1.1s infinite linear;
        animation: load8 1.1s infinite linear;
    }

    @-webkit-keyframes load8 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }

    @keyframes load8 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }

</style>