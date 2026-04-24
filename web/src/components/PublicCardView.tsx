'use client'

import type { Profile, PersonalCard } from '@/lib/types'
import { parseCardColor, parseBusinessCard, CARD_COLORS } from '@/lib/types'
import { SOCIAL_FIELDS } from '@/lib/cardUtils'
import { cardIcon } from '@/lib/cardUtils'

interface Props {
  profile: Pick<Profile, 'id' | 'full_name' | 'avatar_url' | 'account_type'>
  cards: PersonalCard[]
}

export default function PublicCardView({ profile, cards }: Props) {
  const nexusCard = cards.find(c => c.card_type === 'business_card')
  const otherCards = cards.filter(c => c.card_type !== 'business_card')

  const openCard = (card: PersonalCard) => {
    if (!card.content) return
    if (['link', 'social_media', 'file'].includes(card.card_type)) {
      const url = card.content.startsWith('http') ? card.content : `https://${card.content}`
      window.open(url, '_blank', 'noopener,noreferrer')
    }
  }

  const getCardStyle = (card: PersonalCard) => {
    const { hex, isDark } = parseCardColor(card.color)
    const colorEntry = CARD_COLORS.find(c => c.hex === hex)
    return {
      background: isDark ? '#111111' : `linear-gradient(135deg, ${hex}, ${colorEntry?.dark ?? hex})`,
      borderColor: hex + '44',
      textColor: isDark ? hex : 'white',
      accentColor: hex,
    }
  }

  return (
    <div className="min-h-screen bg-[#0A0A0A]">
      <div className="max-w-md mx-auto px-4 py-8">

        {/* Header */}
        <div className="flex items-center gap-3 mb-8">
          <img src="/icon-512.png" alt="Nexus" className="w-10 h-10 rounded-xl object-cover flex-shrink-0" />
          <div>
            <h1 className="text-white font-bold text-lg leading-tight">
              {profile.full_name ?? 'Nexus User'}
            </h1>
            <p className="text-[#037A68] text-xs font-medium capitalize">{profile.account_type}</p>
          </div>
        </div>

        {/* Nexus identity card */}
        {nexusCard && (() => {
          const bcData = parseBusinessCard(nexusCard.content)
          const style = getCardStyle(nexusCard)
          const activeSocials = SOCIAL_FIELDS.filter(f =>
            !['name','jobTitle','company','phone','email','website','address'].includes(f.key as string) &&
            (bcData as unknown as Record<string, string>)[f.key as string]
          )

          return (
            <div className="rounded-2xl border p-5 mb-6" style={{ background: style.background, borderColor: style.borderColor }}>
              <p className="text-xs font-medium opacity-50 mb-1" style={{ color: style.textColor }}>Nexus</p>
              <p className="text-xl font-bold" style={{ color: style.textColor }}>{bcData.name}</p>
              {(bcData.jobTitle || bcData.company) && (
                <p className="text-sm opacity-70 mt-0.5" style={{ color: style.textColor }}>
                  {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
                </p>
              )}

              <div className="mt-4 space-y-2">
                {bcData.phone && (
                  <a href={`tel:${bcData.phone}`} className="flex items-center gap-2 text-sm opacity-80 hover:opacity-100" style={{ color: style.textColor }}>
                    <span>📞</span> {bcData.phone}
                  </a>
                )}
                {bcData.email && (
                  <a href={`mailto:${bcData.email}`} className="flex items-center gap-2 text-sm opacity-80 hover:opacity-100" style={{ color: style.textColor }}>
                    <span>✉️</span> {bcData.email}
                  </a>
                )}
                {bcData.website && (
                  <a href={bcData.website.startsWith('http') ? bcData.website : `https://${bcData.website}`} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 text-sm opacity-80 hover:opacity-100" style={{ color: style.textColor }}>
                    <span>🌐</span> {bcData.website}
                  </a>
                )}
              </div>

              {activeSocials.length > 0 && (
                <div className="mt-4 flex flex-wrap gap-2">
                  {activeSocials.map(f => {
                    const val = (bcData as unknown as Record<string, string>)[f.key as string]
                    const url = f.key === 'instagram' ? `https://instagram.com/${val.replace('@', '')}` :
                                f.key === 'twitter' ? `https://x.com/${val.replace('@', '')}` :
                                f.key === 'linkedin' ? `https://linkedin.com/in/${val}` :
                                f.key === 'github' ? `https://github.com/${val}` :
                                f.key === 'youtube' ? `https://youtube.com/@${val.replace('@', '')}` :
                                f.key === 'tiktok' ? `https://tiktok.com/@${val.replace('@', '')}` :
                                f.key === 'twitch' ? `https://twitch.tv/${val}` :
                                f.key === 'facebook' ? `https://facebook.com/${val}` :
                                f.key === 'discord' ? val : null
                    return url ? (
                      <a
                        key={f.key}
                        href={url}
                        target="_blank"
                        rel="noopener noreferrer"
                        title={f.label}
                        className="w-9 h-9 rounded-xl bg-black/20 flex items-center justify-center text-sm hover:bg-black/30 transition-colors"
                        style={{ color: style.textColor }}
                      >
                        {f.emoji}
                      </a>
                    ) : null
                  })}
                </div>
              )}
            </div>
          )
        })()}

        {/* Other cards */}
        {otherCards.length > 0 && (
          <div className="space-y-2">
            {otherCards.map(card => {
              const style = getCardStyle(card)
              return (
                <button
                  key={card.id}
                  onClick={() => openCard(card)}
                  className="w-full rounded-2xl border px-4 py-4 flex items-center gap-3 text-left active:scale-[0.98] transition-all"
                  style={{ background: style.background, borderColor: style.borderColor }}
                >
                  <div className="w-10 h-10 rounded-xl bg-black/20 flex items-center justify-center flex-shrink-0 text-lg">
                    {card.icon ?? cardIcon(card.card_type)}
                  </div>
                  <p className="font-semibold text-sm flex-1 truncate" style={{ color: style.textColor }}>
                    {card.title}
                  </p>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ color: style.textColor, opacity: 0.5 }}>
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                </button>
              )
            })}
          </div>
        )}

        {cards.length === 0 && (
          <div className="text-center py-16">
            <p className="text-[#444444] text-sm">No cards shared yet</p>
          </div>
        )}

        <div className="mt-10 text-center">
          <p className="text-[#333333] text-xs">Shared via</p>
          <p className="text-[#037A68] text-sm font-semibold mt-0.5">Nexus</p>
        </div>
      </div>
    </div>
  )
}
