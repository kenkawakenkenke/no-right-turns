const { DEFAULT_MAP_URL } = require("./src/components/global.js");

module.exports = {
    async redirects() {
        return [
            {
                source: '/',
                destination: DEFAULT_MAP_URL,
                permanent: true,
            },
            {
                source: '/map/:from/:to',
                destination: DEFAULT_MAP_URL,
                permanent: false,
            },
        ]
    },
};
