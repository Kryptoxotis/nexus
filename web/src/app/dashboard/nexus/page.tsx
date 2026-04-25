export const dynamic = 'force-dynamic'

import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile, PersonalCard } from '@/lib/types'
import NexusCardPreview from '@/components/NexusCardPreview'
import Link from 'next/link'

export default async function NexusPage() {
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

  const { data: myNexusCards } = await db
    .from('personal_cards')
    .select('*')
    .eq('user_id', user.id)
    .eq('card_type', 'business_card')
    .order('order_index', { ascending: true })

  const nexusCards = (myNexusCards as PersonalCard[]) ?? []

  return (
    <div className="space-y-6 pb-8">
      <div>
        <h2 className="text-white font-bold text-lg">My Nexus</h2>
        <p className="text-[#666666] text-xs mt-0.5">Your digital identity cards</p>
      </div>

      {/* My Nexus cards */}
      {nexusCards.length > 0 ? (
        <div className="space-y-3">
          {nexusCards.map(card => (
            <NexusCardPreview key={card.id} card={card} userId={user.id} showQr />
          ))}
        </div>
      ) : (
        <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-10 text-center">
          <p className="text-[#444444] text-sm">No Nexus card yet</p>
          <p className="text-[#333333] text-xs mt-1">Create one to start sharing your identity</p>
        </div>
      )}

      {/* Create Nexus button */}
      <Link
        href="/dashboard/create?type=business_card"
        className="flex items-center gap-4 bg-[#1A1A1A] rounded-2xl border border-[#037A68]/30 px-4 py-4 hover:border-[#037A68]/60 active:scale-[0.99] transition-all"
      >
        <div className="w-11 h-11 rounded-full border-2 border-[#037A68] flex items-center justify-center flex-shrink-0">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#037A68" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14"/>
          </svg>
        </div>
        <div>
          <p className="text-white font-bold text-sm">Create My Nexus</p>
          <p className="text-[#666666] text-xs mt-0.5">Your digital identity card</p>
        </div>
      </Link>

      {/* Saved Nexus cards from others */}
      <div>
        <p className="text-[#666666] text-xs font-medium uppercase tracking-wider mb-3">Saved Nexus</p>
        <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-8 text-center">
          <p className="text-[#444444] text-sm">No saved Nexus yet</p>
          <p className="text-[#333333] text-xs mt-1">Scan someone's QR code to save their Nexus</p>
        </div>
      </div>
    </div>
  )
}
