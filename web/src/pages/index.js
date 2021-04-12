import Head from 'next/head'
import dynamic from 'next/dynamic';

// import styles from '../styles/Home.module.css'

const MapWithNoSSR = dynamic(() => import('../components/map'), {
  ssr: false
});

export default function Home() {
  return (
    <div>
      <Head>
        <title>Create Next App</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main>

        Hey
        <MapWithNoSSR />
      </main>
    </div>
  )
}
