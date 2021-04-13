import React, { useEffect, useRef, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { Map, TileLayer, Marker, Popup } from 'react-leaflet-universal';
import Head from 'next/head';

const useStyles = makeStyles((theme) => ({
    root: {
        // backgroundColor: "red",
        height: "100%",
        minHeight: "100px",
    },
}));

const DEFAULT_BOUNDS = [
    [35.7621781638664, 139.41293293617915],
    [35.642820718714624, 139.84571170956804],
];

function MapView({ fromCoord, toCoord, shortestPath, callback }) {
    const classes = useStyles();

    const position = { lat: 51.505, lng: -0.09 };
    const zoom = 10;
    return (
        <div className={classes.root}>
            <Head>
                <link href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css" rel="stylesheet" integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ==" crossOrigin="" />
            </Head>
            <Map
                center={position} zoom={zoom}
                style={{
                    width: "100%",
                    height: "100%",
                    minHeight: "100px",
                }}>
                <TileLayer
                    attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    opacity={0.8}
                />
            </Map>
        </div>
    );
    // }
}
export default MapView;
