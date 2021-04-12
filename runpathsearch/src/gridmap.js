import * as CoordUtil from "./coordutil.js";
import fetch from "node-fetch";

class GridMap {
    constructor(zoomLevel) {
        this.zoomLevel = zoomLevel;
        this.tileSize = 1 << zoomLevel;
        this.cells = {};
    }

    cellIDFor(coord) {
        const normalized = CoordUtil.normalize(coord);
        const cellX = Math.floor(normalized.x * this.tileSize);
        const cellY = Math.floor(normalized.y * this.tileSize);
        return {
            x: cellX,
            y: cellY,
        };
    }

    async cellFor(cellID) {
        const flatCellID = `${cellID.x}_${cellID.y}`;
        if (!this.cells[flatCellID]) {
            const dataURL = `https://storage.googleapis.com/download/storage/v1/b/no-right-turns-grid/o/${cellID.x}_${cellID.y}?alt=media`;
            const data = await fetch(dataURL);
            const json = await data.json();
            this.cells[flatCellID] = json;
        }
        return this.cells[flatCellID];
    }
}

export default GridMap;