<template>
    <!-- Div leaflet is mounted on -->
    <div>

    </div>
</template>

<script>
    import L from 'leaflet'
    import 'leaflet.markercluster'
    import 'leaflet/dist/leaflet.css'
    import 'leaflet.markercluster/dist/MarkerCluster.css'
    import 'leaflet.markercluster/dist/MarkerCluster.Default.css'

    L.Icon.Default.imagePath = '/static/leaflet/dist/images/'


    export default {
        // array of location array
        // [id, location-name, lat, lng]
        props: ['locations'],

        mounted() {
            const mountPoint = this.$el
            const leafletMap = L.map(mountPoint)
                .setView([0, 0], 2)
            L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(leafletMap)

            this.markerGroup = L.markerClusterGroup()
            leafletMap.addLayer(this.markerGroup)
            this.leafletMap = leafletMap

            populate(this.leafletMap, this.markerGroup, this.locations)
        },

        watch: {
            'locations'(to) {
                if (!to || !to.length) return
                populate(this.leafletMap, this.markerGroup, to)
            }
        }
    }

    function populate(leafletMap, markerGroup, locations) {
        markerGroup.clearLayers()
        locations.forEach(l => {
            const marker = L.marker([l[3], l[4]])
                .bindPopup(`<strong>${l[1]}</strong><p>${l[2]}</p>`)
            markerGroup.addLayer(marker)
        })
    }
</script>

<style scoped>

</style>