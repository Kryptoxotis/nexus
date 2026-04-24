import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, BusinessPass } from '@/lib/types'

export default async function MembersPage() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)

  const { data: profile } = await db
    .from('profiles')
    .select('account_type')
    .eq('id', user.id)
    .single<Pick<Profile, 'account_type'>>()

  if (!profile || profile.account_type === 'individual') redirect('/dashboard')

  const { data: orgs } = await db
    .from('organizations')
    .select('id, name')
    .eq('owner_id', user.id)
    .limit(1)

  const orgId = orgs?.[0]?.id

  const { data: members } = orgId
    ? await db
        .from('business_passes')
        .select('*, profile:profiles(full_name, email)')
        .eq('organization_id', orgId)
        .order('created_at', { ascending: false })
    : { data: [] }

  if (!orgId) redirect('/dashboard/business')

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Members</h1>
        <p className="text-[#666666] text-sm mt-0.5">{members?.length ?? 0} total</p>
      </div>

      {(members?.length ?? 0) === 0 ? (
        <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-8 text-center">
          <p className="text-[#444444] text-sm">No members yet</p>
        </div>
      ) : (
        <div className="space-y-2">
          {(members as (BusinessPass & { profile: Pick<Profile, 'full_name' | 'email'> | null })[]).map(m => (
            <div key={m.id} className="flex items-center gap-3 bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3">
              <div className="w-8 h-8 rounded-full bg-[#037A68]/20 flex items-center justify-center flex-shrink-0">
                <span className="text-[#037A68] text-xs font-bold">
                  {(m.profile?.full_name ?? m.profile?.email ?? '?')[0].toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-sm font-medium truncate">{m.profile?.full_name ?? 'Unknown'}</p>
                <p className="text-[#444444] text-xs truncate">{m.profile?.email}</p>
              </div>
              <span className={`text-xs font-medium capitalize px-2 py-0.5 rounded-full flex-shrink-0 ${
                m.status === 'active' ? 'bg-[#037A68]/15 text-[#037A68]' : 'bg-[#383838] text-[#666666]'
              }`}>
                {m.status}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
