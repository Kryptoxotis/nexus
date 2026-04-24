'use client'

import type { PersonalCard } from '@/lib/types'
import { cardIcon, cardLabel } from '@/lib/cardUtils'
import Link from 'next/link'

interface Props {
  cards: PersonalCard[]
}

export default function CardsList({ cards }: Props) {
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
        <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] border-dashed p-8 text-center">
          <p className="text-[#444444] text-sm mb-1">No cards yet</p>
          <p className="text-[#333333] text-xs">Add a card to start sharing</p>
        </div>
      ) : (
        <div className="space-y-2">
          {cards.map((card) => (
            <Link
              key={card.id}
              href={`/dashboard/cards/${card.id}`}
              className="flex items-center gap-4 bg-[#1A1A1A] rounded-2xl px-4 py-3.5 border border-[#383838] hover:border-[#383838]/80 active:scale-[0.99] transition-all"
            >
              <div className="w-10 h-10 rounded-xl bg-[#111111] flex items-center justify-center flex-shrink-0 text-lg">
                {card.icon ?? cardIcon(card.card_type)}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-sm font-medium truncate">{card.title}</p>
                <p className="text-[#666666] text-xs truncate mt-0.5">{cardLabel(card.card_type)}</p>
              </div>
              <div className={`w-2 h-2 rounded-full flex-shrink-0 ${card.is_active ? 'bg-[#037A68]' : 'bg-[#383838]'}`} />
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
