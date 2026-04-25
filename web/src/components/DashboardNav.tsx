'use client'

import type { Profile } from '@/lib/types'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

interface Props {
  profile: Profile
}

export default function DashboardNav({ profile }: Props) {
  const pathname = usePathname()
  const isAdmin = profile.account_type === 'admin'

  const NavIcon = ({ href, active, children, label }: {
    href: string; active: boolean; children: React.ReactNode; label: string
  }) => (
    <Link
      href={href}
      aria-label={label}
      className="w-11 h-11 rounded-full flex items-center justify-center transition-colors"
      style={{ background: active ? '#2A2A2A' : '#1A1A1A' }}
    >
      {children}
    </Link>
  )

  const iconColor = '#CCCCCC'

  return (
    <header className="px-4 pt-4 pb-2">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white leading-tight">Nexus</h1>
          <p className="text-[#037A68] text-sm font-medium capitalize leading-tight">{profile.account_type}</p>
        </div>

        <div className="flex items-center gap-2">
          {/* Nexus/Contacts icon */}
          <NavIcon href="/dashboard/nexus" active={pathname.startsWith('/dashboard/nexus')} label="My Nexus">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={iconColor} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <line x1="2" y1="10" x2="22" y2="10"/>
            </svg>
          </NavIcon>

          {/* Business passes icon */}
          <NavIcon href="/dashboard/business" active={pathname.startsWith('/dashboard/business')} label="Business">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={iconColor} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="7" width="20" height="14" rx="2"/>
              <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
            </svg>
          </NavIcon>

          {/* Admin icon - only for admins */}
          {isAdmin && (
            <NavIcon href="/dashboard/admin" active={pathname.startsWith('/dashboard/admin')} label="Admin">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={iconColor} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </NavIcon>
          )}

          {/* Profile/Account icon */}
          <NavIcon href="/dashboard/profile" active={pathname === '/dashboard/profile'} label="Account">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={iconColor} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </NavIcon>
        </div>
      </div>
    </header>
  )
}
