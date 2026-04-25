export const dynamic = 'force-dynamic'

import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { BusinessPass } from '@/lib/types'

export default async function BusinessPage() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)

  const { data: passes } = await db
    .from('business_passes')
    .select('*, organization:organizations(name)')
    .eq('user_id', user.id)

  const passList = (passes as (BusinessPass & { organization: { name: string } | null })[]) ?? []

  return (
    <div className="relative min-h-[calc(100vh-100px)] pb-24">
      <div className="space-y-5">
        <h1 className="text-xl font-bold text-white">Business Passes</h1>

        {passList.length === 0 ? (
          <div className="flex flex-col items-center justify-center pt-20">
            <svg width="72" height="72" viewBox="0 0 24 24" fill="none" stroke="#2A2A2A" strokeWidth="1.2" className="mb-4">
              <rect x="2" y="7" width="20" height="14" rx="2"/>
              <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
              <circle cx="12" cy="14" r="2"/>
              <line x1="12" y1="16" x2="12" y2="18"/>
              <line x1="9" y1="20" x2="15" y2="20"/>
            </svg>
            <p className="text-[#444444] text-base font-semibold">No business passes</p>
            <p className="text-[#333333] text-sm mt-1.5">Tap + to enroll in an organization</p>
          </div>
        ) : (
          <div className="space-y-2">
            {passList.map(pass => (
              <div key={pass.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3.5 flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#111111] flex items-center justify-center flex-shrink-0">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#037A68" strokeWidth="1.8">
                    <rect x="2" y="7" width="20" height="14" rx="2"/>
                    <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-white text-sm font-semibold truncate">{pass.organization?.name ?? 'Unknown Org'}</p>
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
        )}
      </div>

      {/* FAB */}
      <div className="fixed bottom-8 right-6 z-10">
        <button className="w-14 h-14 rounded-2xl bg-[#037A68] flex items-center justify-center shadow-lg active:scale-95 transition-transform">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14"/>
          </svg>
        </button>
      </div>
    </div>
  )
}
