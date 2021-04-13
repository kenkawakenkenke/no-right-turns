import fs from "fs";
import * as CoordUtil from "./coordutil.js";
import GridMap from "./gridmap.js";
import Heap from "heap";

const minimizedSegments = JSON.parse(fs.readFileSync("data/minimizedSegments.json"));

const zoomLevel = 15;

async function getNearestNodeAndCell(gridMap, coord) {
    const containingCell = gridMap.cellIDFor(coord);

    // TODO: could be undefined, deal with it.
    const cell = await gridMap.cellFor(containingCell);

    let nearestDist = 0;
    let nearestNodeID = null;
    Object.entries(cell.nodes).forEach(([nodeID, nodeCoord]) => {
        const dist = CoordUtil.distance(coord, nodeCoord);
        if (nearestNodeID === null || dist < nearestDist) {
            nearestDist = dist;
            nearestNodeID = nodeID;
        }
    });
    // TODO: we should actually be looking in neighbouring cells too, if the
    // edge border is closer than the nearest found node.
    return {
        nodeID: nearestNodeID,
        cell: cell,
    };
}

function getSegmentsContainingNode(cell, nodeID) {
    return Object.values(cell.segments)
        .filter(detailedSegment =>
            detailedSegment.ns
                .some(n => `${n}` === nodeID)
        );
}

function indexOfNodeInDetailedSegment(detailedSegment, nodeID) {
    for (let i = 0; i < detailedSegment.ns.length; i++) {
        if (`${detailedSegment.ns[i]}` === nodeID) {
            return i;
        }
    }
    return undefined;
}
function checkFromToIsSameSegment(
    segmentsContainingFrom,
    segmentsContainingTo,
    fromNode, toNode,
    fromCell) {
    // Check if we have any segments in common.
    for (const segFrom of segmentsContainingFrom) {
        for (const segTo of segmentsContainingTo) {
            if (segFrom.id !== segTo.id) {
                continue;
            }
            const indexFrom = indexOfNodeInDetailedSegment(segFrom, fromNode);
            const indexTo = indexOfNodeInDetailedSegment(segTo, toNode);
            if (indexFrom <= indexTo) {
                const nodes = [];
                for (let i = indexFrom; i <= indexTo; i++) {
                    nodes.push(
                        renderWaypoint(fromCell, segFrom.ns[i])
                    );
                }
                return nodes;
            }
        }
    }
    return undefined;
}

const UTURN = 0;
const FOLLOW_ON = 1;
const LEFT_TURN = 2;
const RIGHT_TURN = 3;
const MAX_WEIGHT = 999999;
function connectionWeight(connectionType, strategy) {
    switch (connectionType) {
        case UTURN:
            return MAX_WEIGHT;
        case FOLLOW_ON:
            return 0;
        case LEFT_TURN:
            if (strategy == "NO_TURNS") {
                // Prefer not to (but don't completely disallow) left turns.
                return 1000;
            }
            return 0.5;
        case RIGHT_TURN:
            if (strategy == "NO_RIGHT_TURNS" || strategy == "NO_TURNS") {
                // Prefer not to (but don't completely disallow) right turns.
                return 1000;
            }
            return 0.5;
    }
    return MAX_WEIGHT;
}

