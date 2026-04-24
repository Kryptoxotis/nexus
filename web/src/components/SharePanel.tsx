'use client'

import QRCode from 'react-qr-code'
import { useState } from 'react'

interface Props {
  userId: string
}

export default function SharePanel({ userId }: Props) {
  const [copied, setCopied] = useState(false)
  const profileUrl = `${typeof window !== 'undefined' ? window.location.origin : ''}/p/${userId}`

  const copy = () => {
    navigator.clipboard.writeText(profileUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-5">
      <div className="flex items-start gap-4">
        <div className="bg-white p-2 rounded-xl flex-shrink-0">
          <QRCode value={profileUrl} size={96} />
        </div>
        <div className="flex-1 min-w-0">
          <h2 className="text-white font-semibold text-sm mb-1">My Nexus</h2>
          <p className="text-[#666666] text-xs mb-3 break-all">{profileUrl}</p>
          <button
            onClick={copy}
            className="flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-lg bg-[#037A68] hover:bg-[#025E50] text-white transition-colors"
          >
            {copied ? (
              <>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M20 6L9 17l-5-5"/>
                </svg>
                Copied
              </>
            ) : (
              <>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                </svg>
                Copy link
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
