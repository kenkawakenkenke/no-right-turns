import { makeStyles } from '@material-ui/core/styles';
import { Map, TileLayer, Marker, Popup } from 'react-leaflet-universal';

const useStyles = makeStyles((theme) => ({
    root: {
        height: "100%",
    },
}));

function parseCoordString(str) {
    if (!str) {
        return { lat: 0, lng: 0 };
    }
    const match = str.match("([0-9\\.]*),([0-9\\.]*)");
    return {
        lat: parseFloat(match[1]),
        lng: parseFloat(match[2]),
    };
}

export default function Home({ serverStatus, serverFromCoord, serverToCoord, serverShortestPath, tComputed }) {
    const classes = useStyles();

    const position = { lat: 51.505, lng: -0.09 };
    const zoom = 10;
    return (
        <div className={classes.root}>
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
    )
}

export async function getStaticPaths() {
    return {
        paths: [],
        fallback: true,
    }
}

// This gets called on every request
export async function getStaticProps(context) {
    const from = context.params.from;
    const to = context.params.to;

    const fromCoord = parseCoordString(from);
    const toCoord = parseCoordString(to);

    // console.log("server: ", fromCoord, toCoord);
    // const url = `http://localhost:8080/?fromLat=${fromCoord.lat}&fromLng=${fromCoord.lng}&toLat=${toCoord.lat}&toLng=${toCoord.lng}`;
    const url = `https://pathsearch-em47pjgnhq-an.a.run.app/?fromLat=${fromCoord.lat}&fromLng=${fromCoord.lng}&toLat=${toCoord.lat}&toLng=${toCoord.lng}`;

    const data = await fetch(url);
    const json = await data.json();

    // Pass data to the page via props
    return {
        props: {
            serverStatus: json.status,
            serverFromCoord: fromCoord,
            serverToCoord: toCoord,
            serverShortestPath: json,
            tComputed: new Date().getTime(),
            // ssData: ssData,
        }
    }
}
