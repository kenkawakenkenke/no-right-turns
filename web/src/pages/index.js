import Head from 'next/head'
import dynamic from 'next/dynamic';
import { useRouter } from 'next/router';

export default function Home() {
  return <div>This shouldn't show</div>
}

// export async function getStaticProps(context) {
//   return {
//     redirect: {
//       destination: '/map/35.699728912010656,139.72915375605228/35.6952720844853,139.73539701662958',
//       permanent: true,
//     },
//   }
// }