function compute(
    segmentsContainingFrom,
    segmentsContainingTo,
    strategy) {
    const targetSegments =
        segmentsContainingTo.map(seg => seg.id)
            .reduce((accum, c) => {
                accum[c] = true;
                return accum;
            }, {});

    const shortestDistToSegment = {};
    // Map < Integer, Double > shortestDistToSegment = new HashMap <> ();

    const queue = new Heap((a, b) => {
        return shortestDistToSegment[a] - shortestDistToSegment[b];
    });
    // PriorityQueue < Integer > queue = new PriorityQueue <> (
    //     Comparator.comparingDouble(shortestDistToSegment:: get));

    const processedSegments = {};
    // Set < Integer > processedSegments = new HashSet <> ();

    const prevSegmentForSegment = {};
    // Map < Integer, Tuple < Integer, ConnectionType >> prevSegmentForSegment = new HashMap <> ();

    segmentsContainingFrom.forEach(segmentFrom => {
        // Approximation: we assume the [from] is at the start of the segment (which it typically obvious is not).
        const distToSegment = 0;
        shortestDistToSegment[segmentFrom.id] = distToSegment;
        queue.push(segmentFrom.id);
    });
    // segmentsContainingFrom.stream().forEach(segmentFrom -> {
    //     // Approximation: we assume the [from] is at the start of the segment (which it typically obvious is not).
    //     double distToSegment = 0;
    //     shortestDistToSegment.put(segmentFrom.id, distToSegment);
    //     queue.add(segmentFrom.id);
    // });

    let pickedLastSegment = null;
    while (!queue.empty()) {
        const segmentID = queue.pop();
        if (processedSegments[segmentID]) {
            continue;
        }

        if (targetSegments[segmentID]) {
            // Found.
            // TODO: this is actually an approximation; we don't think about the distance *inside* the segment.
            pickedLastSegment = segmentID;
            break;
        }
        const distanceUpToSegment = shortestDistToSegment[segmentID];
        const minimizedSegment = minimizedSegments[segmentID];
        const distanceToEndOfSegment = distanceUpToSegment + minimizedSegment.d;

        minimizedSegment.cs.forEach(connection => {
            const connectionType = connection.t;
            const weight = connectionWeight(connectionType, strategy);
            if (connectionWeight == MAX_WEIGHT) {
                return;
            }
            const distanceToNextSegment = distanceToEndOfSegment + weight;
            const nextSegment = connection.i;
            const shortestDist = shortestDistToSegment[nextSegment];
            if (!(nextSegment in shortestDistToSegment) || shortestDist > distanceToNextSegment) {
                shortestDistToSegment[nextSegment] = distanceToNextSegment;
                queue.push(nextSegment);
                prevSegmentForSegment[nextSegment] = {
                    segment: segmentID,
                    connectionType,
                };
            }
        });
    }
    if (pickedLastSegment === null) {
        System.err.println("couldn't find path");
        return ImmutableList.of();
    }

    const reversedPickedSegments = [];
    reversedPickedSegments.push({
        segment: pickedLastSegment,
    });
    while (true) {
        const prev = prevSegmentForSegment[reversedPickedSegments[reversedPickedSegments.length - 1].segment];
        if (prev === undefined) {
            break;
        }
        reversedPickedSegments.push(prev);
    }
    return reversedPickedSegments;
}

function unflattenCellID(flatCellID) {
    const match = flatCellID.match("([0-9]*)_([0-9]*)");
    return {
        x: match[1],
        y: match[2],
    };
}

