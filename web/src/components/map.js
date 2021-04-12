import { useRouter } from 'next/router';
import React, { useRef } from 'react';
import {
    MapContainer, TileLayer, Polyline, Rectangle, Circle,
    Marker,
    Popup
} from 'react-leaflet'

function Map({ fromCoord, toCoord, shortestPath }) {
    const router = useRouter();

    const mapState = {
        center: [35.69820027307606, 139.4731736718688],
        // marker: {
        //     lat: 31.698956,
        //     lng: 76.732407,
        // },
        zoom: 11,
        draggable: true,
    };

    const pathLineOptions = { color: '#ff3333' };

    const pathPolyline =
        shortestPath.map(node => [node.lat, node.lng]);

    const fromMarkerRef = useRef(null)
    const toMarkerRef = useRef(null)

    function navigate(from, to) {
        router.push(`/map/${from.lat},${from.lng}/${to.lat},${to.lng}`);
    }

    const fromEventHandlers = {
        dragend() {
            const marker = fromMarkerRef.current
            if (marker == null) {
                return;
            }
            navigate(marker.getLatLng(), toCoord);
        },
    };

    const toEventHandlers = {
        dragend() {
            const marker = toMarkerRef.current
            if (marker == null) {
                return;
            }
            console.log(marker.getLatLng());
            navigate(fromCoord, marker.getLatLng());
        },
    };

    return (
        <div className="map-root">
            <MapContainer
                center={mapState.center}
                zoom={mapState.zoom}
                style={{
                    height: "700px"
                }}>
                <TileLayer
                    attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                <Marker
                    draggable={true}
                    eventHandlers={fromEventHandlers}
                    position={fromCoord}
                    animate={true}
                    ref={fromMarkerRef}
                >
                </Marker>

                <Marker
                    draggable={true}
                    eventHandlers={toEventHandlers}
                    position={toCoord}
                    animate={true}
                    ref={toMarkerRef}
                >
                </Marker>

                <Polyline pathOptions={pathLineOptions} positions={pathPolyline} />

            </MapContainer>
            {/* <style jsx>{`
            .map - root {
                height: 100 %;
            }
                .leaflet - container {
                    height: 400px!important;
                    width: 80 %;
                    margin: 0 auto;
                }
            `}
                </style> */}
        </div>
    );
    // }
}
export default Map;
