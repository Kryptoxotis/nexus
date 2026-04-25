'use client'

import type { Profile } from '@/lib/types'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { createClient } from '@/lib/supabase/client'

interface Props {
  profile: Profile
}

export default function DashboardNav({ profile }: Props) {
  const pathname = usePathname()
  const router = useRouter()
  const supabase = createClient()
  const isAdmin = profile.account_type === 'admin'
  const isBusiness = profile.account_type === 'business' || isAdmin

  const signOut = async () => {
    await supabase.auth.signOut()
    router.push('/')
  }

  const NavIcon = ({ href, active, children }: { href: string; active: boolean; children: React.ReactNode }) => (
    <Link
      href={href}
      className="w-11 h-11 rounded-full flex items-center justify-center transition-colors"
      style={{ background: active ? '#2A2A2A' : '#1A1A1A' }}
    >
      {children}
    </Link>
  )

  return (
    <header className="px-4 pt-4 pb-2">
      <div className="flex items-start justify-between">
        <button onClick={signOut} className="text-left">
          <h1 className="text-2xl font-bold text-white leading-tight">Nexus</h1>
          <p className="text-[#037A68] text-sm font-medium capitalize leading-tight">{profile.account_type}</p>
        </button>

        <div className="flex items-center gap-2">
          {isBusiness && (
            <NavIcon href="/dashboard/business" active={pathname.startsWith('/dashboard/business')}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#EEEEEE" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </NavIcon>
          )}
          {isAdmin && (
            <NavIcon href="/dashboard/admin" active={pathname.startsWith('/dashboard/admin')}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#EEEEEE" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
              </svg>
            </NavIcon>
          )}
          <NavIcon href="/dashboard/nexus" active={pathname.startsWith('/dashboard/nexus')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#EEEEEE" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <line x1="2" y1="10" x2="22" y2="10"/>
            </svg>
          </NavIcon>
          <NavIcon href="/dashboard/profile" active={pathname === '/dashboard/profile'}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#EEEEEE" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </NavIcon>
        </div>
      </div>
    </header>
  )
}
