import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, BusinessRequest } from '@/lib/types'

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
    db.from('business_requests').select('*').eq('status', 'pending').order('created_at', { ascending: false }),
    db.from('organizations').select('*').order('created_at', { ascending: false }).limit(100),
  ])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Admin</h1>
        <p className="text-[#037A68] text-sm mt-0.5">System overview</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: 'Users', value: profiles?.length ?? 0, color: 'text-[#FA5700]' },
          { label: 'Orgs', value: orgs?.length ?? 0, color: 'text-[#037A68]' },
          { label: 'Pending', value: requests?.length ?? 0, color: 'text-[#FA5700]' },
        ].map(s => (
          <div key={s.label} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4 text-center">
            <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
            <p className="text-[#666666] text-xs mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Pending business requests */}
      {(requests?.length ?? 0) > 0 && (
        <div>
          <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">Business Requests</h2>
          <div className="space-y-2">
            {(requests as BusinessRequest[]).map(req => (
              <div key={req.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-white font-medium text-sm">{req.business_name}</p>
                    <p className="text-[#666666] text-xs mt-0.5">{req.contact_email}</p>
                    {req.message && <p className="text-[#555555] text-xs mt-1.5">{req.message}</p>}
                  </div>
                  <span className="text-[#FA5700] text-xs font-medium bg-[#FA5700]/10 px-2 py-0.5 rounded-full">
                    Pending
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recent users */}
      <div>
        <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">Recent Users</h2>
        <div className="space-y-2">
          {(profiles as Profile[] ?? []).slice(0, 10).map(p => (
            <div key={p.id} className="flex items-center gap-3 bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3">
              <div className="w-8 h-8 rounded-full bg-[#037A68]/20 flex items-center justify-center flex-shrink-0">
                <span className="text-[#037A68] text-xs font-bold">
                  {(p.full_name ?? p.email ?? '?')[0].toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-sm font-medium truncate">{p.full_name ?? p.email ?? 'Unknown'}</p>
                <p className="text-[#444444] text-xs truncate">{p.email}</p>
              </div>
              <span className={`text-xs font-medium capitalize ${
                p.account_type === 'admin' ? 'text-[#FA5700]' :
                p.account_type === 'business' ? 'text-[#037A68]' :
                'text-[#555555]'
              }`}>
                {p.account_type}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
