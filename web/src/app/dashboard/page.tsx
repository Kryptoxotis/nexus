export const dynamic = 'force-dynamic'

import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, PersonalCard } from '@/lib/types'
import CardsList from '@/components/CardsList'
import Link from 'next/link'

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

  const { data: allCards } = await db
    .from('personal_cards')
    .select('*')
    .eq('user_id', user.id)
    .order('order_index', { ascending: true })

  const cards = (allCards as PersonalCard[]) ?? []
  const linkCards = cards.filter(c => c.card_type !== 'business_card')

  return (
    <div className="space-y-5 pb-8">
      {/* Search bar */}
      <div className="relative">
        <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#666666]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input
          type="text"
          placeholder="Search cards..."
          className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl pl-9 pr-4 py-3 text-white text-sm placeholder-[#555555] focus:outline-none focus:border-[#037A68] transition-colors"
          readOnly
        />
      </div>

      {/* Create a Card CTA */}
      <Link
        href="/dashboard/create"
        className="flex items-center gap-4 bg-[#1A1A1A] rounded-2xl border border-[#383838] px-4 py-4 hover:border-[#555555] active:scale-[0.99] transition-all"
      >
        <div className="w-11 h-11 rounded-full border-2 border-[#037A68] flex items-center justify-center flex-shrink-0">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#037A68" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14"/>
          </svg>
        </div>
        <div>
          <p className="text-white font-bold text-sm">Create a Card</p>
          <p className="text-[#666666] text-xs mt-0.5">Add a new pass to share via QR or NFC</p>
        </div>
      </Link>

      {/* My Cards */}
      <CardsList cards={linkCards} />
    </div>
  )
}
