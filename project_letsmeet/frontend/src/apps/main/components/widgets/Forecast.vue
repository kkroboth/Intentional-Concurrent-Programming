<template>
    <div>
        <div v-for="day in days" class="day-weather">
            <p>{{dayOfWeek(day.date)}}</p>
            <img :src="`http://openweathermap.org/img/w/${day.weather[0].icon}.png`" alt="weather"
                 :title="day.weather[0].description">
            <span>{{day.temp}} &deg;F</span>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['days'],

        methods: {
            dayOfWeek(date) {
                let utc = date.toString()
                return utc.substring(0, utc.indexOf(' '))
            }
        }
    }
</script>

<style scoped>
    .day-weather {
        display: inline-block;
    }

    .day-weather:not(:first-of-type) {
        margin-left: 16px;
    }

    .day-weather img {
        display: block;
    }

    .day-weather p {
        text-align: center;
    }

</style>