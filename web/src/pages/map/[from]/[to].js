import Head from 'next/head'
import dynamic from 'next/dynamic';
import { useRouter } from "next/router";
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Alert from '@material-ui/lab/Alert';
import Link from "next/link";
import { DEFAULT_MAP_URL } from "../../../components/global.js";

const useStyles = makeStyles((theme) => ({
    root: {
        // backgroundColor: "blue",
        height: "100%",
    },
    topBanner: {
        flexShrink: "1",
        flexGrow: "0",
        // backgroundColor: "gray",
        paddingLeft: "4px",
        paddingRight: "4px",
    },
    main: {
        display: "flex",
        // backgroundColor: "yellow",
        height: "100%",
        flexDirection: "column",
    },
    mapContainer: {
        flex: "1 1 auto",
        minHeight: "100px",
    },
    errorMessage: {
        color: "red",
    },
    warningMessage: {
        backgroundColor: "#ffffcc",
    }
}));

const MapWithNoSSR = dynamic(() => import('../../../components/map'), {
    ssr: false
});

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

function PathInfo({ path }) {
    const numLeftTurns = path.filter(n => n.connectionType === "LEFT_TURN").length;
    const numRightTurns = path.filter(n => n.connectionType === "RIGHT_TURN").length;
    return <div>
        左折回数：{numLeftTurns}回 右折回数：{numRightTurns}回
    </div>;
}

function TopBanner({ serverStatus, tComputed, shortestPath }) {
    const classes = useStyles();

    return <div className={classes.topBanner}>
        {serverStatus === "error" &&
            <Alert severity="error">経路が探せません。遠すぎるか範囲外（東京の外）かも？</Alert>}

        <Link href={DEFAULT_MAP_URL}><Typography variant="h5">絶対右折したくない検索</Typography></Link>
        <Typography variant="body2">
            運転が苦手な自分のために作った、右折せずに目的地まで行ける道を探す経路検索です。
            現在東京都しか対応していません。
            <div className={classes.warningMessage}>表示された経路は誤っているかもしれません。必ず実際の交通ルールに従って運転してください。</div>
            作者：<a href="https://twitter.com/kenkawakenkenke" target="_blank">河本健</a>
        </Typography>
        {/* {shortestPath && shortestPath.path && <PathInfo path={shortestPath.path} />} */}
    </div>;
}

export default function Home({ serverStatus, fromCoord, toCoord, shortestPath, tComputed }) {
    const classes = useStyles();

    if (!fromCoord || !toCoord || !shortestPath) {
        return <div>computing...</div>;
    }
    return (
        <div className={classes.root}>
            <Head>
                <title>絶対右折したくない経路検索</title>
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <main className={classes.main}>
                <TopBanner
                    serverStatus={serverStatus}
                    tComputed={tComputed}
                    shortestPath={shortestPath} />
                <div className={classes.mapContainer}>
                    <MapWithNoSSR fromCoord={fromCoord} toCoord={toCoord} shortestPath={shortestPath.path || []} />
                </div>
            </main>
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
            fromCoord,
            toCoord,
            shortestPath: json,
            tComputed: new Date().getTime(),
            // ssData: ssData,
        }
    }
}
