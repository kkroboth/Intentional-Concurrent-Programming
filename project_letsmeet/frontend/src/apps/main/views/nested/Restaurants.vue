<template>
    <div>
        <p class="headline">Restaurants</p>
        <div v-for="rest in restaurants">
            <v-layout row>
                <v-flex xs4>
                    <img class="rest-img" :src="rest.image" alt="restaurant">
                </v-flex>
                <v-flex style="margin-left: 10px;">
                    <a class="title" :href="rest.url">{{rest.name}}</a>
                    <VueStarRating :increment="0.1" :rating="parseFloat(rest.rating.aggregate_rating)"
                                   read-only></VueStarRating>
                    <p>{{rest.location.address}}</p>
                </v-flex>
            </v-layout>
        </div>
    </div>
</template>

<script>
    import VueStarRating from 'vue-star-rating'

    export default {
        components: {VueStarRating},

        computed: {
            restaurants() {
                return this.$store.state.payload.restaurants
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