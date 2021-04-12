
function toRadians(deg) {
    return deg * PI_180;
}
export function normalize({ lat, lng }) {
    // first convert to Mercator projection
    // first convert the lat lon to mercator coordintes.
    if (lng > 180) {
        lng -= 360;
    }

    lng /= 360;
    lng += 0.5;

    lat = 0.5 - ((Math.log(Math.tan((Math.PI / 4) + toRadians(0.5 * lat))) / Math.PI) / 2.0);

    return {
        x: lng,
        y: lat,
    };
}

const RADIUS_OF_EARTH = 6371;
const DIAMETER_OF_EARTH = RADIUS_OF_EARTH * 2;
const PI_180 = Math.PI / 180;
const PI_360 = Math.PI / 360;
export function distance(coord, otherCoord) {
    const sin_dLat = Math.sin((coord.lat - otherCoord.lat) * PI_360);
    const sin_dLon = Math.sin((otherCoord.lng - coord.lng) * PI_360);
    const lat_cos = Math.cos(coord.lat * PI_180);
    const other_lat_cos = Math.cos(otherCoord.lat * PI_180);
    const a = sin_dLat * sin_dLat + sin_dLon * sin_dLon * lat_cos * other_lat_cos;
    return DIAMETER_OF_EARTH * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}