function renderWaypoint(cell, nodeID, connectionType = undefined) {
    const rendered = {
        ...cell.nodes[nodeID],
    };
    switch (connectionType) {
        case UTURN:
            rendered.connectionType = "UTURN";
            break;
        case FOLLOW_ON:
            rendered.connectionType = "FOLLOW_ON";
            break;
        case LEFT_TURN:
            rendered.connectionType = "LEFT_TURN";
            break;
        case RIGHT_TURN:
            rendered.connectionType = "RIGHT_TURN";
            break;
    }
    return rendered;
}
async function toNodes(
    gridMap,
    fromNode,
    toNode,
    reversedPickedSegments) {
    // Prefetch cells in parallel
    {
        const cells = {};
        for (let i = reversedPickedSegments.length - 1; i >= 0; i--) {
            let segmentAndConnection = reversedPickedSegments[i];
            const minimizedSegment = minimizedSegments[segmentAndConnection.segment];
            cells[minimizedSegment.c] = true;
        }
        const cellIDs = Object.keys(cells)
            .map(c => unflattenCellID(c));
        await gridMap.prefetch(cellIDs);
        console.log("prefetch done");
        // await Promise.all(Object.keys(cells)
        //     .map(c => unflattenCellID(c))
        //     .map(cellID => gridMap.cellFor(cellID)));
    }

    const nodes = [];
    let connectionFromPrevSegment = null;
    for (let i = reversedPickedSegments.length - 1; i >= 0; i--) {
        let segmentAndConnection = reversedPickedSegments[i];
        const minimizedSegment = minimizedSegments[segmentAndConnection.segment];
        const cellID = unflattenCellID(minimizedSegment.c);
        const cell = await gridMap.cellFor(cellID);
        const segment = cell.segments[segmentAndConnection.segment];
        if (i == reversedPickedSegments.length - 1) {
            // First segment
            const indexFrom = indexOfNodeInDetailedSegment(segment, fromNode);
            for (let index = indexFrom; index < segment.ns.length - 1; index++) {
                nodes.push(renderWaypoint(cell, segment.ns[index], connectionFromPrevSegment));
                connectionFromPrevSegment = null;
            }
        } else if (i == 0) {
            // Last segment
            const indexTo = indexOfNodeInDetailedSegment(segment, toNode);
            for (let index = 0; index <= indexTo; index++) {
                nodes.push(renderWaypoint(cell, segment.ns[index], connectionFromPrevSegment));
                connectionFromPrevSegment = null;
            }
        } else {
            for (let index = 0; index < segment.ns.length - 1; index++) {
                nodes.push(renderWaypoint(cell, segment.ns[index], connectionFromPrevSegment));
                connectionFromPrevSegment = null;
            }
        }
        connectionFromPrevSegment = segmentAndConnection.connectionType;
    }
    return nodes;
}

// async function fetchTest() {
//     const db = admin.firestore();
//     const doc = await db.collection("cell").doc("29066_12889").get();
//     console.log(doc.data());
// }

export default async function getShortestPath(fromCoord, toCoord, strategy) {
    try {
        const gridMap = new GridMap(15);

        const tStart = Date.now();
        let tPrev = Date.now();

        console.log("========= Start path search");

        await gridMap.prefetch(
            [fromCoord, toCoord].map(coord => gridMap.cellIDFor(coord)));

        const { nodeID: fromNode, cell: fromCell } = await getNearestNodeAndCell(gridMap, fromCoord);
        const { nodeID: toNode, cell: toCell } = await getNearestNodeAndCell(gridMap, toCoord);

        console.log("Fetched from/to: " + (Date.now() - tStart));
        tPrev = Date.now();

        const segmentsContainingFrom = getSegmentsContainingNode(fromCell, fromNode);
        const segmentsContainingTo = getSegmentsContainingNode(toCell, toNode);

        // Check if we have any segments in common.
        const pathsOnSameSegment
            = checkFromToIsSameSegment(segmentsContainingFrom, segmentsContainingTo, fromNode, toNode, fromCell);
        if (pathsOnSameSegment) {
            return {
                status: "OK",
                path: pathsOnSameSegment,
            };
        }

        console.log("Checked for common segment: " + (Date.now() - tStart) + " " + (Date.now() - tPrev));
        tPrev = Date.now();

        const reversedPickedSegments =
            compute(segmentsContainingFrom, segmentsContainingTo, strategy);

        console.log("Searched shortest path: " + (Date.now() - tStart) + " " + (Date.now() - tPrev));
        tPrev = Date.now();

        const nodes = await toNodes(gridMap, fromNode, toNode, reversedPickedSegments);

        console.log("Constructed nodes list: " + (Date.now() - tStart) + " " + (Date.now() - tPrev));
        tPrev = Date.now();

        return {
            status: "OK",
            path: nodes,
        };
    } catch (err) {
        console.log("error", err);
        return {
            status: "error",
            message: "Server error",
        }
    }
}
