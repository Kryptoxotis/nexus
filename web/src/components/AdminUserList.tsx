'use client'

import type { Profile } from '@/lib/types'
import { createClient, nexus } from '@/lib/supabase/client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'

interface Props {
  profiles: Profile[]
  currentUserId: string
}

export default function AdminUserList({ profiles: initial, currentUserId }: Props) {
  const [profiles, setProfiles] = useState(initial)
  const [deleting, setDeleting] = useState<string | null>(null)
  const router = useRouter()

  const deleteUser = async (id: string) => {
    if (!confirm('Delete this user and all their data?')) return
    setDeleting(id)
    const supabase = createClient()
    const db = nexus(supabase)

    // Delete all user's cards, then profile (auth user is cascade deleted by DB trigger)
    await db.from('personal_cards').delete().eq('user_id', id)
    await db.from('business_requests').delete().eq('user_id', id)
    await db.from('profiles').delete().eq('id', id)

    setProfiles(prev => prev.filter(p => p.id !== id))
    setDeleting(null)
    router.refresh()
  }

  const updateAccountType = async (id: string, type: Profile['account_type']) => {
    const supabase = createClient()
    const db = nexus(supabase)
    await db.from('profiles').update({ account_type: type }).eq('id', id)
    setProfiles(prev => prev.map(p => p.id === id ? { ...p, account_type: type } : p))
  }

  return (
    <div>
      <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">Users ({profiles.length})</h2>
      <div className="space-y-2">
        {profiles.map(p => (
          <div key={p.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#037A68]/20 flex items-center justify-center flex-shrink-0">
                <span className="text-[#037A68] text-xs font-bold">
                  {(p.full_name ?? p.email ?? '?')[0].toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-sm font-medium truncate">{p.full_name ?? 'Unknown'}</p>
                <p className="text-[#444444] text-xs truncate">{p.email}</p>
              </div>
              <select
                value={p.account_type}
                onChange={e => updateAccountType(p.id, e.target.value as Profile['account_type'])}
                disabled={p.id === currentUserId}
                className="bg-[#111111] border border-[#383838] rounded-lg px-2 py-1 text-xs text-[#EEEEEE] focus:outline-none disabled:opacity-40"
              >
                <option value="individual">Individual</option>
                <option value="business">Business</option>
                <option value="admin">Admin</option>
              </select>
              {p.id !== currentUserId && (
                <button
                  onClick={() => deleteUser(p.id)}
                  disabled={deleting === p.id}
                  className="w-7 h-7 rounded-lg flex items-center justify-center text-[#666666] hover:text-red-400 hover:bg-red-400/10 transition-colors disabled:opacity-40"
                >
                  {deleting === p.id ? (
                    <div className="w-3 h-3 border border-current border-t-transparent rounded-full animate-spin" />
                  ) : (
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                      <path d="M10 11v6M14 11v6"/>
                      <path d="M9 6V4h6v2"/>
                    </svg>
                  )}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
