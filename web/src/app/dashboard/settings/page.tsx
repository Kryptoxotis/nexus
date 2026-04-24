'use client'

import { createClient } from '@/lib/supabase/client'
import { nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import type { Profile } from '@/lib/types'

export default function SettingsPage() {
  const supabase = createClient()
  const router = useRouter()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [name, setName] = useState('')
  const [saving, setSaving] = useState(false)
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

  const save = async () => {
    if (!profile) return
    setSaving(true)
    const db = nexus(supabase)
    await db.from('profiles').update({ full_name: name.trim() }).eq('id', profile.id)
    setProfile({ ...profile, full_name: name.trim() })
    setSaving(false)
  }

  if (loading) return (
    <div className="flex items-center justify-center h-40">
      <div className="w-6 h-6 border-2 border-[#037A68] border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Settings</h1>
      </div>

      <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-5 space-y-4">
        <h2 className="text-white font-semibold text-sm">Profile</h2>
        <div>
          <label className="block text-xs text-[#666666] mb-1.5">Email</label>
          <p className="text-[#555555] text-sm">{profile?.email}</p>
        </div>
        <div>
          <label className="block text-xs text-[#666666] mb-1.5">Display Name</label>
          <div className="flex gap-2">
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              className="flex-1 bg-[#111111] border border-[#383838] rounded-xl px-3 py-2.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
            />
            <button
              onClick={save}
              disabled={saving || name === profile?.full_name}
              className="px-4 py-2 rounded-xl bg-[#037A68] hover:bg-[#025E50] text-white text-sm font-medium transition-colors disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </div>
        <div>
          <label className="block text-xs text-[#666666] mb-1">Account Type</label>
          <span className="text-[#037A68] text-sm font-medium capitalize">{profile?.account_type}</span>
        </div>
      </div>
    </div>
  )
}
