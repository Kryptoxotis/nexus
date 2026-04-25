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
    <div className="relative min-h-[calc(100vh-100px)] pb-24">
      <div className="space-y-5">
        {/* My Nexus section */}
        <div>
          <p className="text-[#888888] text-xs font-semibold mb-2 tracking-wide">MY NEXUS</p>
          {nexusCards.length > 0 ? (
            <div className="space-y-3">
              {nexusCards.map(card => (
                <NexusCardPreview key={card.id} card={card} userId={user.id} showQr />
              ))}
            </div>
          ) : (
            <Link
              href="/dashboard/create?type=business_card"
              className="flex flex-col items-center justify-center bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-8 hover:border-[#037A68] transition-colors"
            >
              <p className="text-[#444444] text-sm">No Nexus card yet</p>
              <p className="text-[#037A68] text-xs font-medium mt-1">+ Create your Nexus</p>
            </Link>
          )}
        </div>

        {/* Create Nexus CTA */}
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

        {/* Contacts section */}
        <div>
          <p className="text-[#888888] text-xs font-semibold mb-3 tracking-wide">CONTACTS</p>
          <p className="text-[#333333] text-sm px-1">No contacts yet. Scan a card to add one.</p>
        </div>
      </div>

      {/* FAB - scan NFC/QR */}
      <div className="fixed bottom-8 right-6 z-10">
        <button className="w-14 h-14 rounded-2xl bg-[#037A68] flex items-center justify-center shadow-lg active:scale-95 transition-transform">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="1.8">
            <rect x="3" y="3" width="18" height="18" rx="3"/>
            <path d="M8.5 15.5a5 5 0 0 1 0-7"/>
            <path d="M11 13a2 2 0 0 1 0-2.8"/>
            <path d="M6 17.5a8 8 0 0 1 0-11"/>
          </svg>
        </button>
      </div>
    </div>
  )
}
