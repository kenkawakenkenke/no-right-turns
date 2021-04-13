
const DEFAULT_FROM_COORD = {
    lat: 35.7011925028703,
    lng: 139.7331762313843,
};

exports.DEFAULT_FROM_COORD = DEFAULT_FROM_COORD;

const DEFAULT_TO_COORD = {
    lat: 35.69709353668848,
    lng: 139.74475423805418
};
exports.DEFAULT_TO_COORD = DEFAULT_TO_COORD;

const DEFAULT_MAP_URL =
    `/map/${DEFAULT_FROM_COORD.lat},${DEFAULT_FROM_COORD.lng}/${DEFAULT_TO_COORD.lat},${DEFAULT_TO_COORD.lng}/NO_RIGHT_TURNS`;
exports.DEFAULT_MAP_URL = DEFAULT_MAP_URL;
