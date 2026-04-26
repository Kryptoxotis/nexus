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
  const isCoin = card.card_shape === 'coin'

  return (
    <div>
      {isCoin ? (
        /* Coin / circle shape */
        <div className="flex justify-center">
          <div
            className="relative rounded-full border-2 cursor-pointer flex flex-col items-center justify-center overflow-hidden"
            style={{
              width: '200px',
              height: '200px',
              background: bg,
              borderColor: hex,
            }}
            onClick={() => showQr && setQrExpanded(p => !p)}
          >
            <p className="font-bold text-xl text-center px-4 leading-tight" style={{ color: textColor }}>
              {bcData.name || card.title}
            </p>
            {(bcData.jobTitle || bcData.company) && (
              <p className="text-xs opacity-60 mt-1 text-center px-6" style={{ color: textColor }}>
                {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
              </p>
            )}
            {showQr && (
              <div className="absolute bottom-4 opacity-60">
                <QRCode value={profileUrl} size={24} fgColor={textColor} bgColor="transparent" />
              </div>
            )}
          </div>
        </div>
      ) : (
        /* Card / rectangle shape */
        <div
          className="rounded-2xl relative overflow-hidden border-2 cursor-pointer"
          style={{ background: bg, borderColor: hex, minHeight: '180px' }}
          onClick={() => showQr && setQrExpanded(p => !p)}
        >
          {/* Centered name */}
          <div className="flex flex-col items-center justify-center py-8 px-6 text-center min-h-[180px]">
            <p className="font-bold text-2xl leading-tight" style={{ color: textColor }}>
              {bcData.name || card.title}
            </p>
            {(bcData.jobTitle || bcData.company) && (
              <p className="text-sm opacity-70 mt-1" style={{ color: textColor }}>
                {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
              </p>
            )}
          </div>

          {/* QR bottom-right */}
          {showQr && (
            <div className="absolute bottom-3 right-3 opacity-60">
              <QRCode value={profileUrl} size={32} fgColor={textColor} bgColor="transparent" />
            </div>
          )}
        </div>
      )}

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
