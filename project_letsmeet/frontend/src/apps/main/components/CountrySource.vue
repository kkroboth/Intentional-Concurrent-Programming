<template>
    <div>
        <v-container fluid grid-list-lg>
            <v-layout row>
                <v-flex xs7>
                    <v-card-media :src="source.flag" height="130px" contain></v-card-media>
                    <h3 class="headline mb-0">{{source.name}}</h3>
                    <div>{{city}}</div>
                </v-flex>
                <v-flex xs5>
                    <p class="mb-0"><strong>Currency </strong> {{currency.code}} ({{currency.symbol}}) </p>
                    <p class="mb-0"><strong>Capital</strong> {{source.capital}}</p>
                    <p class="mb-0"><strong>Language</strong> {{language.name}}</p>
                </v-flex>
            </v-layout>
        </v-container>
    </div>
</template>

<script>
    export default {
        props: ['source', 'city'],

        computed: {
            currency() {
                return this.source.currencies[0]
            },

            language() {
                return this.source.languages[0]
            }
        }
    }
</script>

<style scoped>

</style>