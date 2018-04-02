<template>
    <v-app>
        <v-content>
            <transition name="fade">
                <router-view></router-view>
            </transition>
        </v-content>
    </v-app>
</template>

<script>
    export default {

    }
</script>

<style scoped>
    .fade-enter-active, .fade-leave-active {
        transition: opacity .5s ease;
    }

    .fade-enter, .fade-leave-active {
        opacity: 0
    }
</style>