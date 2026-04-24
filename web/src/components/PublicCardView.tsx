'use client'

import type { Profile, PersonalCard } from '@/lib/types'
import { cardIcon, cardLabel } from '@/lib/cardUtils'

interface Props {
  profile: Pick<Profile, 'id' | 'full_name' | 'avatar_url' | 'account_type'>
  cards: PersonalCard[]
}

export default function PublicCardView({ profile, cards }: Props) {
  const handleCardTap = (card: PersonalCard) => {
    if (!card.content) return
    if (card.card_type === 'link' || card.card_type === 'social_media') {
      const url = card.content.startsWith('http') ? card.content : `https://${card.content}`
      window.open(url, '_blank', 'noopener,noreferrer')
    } else if (card.card_type === 'file') {
      window.open(card.content, '_blank', 'noopener,noreferrer')
    }
  }

  return (
    <div className="min-h-screen bg-[#0A0A0A] flex flex-col">
      <div className="max-w-md mx-auto w-full px-4 py-8 flex-1">

        {/* Header */}
        <div className="flex items-center gap-3 mb-8">
          <div className="w-12 h-12 rounded-2xl bg-[#037A68] flex items-center justify-center flex-shrink-0">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.9"/>
              <rect x="13" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
              <rect x="3" y="13" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
              <circle cx="17" cy="17" r="3" fill="#FA5700"/>
            </svg>
          </div>
          <div>
            <h1 className="text-white font-bold text-lg leading-tight">
              {profile.full_name ?? 'Nexus User'}
            </h1>
            <p className="text-[#037A68] text-xs font-medium capitalize">{profile.account_type}</p>
          </div>
        </div>

        {/* Cards */}
        {cards.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-[#444444] text-sm">No cards shared yet</p>
          </div>
        ) : (
          <div className="space-y-3">
            {cards.map((card) => (
              <button
                key={card.id}
                onClick={() => handleCardTap(card)}
                className="w-full flex items-center gap-4 bg-[#1A1A1A] rounded-2xl px-4 py-4 border border-[#383838] hover:border-[#037A68] active:scale-[0.98] transition-all text-left"
              >
                <div className="w-11 h-11 rounded-xl bg-[#111111] flex items-center justify-center flex-shrink-0 text-xl">
                  {card.icon ?? cardIcon(card.card_type)}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-white font-semibold text-sm truncate">{card.title}</p>
                  <p className="text-[#666666] text-xs truncate mt-0.5">
                    {cardLabel(card.card_type)}
                  </p>
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#666666" strokeWidth="2">
                  <path d="M9 18l6-6-6-6"/>
                </svg>
              </button>
            ))}
          </div>
        )}

        {/* Footer */}
        <div className="mt-10 text-center">
          <p className="text-[#333333] text-xs">Shared via</p>
          <p className="text-[#037A68] text-sm font-semibold mt-0.5">Nexus</p>
        </div>
      </div>
    </div>
  )
}
