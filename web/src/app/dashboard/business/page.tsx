import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, Organization, BusinessPass } from '@/lib/types'

export default async function BusinessPage() {
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

  const [{ data: ownedOrgs }, { data: passes }] = await Promise.all([
    db.from('organizations').select('*').eq('owner_id', user.id).order('created_at', { ascending: false }),
    db.from('business_passes').select('*, organization:organizations(name)').eq('user_id', user.id),
  ])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Business</h1>
        <p className="text-[#037A68] text-sm mt-0.5">Organizations & passes</p>
      </div>

      {/* Owned organizations */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider">My Organizations</h2>
        </div>
        {(ownedOrgs?.length ?? 0) === 0 ? (
          <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-6 text-center">
            <p className="text-[#444444] text-sm">No organizations yet</p>
            <p className="text-[#333333] text-xs mt-1">Contact an admin to set up your organization</p>
          </div>
        ) : (
          <div className="space-y-2">
            {(ownedOrgs as Organization[]).map(org => (
              <div key={org.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-white font-medium text-sm">{org.name}</p>
                    {org.type && <p className="text-[#666666] text-xs mt-0.5">{org.type}</p>}
                  </div>
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    org.is_active ? 'bg-[#037A68]/15 text-[#037A68]' : 'bg-[#383838] text-[#666666]'
                  }`}>
                    {org.is_active ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <div className="mt-2 text-xs text-[#444444]">
                  Enrollment: <span className="text-[#666666] capitalize">{org.enrollment_mode}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Passes held */}
      {(passes?.length ?? 0) > 0 && (
        <div>
          <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">My Passes</h2>
          <div className="space-y-2">
            {(passes as (BusinessPass & { organization: { name: string } | null })[]).map(pass => (
              <div key={pass.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-white text-sm font-medium">{pass.organization?.name ?? 'Unknown'}</p>
                  <p className="text-[#444444] text-xs mt-0.5">Used {pass.use_count} times</p>
                </div>
                <span className={`text-xs font-medium capitalize px-2 py-0.5 rounded-full ${
                  pass.status === 'active' ? 'bg-[#037A68]/15 text-[#037A68]' : 'bg-[#383838] text-[#666666]'
                }`}>
                  {pass.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
