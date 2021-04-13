import * as CoordUtil from "./coordutil.js";
import fetch from "node-fetch";
import { uniquify } from "./util/arrays.js";

import admin from 'firebase-admin';
console.log("initialize firebase!");
admin.initializeApp({
    credential: admin.credential.applicationDefault(),
});

class GridMap {
    constructor(zoomLevel) {
        this.zoomLevel = zoomLevel;
        this.tileSize = 1 << zoomLevel;
        this.cells = {};
    }

    async prefetch(cells) {
        const cellsNeedingFetch
            = cells
                .filter(cellID => !(GridMap.getFlatCellID(cellID) in this.cells));
        const uniqueCellsNeedingFetch =
            uniquify(cellsNeedingFetch, cell => GridMap.getFlatCellID(cell));

        const jsons = await Promise.all(
            uniqueCellsNeedingFetch.map(cell => GridMap.fetchCell(cell))
        );
        for (let i = 0; i < uniqueCellsNeedingFetch.length; i++) {
            const flatCellID = GridMap.getFlatCellID(uniqueCellsNeedingFetch[i]);
            this.cells[flatCellID] = jsons[i];
        }
    }

    // async prefetch(cells) {
    //     const cellsNeedingFetch
    //         = cells
    //             .filter(cellID => !(GridMap.getFlatCellID(cellID) in this.cells));
    //     const uniqueCellsNeedingFetch =
    //         uniquify(cellsNeedingFetch, cell => GridMap.getFlatCellID(cell));
    //     console.log("prefetching " + uniqueCellsNeedingFetch.length + " docs");
    //     const collection = admin.firestore().collection("cell");
    //     const docs =
    //         uniqueCellsNeedingFetch
    //             .map(cell => GridMap.getFlatCellID(cell))
    //             .map(cell => collection.doc(cell));

    //     const datas = await admin.firestore().getAll(...docs);
    //     const jsons = [];
    //     datas.forEach(data => jsons.push(data.data()));

    //     for (let i = 0; i < uniqueCellsNeedingFetch.length; i++) {
    //         const flatCellID = GridMap.getFlatCellID(uniqueCellsNeedingFetch[i]);
    //         this.cells[flatCellID] = jsons[i];
    //     }
    // }

    cellIDFor(coord) {
        const normalized = CoordUtil.normalize(coord);
        const cellX = Math.floor(normalized.x * this.tileSize);
        const cellY = Math.floor(normalized.y * this.tileSize);
        return {
            x: cellX,
            y: cellY,
        };
    }

    static getFlatCellID(cellID) {
        return `${cellID.x}_${cellID.y}`;
    }
    static async fetchCell(cellID) {
        const dataURL = `https://storage.googleapis.com/download/storage/v1/b/no-right-turns-grid/o/${cellID.x}_${cellID.y}?alt=media`;
        const tStart = Date.now();
        // console.log("fetch: ", dataURL);
        const data = await fetch(dataURL);
        const json = await data.json();
        return json;
    }

    async cellFor(cellID) {
        const flatCellID = GridMap.getFlatCellID(cellID);
        if (!this.cells[flatCellID]) {
            const tStart = Date.now();
            const json = await GridMap.fetchCell(cellID);
            this.cells[flatCellID] = json;
        }
        return this.cells[flatCellID];
    }
}

export default GridMap;