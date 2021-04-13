// import '../styles/globals.css'
import React from 'react'
import { makeStyles, ThemeProvider } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import theme from '../styles/theme.js';

const useStyles = makeStyles((theme) => ({
  root: {
    height: "100%",
    // margin: "4px",
    // "& a": {
    //   color: "#665555",
    //   '&:focus, &:hover, &:active': {
    //     textDecoration: "none",
    //     //     backgroundColor: "red",
    //     // boxShadow: '#999 0 2px 3px 1px',
    //   }
    // },
    // "& a": {
    //   color: "#665555",
    //   textDecorationLine: "underline",
    //   textDecorationColor: "#dddddd",
    //   textDecorationThickness: "1px",
    //   // textDecorationStyle: "dashed",

    //   '&:focus, &:hover, &:active': {
    //     textDecoration: "underline",
    //     textDecorationLine: "underline",
    //     textDecorationColor: "#EB3131",
    //     textDecorationThickness: "4px",
    //     textDecorationStyle: "dashed",
    //     //     backgroundColor: "red",
    //     // boxShadow: '#999 0 2px 3px 1px',
    //   }
    // },
  },
}));

function Root({ children }) {
  const classes = useStyles();
  return <>
    <div className={classes.root}>
      {children}
    </div>
  </>;
}

const App = ({ Component, pageProps }) => {
  React.useEffect(() => {
    const jssStyles = document.querySelector('#jss-server-side');
    if (jssStyles) {
      jssStyles.parentElement.removeChild(jssStyles);
    }
  }, []);

  return <div>
    <ThemeProvider theme={theme}>
      <CssBaseline />

      {/* <ToastContextProvider> */}
      {/* <ConfirmDialogContextProvider> */}

      <Root>
        <Component {...pageProps} />
      </Root>

      {/* </ConfirmDialogContextProvider> */}
      {/* </ToastContextProvider> */}

    </ThemeProvider>
  </div>;
}

export default App