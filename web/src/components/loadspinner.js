import React, { createContext, useState, useContext } from "react";
import { makeStyles } from '@material-ui/core/styles';
import Loader from "react-loader-spinner";

const useStyles = makeStyles((theme) => ({
    spinnerOverlayView: {
        position: "absolute",
        top: "0px",
        left: "0px",
        right: "0px",
        bottom: "0px",
        display: "flex",
        justifyContent: "center",
        // backgroundColor: "black",
        opacity: "1",
        // backgroundColor: "red",
        // width: "100%",
        // height: "100%",
    },
    spinner: {
        position: "absolute",
        width: "100px",
        // left: "50%",
        // margin: "0 auto",
        top: "50%",
    }
}));

const SpinnerContext = createContext();

export const useSpinner = () => useContext(SpinnerContext);

export const SpinnerContextProvider = ({ children }) => {
    const classes = useStyles();
    const [showSpinner, setShowSpinner] = useState(false);

    return <SpinnerContext.Provider value={setShowSpinner}>
        {children}

        {showSpinner &&
            <div className={classes.spinnerOverlayView}>
                <Loader
                    className={classes.spinner}
                    type="ThreeDots"
                    color="#222222"
                    height={100}
                    width={100}
                />
            </div>}

    </SpinnerContext.Provider >;
}
