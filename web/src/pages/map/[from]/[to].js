import Head from 'next/head'
import dynamic from 'next/dynamic';
import { useRouter } from "next/router";

// import styles from '../styles/Home.module.css'

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
export default function Home({ fromCoord, toCoord, shortestPath, tComputed }) {

    return (
        <div>
            <Head>
                <title>Create Next App</title>
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <main>
                <p>computed:{new Date(tComputed).toString()}</p>
                Hey lat:{JSON.stringify(fromCoord)} lng:{JSON.stringify(toCoord)}
                <MapWithNoSSR fromCoord={fromCoord} toCoord={toCoord} shortestPath={shortestPath.path} />
            </main>
        </div>
    )
}

// This gets called on every request
export async function getServerSideProps(context) {
    const from = context.params.from;
    const to = context.params.to;

    const fromCoord = parseCoordString(from);
    const toCoord = parseCoordString(to);

    console.log("server: ", fromCoord, toCoord);
    const url = `https://pathsearch-em47pjgnhq-an.a.run.app/?fromLat=${fromCoord.lat}&fromLng=${fromCoord.lng}&toLat=${toCoord.lat}&toLng=${toCoord.lng}`;

    const data = await fetch(url);
    const json = await data.json();

    // const router = useRouter();
    // const { from, to } = router.query;
    // console.log(from, to);
    // Fetch data from external API
    // const res = await fetch(`https://.../data`)
    // const data = await res.json()
    // const ssData = "serverside data";
    // console.log("serverside rendering!!");

    // Pass data to the page via props
    return {
        props: {
            fromCoord,
            toCoord,
            shortestPath: json,
            tComputed: new Date().getTime(),
            // ssData: ssData,
        }
    }
}
