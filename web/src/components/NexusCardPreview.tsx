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
      {/* Card */}
      <div
        className="rounded-2xl relative overflow-hidden border cursor-pointer"
        style={{ background: bg, borderColor: hex + '44', minHeight: '180px' }}
        onClick={() => showQr && setQrExpanded(p => !p)}
      >
        {/* Centered content */}
        <div className="flex flex-col items-center justify-center py-8 px-6 text-center">
          <p className="font-bold text-2xl leading-tight" style={{ color: textColor }}>
            {bcData.name || card.title}
          </p>
          {(bcData.jobTitle || bcData.company) && (
            <p className="text-sm opacity-70 mt-1" style={{ color: textColor }}>
              {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
            </p>
          )}
        </div>

        {/* Bottom row: QR left, NFC right */}
        <div className="absolute bottom-3 left-3 right-3 flex items-end justify-between">
          {showQr && (
            <div className="opacity-70">
              <QRCode value={profileUrl} size={32} fgColor={textColor} bgColor="transparent" />
            </div>
          )}
          {/* NFC icon bottom right */}
          <div className="opacity-60 ml-auto">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke={textColor} strokeWidth="1.5">
              <rect x="3" y="3" width="18" height="18" rx="3"/>
              <path d="M8.5 15.5a5 5 0 0 1 0-7"/>
              <path d="M11 13a2 2 0 0 1 0-2.8"/>
              <path d="M6 17.5a8 8 0 0 1 0-11"/>
            </svg>
          </div>
        </div>
      </div>

      {/* Expanded QR */}
      {qrExpanded && (
        <div className="mt-3 bg-[#1A1A1A] rounded-2xl border border-[#383838] p-5 flex flex-col items-center gap-3">
          <div className="bg-white p-3 rounded-xl">
            <QRCode value={profileUrl} size={160} />
          </div>
          <p className="text-[#666666] text-xs">Scan to view your Nexus</p>
          <button
            onClick={() => navigator.clipboard.writeText(profileUrl)}
            className="text-xs text-[#037A68] font-medium"
          >
            Copy link
          </button>
        </div>
      )}
    </div>
  )
}
