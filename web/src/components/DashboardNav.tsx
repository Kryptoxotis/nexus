'use client'

import type { Profile } from '@/lib/types'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { createClient } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'

interface Props {
  profile: Profile
}

export default function DashboardNav({ profile }: Props) {
  const pathname = usePathname()
  const router = useRouter()
  const supabase = createClient()

  const signOut = async () => {
    await supabase.auth.signOut()
    router.push('/')
  }

  const isAdmin = profile.account_type === 'admin'
  const isBusiness = profile.account_type === 'business' || isAdmin

  return (
    <header className="border-b border-[#1A1A1A] bg-[#0A0A0A]">
      <div className="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-5">
          <Link href="/dashboard" className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-[#037A68] flex items-center justify-center">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.9"/>
                <rect x="13" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
                <rect x="3" y="13" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
                <circle cx="17" cy="17" r="3" fill="#FA5700"/>
              </svg>
            </div>
            <span className="text-white font-semibold text-sm">Nexus</span>
          </Link>

          <nav className="flex items-center gap-1">
            <Link
              href="/dashboard"
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                pathname === '/dashboard' ? 'bg-[#1A1A1A] text-white' : 'text-[#666666] hover:text-white'
              }`}
            >
              Cards
            </Link>
            {isBusiness && (
              <Link
                href="/dashboard/business"
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  pathname.startsWith('/dashboard/business') ? 'bg-[#1A1A1A] text-white' : 'text-[#666666] hover:text-white'
                }`}
              >
                Business
              </Link>
            )}
            {isAdmin && (
              <Link
                href="/dashboard/admin"
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  pathname.startsWith('/dashboard/admin') ? 'bg-[#1A1A1A] text-white' : 'text-[#666666] hover:text-white'
                }`}
              >
                Admin
              </Link>
            )}
          </nav>
        </div>

        <button
          onClick={signOut}
          className="text-xs text-[#444444] hover:text-[#666666] transition-colors"
        >
          Sign out
        </button>
      </div>
    </header>
  )
}
