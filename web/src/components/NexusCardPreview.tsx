'use client'

import type { PersonalCard } from '@/lib/types'
import { parseCardColor, parseBusinessCard, CARD_COLORS } from '@/lib/types'
import QRCode from 'react-qr-code'
import { useState } from 'react'

interface Props {
  card: PersonalCard
  userId: string
  showQr?: boolean
}

export default function NexusCardPreview({ card, userId, showQr = true }: Props) {
  const [qrExpanded, setQrExpanded] = useState(false)
  const { hex, isDark } = parseCardColor(card.color)
  const colorEntry = CARD_COLORS.find(c => c.hex === hex)
  const bcData = parseBusinessCard(card.content)
  const profileUrl = typeof window !== 'undefined'
    ? `${window.location.origin}/p/${userId}`
    : `/p/${userId}`

  const bg = isDark ? '#111111' : `linear-gradient(135deg, ${hex}, ${colorEntry?.dark ?? hex})`
  const textColor = isDark ? hex : 'white'

  return (
    <div>
      <div
        className="rounded-2xl p-5 relative overflow-hidden border cursor-pointer"
        style={{ background: bg, borderColor: hex + '44' }}
        onClick={() => showQr && setQrExpanded(p => !p)}
      >
        <div className="pr-20">
          <p className="text-xs font-medium opacity-50 mb-1" style={{ color: textColor }}>Nexus</p>
          <p className="font-bold text-lg leading-tight" style={{ color: textColor }}>
            {bcData.name || card.title}
          </p>
          {(bcData.jobTitle || bcData.company) && (
            <p className="text-sm opacity-70 mt-0.5" style={{ color: textColor }}>
              {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
            </p>
          )}
          {bcData.email && (
            <p className="text-xs opacity-50 mt-1" style={{ color: textColor }}>{bcData.email}</p>
          )}
        </div>

        {showQr && (
          <div className="absolute bottom-3 right-3 bg-white rounded-lg p-1.5">
            <QRCode value={profileUrl} size={48} />
          </div>
        )}
      </div>

      {qrExpanded && (
        <div className="mt-3 bg-[#1A1A1A] rounded-2xl border border-[#383838] p-5 flex flex-col items-center gap-3">
          <div className="bg-white p-3 rounded-xl">
            <QRCode value={profileUrl} size={160} />
          </div>
          <p className="text-[#666666] text-xs text-center">Scan to view your Nexus</p>
          <button
            onClick={() => {
              navigator.clipboard.writeText(profileUrl)
            }}
            className="text-xs text-[#037A68] font-medium"
          >
            Copy link
          </button>
        </div>
      )}
    </div>
  )
}
