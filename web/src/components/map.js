import { useRouter } from 'next/router';
import React, { useRef } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import {
    MapContainer, TileLayer, Polyline, Rectangle, Circle,
    CircleMarker,
    LayerGroup,
    Tooltip,
    Marker,
    Popup
} from 'react-leaflet'
import { DEFAULT_FROM_COORD, DEFAULT_TO_COORD } from "./global.js";
import { latLng } from 'leaflet';

const useStyles = makeStyles((theme) => ({
    root: {
        // backgroundColor: "red",
        height: "100%",
        minHeight: "100px",
    },
}));

function coordEquals(obj1, obj2) {
    return obj1.lat === obj2.lat && obj1.lng === obj2.lng;
}

function computeBounds(fromCoord, toCoord, path) {
    const coords = [...path];
    if (fromCoord) {
        coords.push(fromCoord);
    }
    if (toCoord) {
        coords.push(toCoord);
    }
    if (coords.length === 0) {
        return [
            [35.7621781638664, 139.41293293617915],
            [35.642820718714624, 139.84571170956804],
        ];
    }
    let west = coords[0].lng;
    let east = coords[0].lng;
    let north = coords[0].lat;
    let south = coords[0].lat;
    coords.forEach(coord => {
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
    const classes = useStyles();
    const router = useRouter();

    const pathLineOptions = { color: '#ff3333' };

    const pathPolyline =
        shortestPath.map(node => [node.lat, node.lng])
    console.log("load map now");

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

    const fromUnchanged = coordEquals(DEFAULT_FROM_COORD, fromCoord);
    const toUnchanged = coordEquals(DEFAULT_TO_COORD, toCoord);

    return (
        <div className={classes.root}>
            <MapContainer
                bounds={computeBounds(fromCoord, toCoord, shortestPath)}
                style={{
                    width: "100%",
                    height: "100%"
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
                    >
                        出発地{fromUnchanged && "：ドラッグして変えてみよう！"}
                    </Tooltip>
                </Marker>

                <Marker
                    draggable={true}
                    eventHandlers={toEventHandlers}
                    position={toCoord}
                    animate={true}
                    ref={toMarkerRef}
                >
                    <Tooltip permanent
                    >
                        目的地{toUnchanged && "：ドラッグして変えてみよう！"}
                    </Tooltip>
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
        </div>
    );
    // }
}
export default Map;
