<template>
    <div>
        <!--Current-->
        <div>
            <p class="mb-0">{{current.date.toLocaleTimeString()}}</p>
            <p class="mb-1">{{capitalized(current.weather[0].description)}}</p>
            <div class="current-weather">
                <img :src="`http://openweathermap.org/img/w/${current.weather[0].icon}.png`" alt="weather">
                <span>{{current.temp}} &deg;F</span>
            </div>

            <div style="float: right;">
                <p class="mb-0">
                    <strong>Clouds</strong> {{current.clouds}}%
                </p>
                <p class="mb-0">
                    <strong>Humidity</strong> {{current.humidity}}%
                </p>
            </div>
        </div>

        <!--Daily -->
        <Forecast style="text-align: center; clear: both;" :days="middleDays"></Forecast>
    </div>
</template>

<script>
    import Forecast from './widgets/Forecast.vue'

    export default {
        props: ['source'],

        components: {Forecast},

        methods: {
            getMidDay(weatherHours) {
                return weatherHours[Math.floor(weatherHours.length / 2)]
            },

            capitalized(str) {
                return str.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
            }
        },

        computed: {
            datesIncluded() {
                return this.source.map(source => Object.assign({}, source, {date: new Date(source.date)}))
            },

            partitioned() {
                // Assumption: Array is sorted!
                const days = new Map()

                const weather = this.datesIncluded
                weather.forEach(weather => {
                    let day = weather.date.getDay()
                    if (days.has(day))
                        days.get(day).push(weather)
                    else
                        days.set(day, [weather])
                })

                // For the sake of Vue, map to array of arrays
                return Array.from(days.values())
            },

            middleDays() {
                const midDays = []
                this.partitioned.forEach(day => midDays.push(this.getMidDay(day)))
                return midDays
            },

            current() {
                return this.datesIncluded[0]
            }
        }
    }
</script>

<style scoped>
    .current-weather {
        display: flex;
        align-items: center;
        float: left;
    }
</style>