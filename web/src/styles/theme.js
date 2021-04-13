import { createMuiTheme } from '@material-ui/core/styles';
import { red } from '@material-ui/core/colors';

// Create a theme instance.
const theme = createMuiTheme({
    typography: {
        fontFamily: [
            "Noto Sans JP",
            "Lato",
            "游ゴシック Medium",
            "游ゴシック体",
            "Yu Gothic Medium",
            "YuGothic",
            "ヒラギノ角ゴ ProN",
            "Hiragino Kaku Gothic ProN",
            "メイリオ",
            "Meiryo",
            "ＭＳ Ｐゴシック",
            "MS PGothic",
            "sans-serif",
        ].join(","),
    },
    palette: {
        primary: {
            main: '#95D193',
        },
        secondary: {
            main: '#EB3131',
        },
        error: {
            main: red.A400,
        },
        background: {
            default: '#fff',
        },
    },
    overrides: {
        "MuiLink": {
            "underlineHover": {
                color: "#665555",
                // backgroundColor: "yellow",
                // color: "blue",
                textDecorationLine: "underline",
                textDecorationColor: "#dddddd",
                textDecorationThickness: "1px",
                textDecorationStyle: "dashed",

                '&:focus, &:hover, &:active': {
                    textDecoration: "underline",
                    textDecorationLine: "underline",
                    textDecorationColor: "#EB3131",
                    textDecorationThickness: "4px",
                    textDecorationStyle: "dashed",
                    //     backgroundColor: "red",
                    // boxShadow: '#999 0 2px 3px 1px',
                }
            },
        }
    }
});

export default theme;
