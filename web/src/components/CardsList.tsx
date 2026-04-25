'use client'

import type { PersonalCard, CardType } from '@/lib/types'
import { cardLabel, businessCardSubtitle, SOCIAL_FIELDS } from '@/lib/cardUtils'
import { parseCardColor, parseBusinessCard, CARD_COLORS } from '@/lib/types'
import Link from 'next/link'
import { Link2, Share2, Paperclip, CreditCard, BadgeCheck, File } from 'lucide-react'

const CardIcon = ({ type, title }: { type: CardType; title?: string }) => {
  if (type === 'social_media' && title) {
    const platform = SOCIAL_FIELDS.find(f =>
      f.label.toLowerCase() === title.toLowerCase() && f.icon
    )
    if (platform?.icon) return <img src={platform.icon} alt={platform.label} className="w-5 h-5 object-contain" />
  }
  switch (type) {
    case 'business_card': return <BadgeCheck size={18} strokeWidth={1.8} />
    case 'link':          return <Link2 size={18} strokeWidth={1.8} />
    case 'social_media':  return <Share2 size={18} strokeWidth={1.8} />
    case 'file':          return <Paperclip size={18} strokeWidth={1.8} />
    case 'custom':        return <CreditCard size={18} strokeWidth={1.8} />
    default:              return <File size={18} strokeWidth={1.8} />
  }
}
import { createClient, nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useState } from 'react'

interface Props {
  cards: PersonalCard[]
}

export default function CardsList({ cards: initial }: Props) {
  const router = useRouter()
  const [cards, setCards] = useState(initial)

  const toggleActive = async (id: string, current: boolean) => {
    const supabase = createClient()
    const db = nexus(supabase)
    await db.from('personal_cards').update({ is_active: !current }).eq('id', id)
    setCards(prev => prev.map(c => c.id === id ? { ...c, is_active: !current } : c))
    router.refresh()
  }

  const getCardStyle = (card: PersonalCard) => {
    const { hex, isDark } = parseCardColor(card.color)
    const colorEntry = CARD_COLORS.find(c => c.hex === hex)
    return {
      background: isDark ? '#111111' : `linear-gradient(135deg, ${hex}, ${colorEntry?.dark ?? hex})`,
      borderColor: hex + '44',
      textColor: isDark ? hex : 'white',
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-[#666666] text-xs font-medium uppercase tracking-wider">My Cards</h2>
        <Link
          href="/dashboard/create"
          className="flex items-center gap-1 text-xs font-medium text-[#037A68] hover:text-[#0AD7A5] transition-colors"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Add card
        </Link>
      </div>

      {cards.length === 0 ? (
        <Link
          href="/dashboard/create"
          className="flex flex-col items-center justify-center bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-10 hover:border-[#037A68] transition-colors"
        >
          <p className="text-[#444444] text-sm mb-1">No cards yet</p>
          <p className="text-[#037A68] text-xs font-medium">+ Create your first card</p>
        </Link>
      ) : (
        <div className="space-y-2">
          {cards.map(card => {
            const style = getCardStyle(card)
            const isNexus = card.card_type === 'business_card'
            const bcData = isNexus ? parseBusinessCard(card.content) : null

            return (
              <div
                key={card.id}
                className="rounded-2xl border overflow-hidden"
                style={{ borderColor: style.borderColor }}
              >
                <div
                  className="px-4 py-3.5 flex items-center gap-3"
                  style={{ background: style.background }}
                >
                  <div className="w-10 h-10 rounded-2xl bg-black/20 flex items-center justify-center flex-shrink-0" style={{ color: style.textColor }}>
                    <CardIcon type={card.card_type} title={card.title} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm truncate" style={{ color: style.textColor }}>
                      {card.title}
                    </p>
                    {isNexus && bcData && (
                      <p className="text-xs truncate mt-0.5 opacity-70" style={{ color: style.textColor }}>
                        {businessCardSubtitle(bcData)}
                      </p>
                    )}
                    {!isNexus && (
                      <p className="text-xs truncate mt-0.5 opacity-70" style={{ color: style.textColor }}>
                        {cardLabel(card.card_type)}
                      </p>
                    )}
                  </div>
                  <button
                    onClick={() => toggleActive(card.id, card.is_active)}
                    className={`w-10 h-6 rounded-full transition-colors flex-shrink-0 relative ${
                      card.is_active ? 'bg-[#037A68]' : 'bg-black/30'
                    }`}
                  >
                    <span
                      className={`absolute top-1 w-4 h-4 rounded-full bg-white transition-all ${
                        card.is_active ? 'left-5' : 'left-1'
                      }`}
                    />
                  </button>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
