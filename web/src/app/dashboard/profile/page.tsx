'use client'

import { createClient, nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import type { Profile } from '@/lib/types'

export default function ProfilePage() {
  const supabase = createClient()
  const router = useRouter()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [name, setName] = useState('')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) { router.push('/'); return }
      const db = nexus(supabase)
      const { data } = await db.from('profiles').select('*').eq('id', user.id).single<Profile>()
      if (data) { setProfile(data); setName(data.full_name ?? '') }
      setLoading(false)
    }
    load()
  }, [])

  const saveName = async () => {
    if (!profile) return
    setSaving(true)
    const db = nexus(supabase)
    await db.from('profiles').update({ full_name: name.trim() }).eq('id', profile.id)
    setProfile({ ...profile, full_name: name.trim() })
    setSaving(false)
    setSaved(true)
    setTimeout(() => setSaved(false), 2000)
  }

  const signOut = async () => {
    await supabase.auth.signOut()
    router.push('/')
  }

  if (loading) return (
    <div className="flex items-center justify-center h-40">
      <div className="w-6 h-6 border-2 border-[#037A68] border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="space-y-4 pb-8">
      <h2 className="text-xl font-bold text-white">Accounts</h2>

      <div className="bg-[#037A68] rounded-2xl px-4 py-4 flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center flex-shrink-0">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-white font-bold text-sm truncate">{profile?.full_name || 'User'}</p>
          <p className="text-white/70 text-xs truncate">{profile?.email}</p>
        </div>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
          <path d="M20 6L9 17l-5-5"/>
        </svg>
      </div>

      <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4 space-y-3">
        <p className="text-white font-semibold text-sm">Profile</p>
        <div>
          <p className="text-[#666666] text-xs mb-1.5">Display Name</p>
          <div className="flex gap-2">
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              className="flex-1 bg-[#111111] border border-[#383838] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#037A68] transition-colors"
            />
            <button
              onClick={saveName}
              disabled={saving || name === profile?.full_name}
              className="px-4 py-2 rounded-xl bg-[#037A68] hover:bg-[#025E50] text-white text-sm font-semibold transition-colors disabled:opacity-40"
            >
              {saved ? '✓' : saving ? '...' : 'Save'}
            </button>
          </div>
        </div>
        <div>
          <p className="text-[#666666] text-xs mb-0.5">Account Type</p>
          <p className="text-[#037A68] text-sm font-medium capitalize">{profile?.account_type}</p>
        </div>
      </div>

      <button
        onClick={signOut}
        className="flex items-center justify-center gap-2 w-full py-3.5 rounded-2xl text-[#FA5700] text-sm font-semibold border border-[#FA5700]/20 hover:bg-[#FA5700]/10 transition-colors"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Sign Out
      </button>

      <p className="text-center text-[#333333] text-xs">Version 1.0.0</p>
    </div>
  )
}
