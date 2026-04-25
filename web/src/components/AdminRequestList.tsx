'use client'

import type { BusinessRequest } from '@/lib/types'
import { createClient, nexus } from '@/lib/supabase/client'
import { useState } from 'react'

interface Props {
  requests: BusinessRequest[]
}

export default function AdminRequestList({ requests: initial }: Props) {
  const [requests, setRequests] = useState(initial)

  const updateRequest = async (id: string, status: 'approved' | 'rejected') => {
    const supabase = createClient()
    const db = nexus(supabase)
    await db.from('business_requests').update({ status }).eq('id', id)
    setRequests(prev => prev.map(r => r.id === id ? { ...r, status } : r))
  }

  const pending = requests.filter(r => r.status === 'pending')
  if (pending.length === 0) return null

  return (
    <div>
      <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">Business Requests</h2>
      <div className="space-y-2">
        {pending.map(req => (
          <div key={req.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4">
            <div className="flex items-start justify-between mb-3">
              <div>
                <p className="text-white font-medium text-sm">{req.business_name}</p>
                <p className="text-[#666666] text-xs mt-0.5">{req.contact_email}</p>
                {req.message && <p className="text-[#555555] text-xs mt-1">{req.message}</p>}
              </div>
              <span className="text-[#FA5700] text-xs font-medium bg-[#FA5700]/10 px-2 py-0.5 rounded-full">Pending</span>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => updateRequest(req.id, 'approved')}
                className="flex-1 py-2 rounded-xl bg-[#037A68] text-white text-xs font-semibold hover:bg-[#025E50] transition-colors"
              >
                Approve
              </button>
              <button
                onClick={() => updateRequest(req.id, 'rejected')}
                className="flex-1 py-2 rounded-xl bg-[#383838] text-[#EEEEEE] text-xs font-semibold hover:bg-[#444444] transition-colors"
              >
                Reject
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
