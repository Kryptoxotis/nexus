import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { AccessLog } from '@/lib/types'

export default async function LogsPage() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)
  const { data: logs } = await db
    .from('access_logs')
    .select('*')
    .eq('user_id', user.id)
    .order('created_at', { ascending: false })
    .limit(100)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Access Logs</h1>
        <p className="text-[#666666] text-sm mt-0.5">{logs?.length ?? 0} recent events</p>
      </div>

      {(logs?.length ?? 0) === 0 ? (
        <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-8 text-center">
          <p className="text-[#444444] text-sm">No logs yet</p>
          <p className="text-[#333333] text-xs mt-1">Logs appear when your cards are accessed</p>
        </div>
      ) : (
        <div className="space-y-2">
          {(logs as AccessLog[]).map(log => (
            <div key={log.id} className="bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-3 flex items-center justify-between">
              <div>
                <p className="text-white text-sm font-medium capitalize">
                  {log.card_type ?? 'Card'} access
                </p>
                <p className="text-[#444444] text-xs mt-0.5">
                  {new Date(log.created_at).toLocaleString()}
                </p>
              </div>
              <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                log.access_granted ? 'bg-[#037A68]/15 text-[#037A68]' : 'bg-red-900/20 text-red-400'
              }`}>
                {log.access_granted ? 'Granted' : 'Denied'}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
