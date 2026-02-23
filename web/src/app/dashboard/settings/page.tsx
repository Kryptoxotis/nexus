'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import type { Profile } from '@/lib/types'

export default function SettingsPage() {
  const supabase = createClient()
  const router = useRouter()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [displayName, setDisplayName] = useState('')

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    const { data } = await supabase
      .from('profiles')
      .select('*')
      .eq('user_id', user.id)
      .single<Profile>()

    if (data) {
      setProfile(data)
      setDisplayName(data.display_name || '')
    }
    setLoading(false)
  }

  const handleUpdateName = async () => {
    if (!profile) return
    setSaving(true)

    const { error } = await supabase
      .from('profiles')
      .update({
        display_name: displayName,
        updated_at: new Date().toISOString(),
      })
      .eq('user_id', profile.user_id)

    if (!error) {
      setProfile({ ...profile, display_name: displayName })
    }
    setSaving(false)
  }

  const handleSwitchRole = async (role: 'personal' | 'business') => {
    if (!profile) return

    const { error } = await supabase
      .from('profiles')
      .update({
        current_role: role,
        updated_at: new Date().toISOString(),
      })
      .eq('user_id', profile.user_id)

    if (!error) {
      setProfile({ ...profile, current_role: role })
      router.refresh()
    }
  }

  const handleSignOut = async () => {
    await supabase.auth.signOut()
    router.push('/')
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div></div>
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

      {/* Profile Section */}
      <section className="bg-white rounded-xl border border-gray-200 p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Profile</h2>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <p className="text-sm text-gray-600">{profile?.email}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Display Name</label>
            <div className="flex gap-2">
              <input
                type="text"
                value={displayName}
                onChange={e => setDisplayName(e.target.value)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
              <button
                onClick={handleUpdateName}
                disabled={saving || displayName === profile?.display_name}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Role Section */}
      <section className="bg-white rounded-xl border border-gray-200 p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Account Type</h2>
        <div className="grid grid-cols-2 gap-4">
          <button
            onClick={() => handleSwitchRole('personal')}
            className={`p-4 rounded-xl border-2 text-left transition-colors ${
              profile?.current_role === 'personal'
                ? 'border-blue-500 bg-blue-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <p className="font-semibold text-gray-900">Personal</p>
            <p className="text-sm text-gray-500 mt-1">Use and manage your own passes</p>
          </button>
          <button
            onClick={() => handleSwitchRole('business')}
            className={`p-4 rounded-xl border-2 text-left transition-colors ${
              profile?.current_role === 'business'
                ? 'border-blue-500 bg-blue-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <p className="font-semibold text-gray-900">Business</p>
            <p className="text-sm text-gray-500 mt-1">Issue passes for your organization</p>
          </button>
        </div>
      </section>

      {/* Danger Zone */}
      <section className="bg-white rounded-xl border border-red-200 p-6">
        <h2 className="text-lg font-semibold text-red-600 mb-4">Sign Out</h2>
        <p className="text-sm text-gray-600 mb-4">Sign out from the web dashboard. Your passes will remain on your phone.</p>
        <button
          onClick={handleSignOut}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
        >
          Sign Out
        </button>
      </section>
    </div>
  )
}
