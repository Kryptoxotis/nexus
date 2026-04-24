import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Nexus - Your Digital Pass Wallet',
  description: 'Share your contact info, social media, and memberships with one tap.',
  manifest: '/manifest.json',
  themeColor: '#0A0A0A',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'Nexus',
  },
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={inter.className}>{children}</body>
    </html>
  )
}
