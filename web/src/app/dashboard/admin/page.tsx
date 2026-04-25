export const dynamic = 'force-dynamic'

import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, BusinessRequest } from '@/lib/types'
import AdminUserList from '@/components/AdminUserList'
import AdminRequestList from '@/components/AdminRequestList'

export default async function AdminPage() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)

  const { data: profile } = await db
    .from('profiles')
    .select('account_type')
    .eq('id', user.id)
    .single<Pick<Profile, 'account_type'>>()

  if (profile?.account_type !== 'admin') redirect('/dashboard')

  const [{ data: profiles }, { data: requests }, { data: orgs }] = await Promise.all([
    db.from('profiles').select('*').order('created_at', { ascending: false }).limit(100),
    db.from('business_requests').select('*').order('created_at', { ascending: false }),
    db.from('organizations').select('id').limit(100),
  ])

  return (
    <div className="space-y-6 pb-8">
      <div>
        <h1 className="text-xl font-bold text-white">Admin</h1>
        <p className="text-[#037A68] text-sm mt-0.5">System overview</p>
      </div>

      <div className="grid grid-cols-3 gap-3">
        {[
          { label: 'Users',   value: profiles?.length ?? 0, color: 'text-[#FA5700]' },
          { label: 'Orgs',    value: orgs?.length ?? 0,     color: 'text-[#037A68]' },
          { label: 'Pending', value: (requests as BusinessRequest[] ?? []).filter(r => r.status === 'pending').length, color: 'text-[#FA5700]' },
        ].map(s => (
          <div key={s.label} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4 text-center">
            <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
            <p className="text-[#666666] text-xs mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      <AdminRequestList requests={(requests as BusinessRequest[]) ?? []} />

      <AdminUserList profiles={(profiles as Profile[]) ?? []} currentUserId={user.id} />
    </div>
  )
}
