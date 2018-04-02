<template>
    <div>
        <p class="headline">Events</p>
        <div v-for="event in events">
            <v-layout row>
                <v-flex xs4>
                    <img class="rest-img" :src="event.image.url" alt="event">
                </v-flex>
                <v-flex style="margin-left: 10px;">
                    <a class="title" :href="event.url">{{event.name}}</a>
                    <p>{{getDateTime(event.date)}}</p>
                </v-flex>
            </v-layout>
        </div>
    </div>
</template>

<script>
    export default {

        computed: {
            events() {
                return this.$store.state.payload.events
            }
        },

        methods: {
            getDateTime(date) {
                return new Date(date.dateTime).toLocaleString()
            }
        }
    }
</script>

<style scoped>
    .rest-img {
        max-width: 100%;
        height: auto;
    }
</style>