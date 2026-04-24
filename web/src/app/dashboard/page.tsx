import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, PersonalCard } from '@/lib/types'
import CardsList from '@/components/CardsList'
import SharePanel from '@/components/SharePanel'

export default async function DashboardPage() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)

  const { data: profile } = await db
    .from('profiles')
    .select('*')
    .eq('id', user.id)
    .single<Profile>()

  if (!profile) redirect('/')

  const { data: cards } = await db
    .from('personal_cards')
    .select('*')
    .eq('user_id', user.id)
    .order('order_index', { ascending: true })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Nexus</h1>
        <p className="text-[#037A68] text-sm font-medium capitalize">{profile.account_type}</p>
      </div>

      <SharePanel userId={user.id} />

      <CardsList cards={(cards as PersonalCard[]) ?? []} />
    </div>
  )
}
