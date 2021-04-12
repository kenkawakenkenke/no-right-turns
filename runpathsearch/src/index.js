import express from 'express';
import getShortestPath from "./pathsearch.js";

const tInstanceStarted = new Date();

const app = express();

app.get('/', async (req, res) => {
    const fromCoord = {
        lat: req.query.fromLat,
        lng: req.query.fromLng,
    };
    const toCoord = {
        lat: req.query.toLat,
        lng: req.query.toLng,
    };
    const strategy = req.query.strategy || "NO_RIGHT_TURNS";

    const pathResponse = await getShortestPath(fromCoord, toCoord, strategy);

    res.set('Cache-Control', 'public, max-age=60');
    // res.set('Cache-Control', 'public, max-age=31536000');
    res.send(JSON.stringify(pathResponse));
});

app.get('/infoz', (req, res) => {
    const info = {
        tStarted: tInstanceStarted,
    };
    res.send(JSON.stringify(info));
});

const port = process.env.PORT || 8080;
app.listen(port, () => {
    console.log(`pathsearch: running at: https://localhost:${port}`);
});
