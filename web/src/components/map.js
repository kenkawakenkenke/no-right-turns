import { useRouter } from 'next/router';
import React, { useRef } from 'react';
import {
    MapContainer, TileLayer, Polyline, Rectangle, Circle,
    CircleMarker,
    LayerGroup,
    Tooltip,
    Marker,
    Popup
} from 'react-leaflet'

function computeBounds(path) {
    if (path.length === 0) {
        return [
            [35.7621781638664, 139.41293293617915],
            [35.642820718714624, 139.84571170956804],
        ];
    }
    let west = path[0].lng;
    let east = path[0].lng;
    let north = path[0].lat;
    let south = path[0].lat;
    path.forEach(coord => {
        west = Math.min(west, coord.lng);
        east = Math.max(east, coord.lng);
        north = Math.max(north, coord.lat);
        south = Math.min(south, coord.lat);
    });
    const centerLng = (west + east) / 2;
    const centerLat = (north + south) / 2;
    const expander = 1.1;
    west = (west - centerLng) * expander + centerLng;
    east = (east - centerLng) * expander + centerLng;
    north = (north - centerLat) * expander + centerLat;
    south = (south - centerLat) * expander + centerLat;
    return [
        [north, west],
        [south, east]
    ];
}

function ConnectionTypeMarkers({ path, connectionType, color }) {
    const nodesOfInterest = path
        .filter(node =>
            node.connectionType === connectionType);

    return <LayerGroup>
        {nodesOfInterest.map((turn, idx) =>
            <Circle
                key={`turn_${connectionType}_${idx}`}
                center={[turn.lat, turn.lng]}
                pathOptions={{ color }}
                radius={20} />
        )}
    </LayerGroup>;
}

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
                bounds={computeBounds(shortestPath)}
                style={{
                    width: "100%",
                    height: "700px"
                }}>
                <TileLayer
                    attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    opacity={0.7}
                />

                <Marker
                    draggable={true}
                    eventHandlers={fromEventHandlers}
                    position={fromCoord}
                    animate={true}
                    ref={fromMarkerRef}
                >
                    <Tooltip permanent
                    >Start!</Tooltip>
                </Marker>

                <Marker
                    draggable={true}
                    eventHandlers={toEventHandlers}
                    position={toCoord}
                    animate={true}
                    ref={toMarkerRef}
                >
                    <Tooltip permanent
                    >End!</Tooltip>
                </Marker>

                <Polyline pathOptions={pathLineOptions} positions={pathPolyline} />

                <ConnectionTypeMarkers
                    path={shortestPath}
                    connectionType="LEFT_TURN"
                    color="orange" />
                <ConnectionTypeMarkers
                    path={shortestPath}
                    connectionType="RIGHT_TURN"
                    color="red" />
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
