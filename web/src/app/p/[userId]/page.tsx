import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, PersonalCard } from '@/lib/types'
import { notFound } from 'next/navigation'
import PublicCardView from '@/components/PublicCardView'

export default async function PublicProfilePage({
  params,
}: {
  params: { userId: string }
}) {
  const supabase = createClient()
  const db = nexus(supabase)

  const { data: profile } = await db
    .from('profiles')
    .select('id, full_name, avatar_url, account_type')
    .eq('id', params.userId)
    .eq('status', 'active')
    .single<Pick<Profile, 'id' | 'full_name' | 'avatar_url' | 'account_type'>>()

  if (!profile) notFound()

  const { data: cards } = await db
    .from('personal_cards')
    .select('*')
    .eq('user_id', params.userId)
    .eq('is_active', true)
    .order('order_index', { ascending: true })

  return <PublicCardView profile={profile} cards={(cards as PersonalCard[]) ?? []} />
}
