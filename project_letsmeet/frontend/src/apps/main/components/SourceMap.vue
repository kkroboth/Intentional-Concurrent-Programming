<template>
    <div>
        <div style="height: 250px;" ref="map"></div>
    </div>
</template>

<script>
    import L from 'leaflet'
    import 'leaflet/dist/leaflet.css'

    export default {
        props: ['location'],

        mounted() {
            const leafletMap = L.map(this.$refs.map, {
                keyboard: false,
                scrollWheelZoom: false,
                tap: false
            })
            leafletMap.dragging.disable()
            leafletMap.touchZoom.disable()
            leafletMap.doubleClickZoom.disable()
            leafletMap.boxZoom.disable()

            L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(leafletMap)
            leafletMap.fitBounds(L.latLngBounds([this.location.latlong]))
            leafletMap.setZoom(9)

            L.marker(this.location.latlong).addTo(leafletMap)

        }
    }
</script>

<style scoped>

</style>