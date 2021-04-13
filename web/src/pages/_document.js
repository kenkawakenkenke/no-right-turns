// https://github.com/mui-org/material-ui/blob/master/examples/nextjs/pages/_document.js
import React from 'react';
import Document, { Html, Head, Main, NextScript } from 'next/document';
import { ServerStyleSheets } from '@material-ui/core/styles';
import theme from '../styles/theme';
class MyDocument extends Document {
    static async getInitialProps(ctx) {
        const initialProps = await Document.getInitialProps(ctx)
        return { ...initialProps }
    }

    render() {
        return (
            <Html>
                <Head>

                    <meta name="description" content="なるべく右折せずに目的地まで到達できる経路を検索できる、新しいナビです" />
                    <meta property="og:url" content="http://no-right-turns.skip.work/" />
                    <meta property="og:type" content="website" />
                    <meta property="og:title" content="絶対右折したくない検索" />
                    <meta property="og:description" content="なるべく右折せずに目的地まで到達できる経路を検索できる、新しいナビです" />
                    <meta property="og:site_name" content="絶対右折したくない検索" />
                    <meta property="og:image" content="https://no-right-turns.skip.work/norightturn_scrnshot.jpg" />
                    <meta name="twitter:card" content="summary_large_image" />
                    <meta name="twitter:site" content="@kenkawakenkenke" />

                    <link rel="apple-touch-icon" sizes="57x57" href="/apple-icon-57x57.png" />
                    <link rel="apple-touch-icon" sizes="60x60" href="/apple-icon-60x60.png" />
                    <link rel="apple-touch-icon" sizes="72x72" href="/apple-icon-72x72.png" />
                    <link rel="apple-touch-icon" sizes="76x76" href="/apple-icon-76x76.png" />
                    <link rel="apple-touch-icon" sizes="114x114" href="/apple-icon-114x114.png" />
                    <link rel="apple-touch-icon" sizes="120x120" href="/apple-icon-120x120.png" />
                    <link rel="apple-touch-icon" sizes="144x144" href="/apple-icon-144x144.png" />
                    <link rel="apple-touch-icon" sizes="152x152" href="/apple-icon-152x152.png" />
                    <link rel="apple-touch-icon" sizes="180x180" href="/apple-icon-180x180.png" />
                    <link rel="icon" type="image/png" sizes="192x192" href="/android-icon-192x192.png" />
                    <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png" />
                    <link rel="icon" type="image/png" sizes="96x96" href="/favicon-96x96.png" />
                    <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png" />
                    <link rel="manifest" href="/manifest.json" />
                    <meta name="msapplication-TileColor" content="#ffffff" />
                    <meta name="msapplication-TileImage" content="/ms-icon-144x144.png" />
                    <meta name="theme-color" content="#ffffff" />
                    {/* PWA primary color */}
                    <meta name="theme-color" content={theme.palette.primary.main} />
                    <link
                        rel="stylesheet"
                        href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap"
                    />
                    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Noto+Sans+JP&subset=japanese" />
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.3.1/leaflet.css" />
                    <link href="https://unpkg.com/leaflet-geosearch@latest/assets/css/leaflet.css" rel="stylesheet" />

                    <link rel="stylesheet" href="/app.css" />
                    {/* <style global jsx>{`
                    html,
                    body,
                    body > div:first-child,
                    div#__next,
                    div#__next > div {
                        height: 100%;
                    }
                    `} */}
                    {/* </style> */}
                </Head>
                <body>
                    <Main />
                    <NextScript />
                </body>
            </Html >
        )
    }
}

export default MyDocument;


// `getInitialProps` belongs to `_document` (instead of `_app`),
// it's compatible with server-side generation (SSG).
MyDocument.getInitialProps = async (ctx) => {
    // Resolution order
    //
    // On the server:
    // 1. app.getInitialProps
    // 2. page.getInitialProps
    // 3. document.getInitialProps
    // 4. app.render
    // 5. page.render
    // 6. document.render
    //
    // On the server with error:
    // 1. document.getInitialProps
    // 2. app.render
    // 3. page.render
    // 4. document.render
    //
    // On the client
    // 1. app.getInitialProps
    // 2. page.getInitialProps
    // 3. app.render
    // 4. page.render

    // Render app and page and get the context of the page with collected side effects.
    const sheets = new ServerStyleSheets();
    const originalRenderPage = ctx.renderPage;

    ctx.renderPage = () =>
        originalRenderPage({
            enhanceApp: (App) => (props) => sheets.collect(<App {...props} />),
        });

    const initialProps = await Document.getInitialProps(ctx);

    return {
        ...initialProps,
        // Styles fragment is rendered after the app and page rendering finish.
        styles: [...React.Children.toArray(initialProps.styles), sheets.getStyleElement()],
    };
};