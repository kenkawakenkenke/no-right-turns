const { DEFAULT_MAP_URL } = require("./src/components/global.js");

module.exports = {
    // async rewrites() {
    //     return [
    //         {
    //             source: '/',
    //             destination: '/map/35.699728912010656,139.72915375605228/35.6952720844853,139.73539701662958',
    //         },
    //     ]
    // },
    async redirects() {
        return [
            {
                source: '/',
                destination: DEFAULT_MAP_URL,
                // destination: '/map/35.699728912010656,139.72915375605228/35.6952720844853,139.73539701662958',
                permanent: true,
            },
        ]
    },
};
