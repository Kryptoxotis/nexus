'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import MemberTable from '@/components/MemberTable'
import type { BusinessMember } from '@/lib/types'

export default function MembersPage() {
  const supabase = createClient()
  const [members, setMembers] = useState<(BusinessMember & { profile?: { display_name: string | null; email: string | null } })[]>([])
  const [loading, setLoading] = useState(true)
  const [businessId, setBusinessId] = useState<string | null>(null)

  useEffect(() => {
    fetchMembers()
  }, [])

  const fetchMembers = async () => {
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) return

    // Get business owned by user
    const { data: business } = await supabase
      .from('businesses')
      .select('id')
      .eq('owner_id', user.id)
      .single()

    if (!business) {
      setLoading(false)
      return
    }

    setBusinessId(business.id)

    // Get members with their profiles
    const { data } = await supabase
      .from('business_members')
      .select(`
        *,
        profile:profiles!business_members_user_id_fkey(display_name, email)
      `)
      .eq('business_id', business.id)
      .order('joined_at', { ascending: true })

    setMembers(data || [])
    setLoading(false)
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div></div>
  }

  if (!businessId) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">You need to create a business first.</p>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Members</h1>
        <span className="text-sm text-gray-500">{members.length} total</span>
      </div>

      <div className="bg-white rounded-xl border border-gray-200">
        <MemberTable members={members} />
      </div>
    </div>
  )
}
