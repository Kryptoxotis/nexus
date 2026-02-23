'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Business } from '@/lib/types'

export default function BusinessPage() {
  const supabase = createClient()
  const [business, setBusiness] = useState<Business | null>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
  })

  useEffect(() => {
    fetchBusiness()
  }, [])

  const fetchBusiness = async () => {
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    const { data } = await supabase
      .from('businesses')
      .select('*')
      .eq('owner_id', user.id)
      .single()

    if (data) {
      setBusiness(data)
      setFormData({
        name: data.name,
        description: data.description || '',
        category: data.category || '',
      })
    }
    setLoading(false)
  }

  const handleCreateBusiness = async (e: React.FormEvent) => {
    e.preventDefault()
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    const { data, error } = await supabase.from('businesses').insert({
      owner_id: user.id,
      name: formData.name,
      description: formData.description || null,
      category: formData.category || null,
    }).select().single()

    if (!error && data) {
      // Also add owner as business member
      await supabase.from('business_members').insert({
        business_id: data.id,
        user_id: user.id,
        role: 'owner',
        status: 'active',
      })
      setBusiness(data)
    }
  }

  const handleUpdateBusiness = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!business) return

    const { error } = await supabase.from('businesses')
      .update({
        name: formData.name,
        description: formData.description || null,
        category: formData.category || null,
        updated_at: new Date().toISOString(),
      })
      .eq('id', business.id)

    if (!error) {
      setBusiness({ ...business, ...formData } as Business)
      setEditing(false)
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div></div>
  }

  if (!business) {
    return (
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Create Your Business</h1>
        <form onSubmit={handleCreateBusiness} className="bg-white rounded-xl border border-gray-200 p-6 max-w-lg">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Business Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={e => setFormData({...formData, name: e.target.value})}
                placeholder="e.g., Gold's Gym Downtown"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                value={formData.description}
                onChange={e => setFormData({...formData, description: e.target.value})}
                placeholder="Tell people about your business"
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <input
                type="text"
                value={formData.category}
                onChange={e => setFormData({...formData, category: e.target.value})}
                placeholder="e.g., Fitness, Office, Coworking"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          <button
            type="submit"
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
          >
            Create Business
          </button>
        </form>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Business</h1>
        <button
          onClick={() => setEditing(!editing)}
          className="px-4 py-2 text-sm font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          {editing ? 'Cancel' : 'Edit'}
        </button>
      </div>

      {editing ? (
        <form onSubmit={handleUpdateBusiness} className="bg-white rounded-xl border border-gray-200 p-6 max-w-lg">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Business Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={e => setFormData({...formData, name: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                value={formData.description}
                onChange={e => setFormData({...formData, description: e.target.value})}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <input
                type="text"
                value={formData.category}
                onChange={e => setFormData({...formData, category: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          <button
            type="submit"
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
          >
            Save Changes
          </button>
        </form>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 bg-blue-100 rounded-xl flex items-center justify-center">
              <span className="text-2xl font-bold text-blue-600">
                {business.name.charAt(0).toUpperCase()}
              </span>
            </div>
            <div>
              <h2 className="text-xl font-semibold text-gray-900">{business.name}</h2>
              {business.description && (
                <p className="text-gray-600 mt-1">{business.description}</p>
              )}
              {business.category && (
                <span className="inline-block mt-2 px-3 py-1 text-sm font-medium rounded-full bg-gray-100 text-gray-600">
                  {business.category}
                </span>
              )}
              <p className="text-xs text-gray-400 mt-3">
                Created: {new Date(business.created_at).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
