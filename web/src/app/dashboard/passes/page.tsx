'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import PassCard from '@/components/PassCard'
import type { Pass } from '@/lib/types'

export default function PassesPage() {
  const supabase = createClient()
  const [passes, setPasses] = useState<Pass[]>([])
  const [loading, setLoading] = useState(true)
  const [showAddForm, setShowAddForm] = useState(false)
  const [formData, setFormData] = useState({
    pass_id: '',
    pass_name: '',
    organization: '',
    link: '',
    expiry_date: '',
  })

  useEffect(() => {
    fetchPasses()
  }, [])

  const fetchPasses = async () => {
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    const { data } = await supabase
      .from('passes')
      .select('*')
      .eq('user_id', user.id)
      .order('created_at', { ascending: false })

    setPasses(data || [])
    setLoading(false)
  }

  const handleAddPass = async (e: React.FormEvent) => {
    e.preventDefault()
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    const { error } = await supabase.from('passes').insert({
      user_id: user.id,
      pass_id: formData.pass_id,
      pass_name: formData.pass_name,
      organization: formData.organization,
      link: formData.link || null,
      expiry_date: formData.expiry_date || null,
    })

    if (!error) {
      setFormData({ pass_id: '', pass_name: '', organization: '', link: '', expiry_date: '' })
      setShowAddForm(false)
      fetchPasses()
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this pass?')) return

    const { error } = await supabase.from('passes').delete().eq('id', id)
    if (!error) {
      setPasses(passes.filter(p => p.id !== id))
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div></div>
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Passes</h1>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
        >
          {showAddForm ? 'Cancel' : 'Add Pass'}
        </button>
      </div>

      {showAddForm && (
        <form onSubmit={handleAddPass} className="bg-white rounded-xl border border-gray-200 p-5 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pass ID *</label>
              <input
                type="text"
                required
                value={formData.pass_id}
                onChange={e => setFormData({...formData, pass_id: e.target.value})}
                placeholder="e.g., ABC123456"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pass Name *</label>
              <input
                type="text"
                required
                value={formData.pass_name}
                onChange={e => setFormData({...formData, pass_name: e.target.value})}
                placeholder="e.g., Gym Membership"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Organization *</label>
              <input
                type="text"
                required
                value={formData.organization}
                onChange={e => setFormData({...formData, organization: e.target.value})}
                placeholder="e.g., Gold's Gym"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Link (Optional)</label>
              <input
                type="url"
                value={formData.link}
                onChange={e => setFormData({...formData, link: e.target.value})}
                placeholder="https://example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Expiry Date (Optional)</label>
              <input
                type="date"
                value={formData.expiry_date}
                onChange={e => setFormData({...formData, expiry_date: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          <button
            type="submit"
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
          >
            Add Pass
          </button>
        </form>
      )}

      {passes.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-gray-200">
          <svg className="w-12 h-12 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
          </svg>
          <p className="text-gray-500 mt-3">No passes yet</p>
          <p className="text-gray-400 text-sm mt-1">Add a pass from the Android app or click &quot;Add Pass&quot; above</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {passes.map(pass => (
            <PassCard key={pass.id} pass={pass} onDelete={handleDelete} />
          ))}
        </div>
      )}
    </div>
  )
}
