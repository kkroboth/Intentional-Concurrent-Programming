<template>
    <v-card>
        <v-card-text>
            <div v-for="event in source">
                <div>
                    <a :href="event.url">{{event.name}}</a>
                </div>
                <img :src="event.image.url" alt="Image of event" height="300">
            </div>
        </v-card-text>
    </v-card>
</template>

<script>
    export default {
        props: ['source']
    }
</script>

<style scoped>

</style>