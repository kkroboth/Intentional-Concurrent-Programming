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
        // array of locations
        props: ['locations', 'showSelected'],

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

            populate(this.leafletMap, this.markerGroup, this.locations, this)
        },

        data() {
            return {
                internalSelected: null
            }

        },

        methods: {
            centerSelected() {
                if (this.internalSelected) {
                    this.leafletMap.fitBounds(L.latLngBounds([this.internalSelected.getLatLng()]))
                    this.leafletMap.setZoom(5)
                }
            }
        },

        watch: {
            'locations'(to) {
                if (!to || !to.length) return
                populate(this.leafletMap, this.markerGroup, to, this)
            }
        }
    }

    function populate(leafletMap, markerGroup, locations, vm) {
        markerGroup.clearLayers()
        locations.forEach(l => {
            const marker = L.marker(l.latlong, {icon: getDivIcon()})
                .bindTooltip(l.city + "")
                .on('click', function () {
                    // Optional zoom into marker if far out
                    if (leafletMap.getZoom() >= 5) return
                    leafletMap.fitBounds(L.latLngBounds([this.getLatLng()]))
                    leafletMap.setZoom(5)
                }).on('click', (e) => {
                    vm.$emit('selected', e.target.location)
                    vm.internalSelected = e.target
                })
            marker.location = l
            markerGroup.addLayer(marker)
        })
    }

    function getDivIcon() {
        if (!getDivIcon.icon) {
            getDivIcon.icon = L.divIcon({className: 'fas fa-suitcase fa-lg'})
        }

        return getDivIcon.icon
    }
</script>

<style scoped>

</style>