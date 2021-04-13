// import '../styles/globals.css'
import React from 'react'
import { makeStyles, ThemeProvider } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import theme from '../styles/theme.js';

const useStyles = makeStyles((theme) => ({
  root: {
    height: "100%",
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
    {/* <ThemeProvider theme={theme}>
      <CssBaseline /> */}

    {/* <SpinnerContextProvider> */}
    {/* <ToastContextProvider> */}
    {/* <ConfirmDialogContextProvider> */}

    {/* <Root> */}
    <Component {...pageProps} />
    {/* </Root> */}

    {/* </SpinnerContextProvider> */}

    {/* </ConfirmDialogContextProvider> */}
    {/* </ToastContextProvider> */}

    {/* </ThemeProvider> */}
  </div>;
}

export default App