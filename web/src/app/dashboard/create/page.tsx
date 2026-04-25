'use client'

import { createClient, nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import type { CardType, BusinessCardData } from '@/lib/types'
import { CARD_COLORS, encodeCardColor, emptyBusinessCard } from '@/lib/types'
import { SOCIAL_FIELDS } from '@/lib/cardUtils'
import { Link2, Share2, Paperclip, CreditCard, BadgeCheck } from 'lucide-react'

// Social platforms (exclude basic contact fields)
const SOCIAL_PLATFORMS = SOCIAL_FIELDS.filter(f => f.icon !== null)

function buildSocialUrl(key: string, handle: string): string {
  const h = handle.replace(/^@/, '')
  const urls: Record<string, string> = {
    instagram: `https://instagram.com/${h}`,
    twitter:   `https://x.com/${h}`,
    linkedin:  `https://linkedin.com/in/${h}`,
    github:    `https://github.com/${h}`,
    facebook:  `https://facebook.com/${h}`,
    youtube:   `https://youtube.com/@${h}`,
    tiktok:    `https://tiktok.com/@${h}`,
    twitch:    `https://twitch.tv/${h}`,
    discord:   handle,
    whatsapp:  `https://wa.me/${h.replace(/[^0-9+]/g, '')}`,
  }
  return urls[key] ?? handle
}

const CardTypeIcon = ({ type, size = 22 }: { type: CardType; size?: number }) => {
  switch (type) {
    case 'business_card': return <BadgeCheck size={size} strokeWidth={1.8} />
    case 'link':          return <Link2 size={size} strokeWidth={1.8} />
    case 'social_media':  return <Share2 size={size} strokeWidth={1.8} />
    case 'file':          return <Paperclip size={size} strokeWidth={1.8} />
    case 'custom':        return <CreditCard size={size} strokeWidth={1.8} />
    default:              return <Link2 size={size} strokeWidth={1.8} />
  }
}

const CARD_TYPES: { type: CardType; label: string; desc: string }[] = [
  { type: 'business_card', label: 'Nexus',       desc: 'Your digital identity card'  },
  { type: 'link',          label: 'Link',         desc: 'Opens a URL when tapped'     },
  { type: 'social_media',  label: 'Social Media', desc: 'Link to your social profile' },
  { type: 'file',          label: 'File',         desc: 'Upload a file to share'      },
  { type: 'custom',        label: 'Custom',       desc: 'Custom text or data'         },
]

const DEFAULT_FIELDS = new Set(['name', 'jobTitle', 'company', 'phone', 'email'])

function ToggleRow({ label, left, right, value, onChange }: {
  label: string; left: string; right: string; value: string; onChange: (v: string) => void
}) {
  return (
    <div>
      <p className="text-[#666666] text-xs mb-2">{label}</p>
      <div className="grid grid-cols-2 gap-2">
        {[left, right].map(opt => (
          <button
            key={opt}
            onClick={() => onChange(opt.toLowerCase())}
            className="py-3 rounded-2xl text-sm font-semibold transition-colors"
            style={{
              background: value === opt.toLowerCase() ? '#1A1A1A' : '#111111',
              color: value === opt.toLowerCase() ? '#EEEEEE' : '#666666',
              border: `1px solid ${value === opt.toLowerCase() ? '#383838' : '#222222'}`,
            }}
          >
            {opt}
          </button>
        ))}
      </div>
    </div>
  )
}

function ColorSwatches({ selected, onChange }: { selected: string; onChange: (hex: string) => void }) {
  const current = CARD_COLORS.find(c => c.hex === selected)
  return (
    <div>
      <p className="text-[#666666] text-xs mb-2">Card Color</p>
      <div className="flex gap-2 flex-wrap mb-1">
        {CARD_COLORS.map(c => (
          <button
            key={c.hex}
            onClick={() => onChange(c.hex)}
            className="w-12 h-12 rounded-2xl transition-all"
            style={{
              background: `linear-gradient(135deg, ${c.hex}, ${c.dark})`,
              outline: selected === c.hex ? `2px solid white` : 'none',
              outlineOffset: '2px',
            }}
          />
        ))}
      </div>
      {current && <p className="text-[#555555] text-xs">{current.name}</p>}
    </div>
  )
}

function CardPreview({ type, title, subtitle, colorHex, isDark }: {
  type: CardType; title: string; subtitle?: string; colorHex: string; isDark: boolean
}) {
  const colorEntry = CARD_COLORS.find(c => c.hex === colorHex)
  const bg = isDark ? '#111111' : `linear-gradient(135deg, ${colorHex}, ${colorEntry?.dark ?? colorHex})`
  const textColor = isDark ? colorHex : 'white'
  return (
    <div className="rounded-2xl p-5 aspect-video flex flex-col justify-end border" style={{ background: bg, borderColor: colorHex + '44' }}>
      <p className="font-bold text-lg leading-tight" style={{ color: textColor }}>{title || 'Card Title'}</p>
      {subtitle && <p className="text-sm opacity-70 mt-0.5" style={{ color: textColor }}>{subtitle}</p>}
    </div>
  )
}

export default function CreateCardPage({ searchParams }: { searchParams: { type?: string } }) {
  const router = useRouter()
  const preselected = (searchParams.type as CardType) || null
  const [step, setStep] = useState<'type' | 'details'>(preselected ? 'details' : 'type')
  const [selectedType, setSelectedType] = useState<CardType | null>(preselected)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [colorHex, setColorHex] = useState(CARD_COLORS[0].hex)
  const [cardMode, setCardMode] = useState('light')
  const [cardShape, setCardShape] = useState('card')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [bcData, setBcData] = useState<BusinessCardData>(emptyBusinessCard())
  const [enabledFields, setEnabledFields] = useState<Set<string>>(new Set(DEFAULT_FIELDS))
  const [autoUser, setAutoUser] = useState('')
  const [autoEmail, setAutoEmail] = useState('')
  const [socialPlatform, setSocialPlatform] = useState<string | null>(null)
  const [socialHandle, setSocialHandle] = useState('')

  const isDark = cardMode === 'dark'

  const toggleField = (key: string) => {
    setEnabledFields(prev => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  const deriveUsername = (company: string) => company.trim().toLowerCase().replace(/[^a-z0-9]/g, '')
  const deriveEmail = (name: string, company: string) => {
    const first = name.trim().split(/\s+/)[0]?.toLowerCase().replace(/[^a-z]/g, '') ?? ''
    const domain = company.trim().toLowerCase().replace(/[^a-z0-9]/g, '')
    return first && domain ? `${first}@${domain}.com` : ''
  }

  const updateBc = (field: keyof BusinessCardData, value: string) => {
    setBcData(prev => {
      const next = { ...prev, [field]: value }
      if (field === 'name') {
        const newAutoEmail = deriveEmail(value, prev.company)
        if (!prev.email || prev.email === autoEmail) next.email = newAutoEmail
        setAutoEmail(newAutoEmail)
      }
      if (field === 'company') {
        const newUser = deriveUsername(value)
        const socialKeys = ['instagram','twitter','github','linkedin','facebook','youtube','tiktok','twitch'] as const
        socialKeys.forEach(k => {
          if (enabledFields.has(k) && (!(prev as unknown as Record<string,string>)[k] || (prev as unknown as Record<string,string>)[k] === autoUser)) {
            (next as unknown as Record<string, string>)[k] = newUser
          }
        })
        setAutoUser(newUser)
        const domain = value.trim().toLowerCase().replace(/[^a-z0-9]/g, '')
        if (!prev.website || prev.website === `${autoUser}.com`) next.website = domain ? `${domain}.com` : ''
        const newAutoEmail = deriveEmail(prev.name, value)
        if (!prev.email || prev.email === autoEmail) next.email = newAutoEmail
        setAutoEmail(newAutoEmail)
      }
      if (field === 'phone') {
        if (!prev.whatsapp || prev.whatsapp === prev.phone) next.whatsapp = value
      }
      return next
    })
  }

  const handleSave = async () => {
    const supabase = createClient()
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) { router.push('/'); return }

    let finalTitle = title
    let finalContent = content

    if (selectedType === 'business_card') {
      if (!bcData.name.trim()) { setError('Full Name is required'); return }
      finalTitle = bcData.name.trim()
      const filtered: Partial<BusinessCardData> = { name: bcData.name }
      enabledFields.forEach(k => {
        const val = (bcData as unknown as Record<string, string>)[k]
        if (val) (filtered as unknown as Record<string, string>)[k] = val
      })
      finalContent = JSON.stringify(filtered)
    } else if (selectedType === 'social_media') {
      if (!socialPlatform) { setError('Choose a platform'); return }
      if (!socialHandle.trim()) { setError('Enter your handle'); return }
      const platform = SOCIAL_PLATFORMS.find(p => p.key === socialPlatform)
      finalTitle = platform?.label ?? socialPlatform
      finalContent = buildSocialUrl(socialPlatform, socialHandle.trim())
    } else if (selectedType === 'file') {
      if (!finalTitle.trim()) { setError('Title is required'); return }
      // File upload via storage - use content as URL for now
      if (!content.trim()) { setError('Enter a file URL or link'); return }
    } else {
      if (!finalTitle.trim()) { setError('Title is required'); return }
    }

    setSaving(true)
    setError('')

    const db = nexus(supabase)
    const { error: err } = await db.from('personal_cards').insert({
      user_id: user.id,
      card_type: selectedType,
      title: finalTitle,
      content: finalContent || null,
      color: encodeCardColor(colorHex, isDark),
      is_active: true,
      order_index: 0,
      card_shape: cardShape,
    })

    if (err) {
      setError('Failed to save. Try again.')
      setSaving(false)
    } else {
      router.push('/dashboard')
      router.refresh()
    }
  }

  if (step === 'type') {
    return (
      <div className="space-y-4 pb-8">
        <div>
          <h1 className="text-xl font-bold text-white">Create Card</h1>
          <p className="text-[#037A68] text-sm">Choose a type</p>
        </div>
        <div className="space-y-3">
          {/* Nexus full width */}
          <button
            onClick={() => { setSelectedType('business_card'); setStep('details') }}
            className="w-full bg-[#1A1A1A] rounded-2xl border border-[#037A68]/40 p-4 text-left hover:border-[#037A68] active:scale-[0.99] transition-all flex items-center gap-4"
          >
            <div className="w-12 h-12 rounded-2xl bg-[#111111] flex items-center justify-center text-[#037A68] flex-shrink-0">
              <BadgeCheck size={24} strokeWidth={1.8} />
            </div>
            <div>
              <p className="text-white font-bold text-sm">Nexus</p>
              <p className="text-[#666666] text-xs mt-0.5">Your digital identity card</p>
            </div>
          </button>
          {/* Other types 2x2 */}
          <div className="grid grid-cols-2 gap-3">
            {CARD_TYPES.filter(c => c.type !== 'business_card').map(ct => (
              <button
                key={ct.type}
                onClick={() => { setSelectedType(ct.type); setStep('details') }}
                className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4 text-left hover:border-[#555555] active:scale-[0.98] transition-all"
              >
                <div className="w-10 h-10 rounded-2xl bg-[#111111] flex items-center justify-center text-[#037A68] mb-3">
                  <CardTypeIcon type={ct.type} size={20} />
                </div>
                <p className="text-white font-semibold text-sm">{ct.label}</p>
                <p className="text-[#666666] text-xs mt-0.5 leading-snug">{ct.desc}</p>
              </button>
            ))}
          </div>
        </div>
      </div>
    )
  }

  const selected = CARD_TYPES.find(c => c.type === selectedType)!
  const bcPreviewSubtitle = selectedType === 'business_card'
    ? [bcData.jobTitle, bcData.company].filter(Boolean).join(' at ') || undefined
    : undefined

  return (
    <div className="space-y-5 pb-8">
      <button onClick={() => setStep('type')} className="flex items-center gap-1 text-[#666666] text-sm hover:text-white transition-colors">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 18l-6-6 6-6"/></svg>
        Back
      </button>

      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-2xl bg-[#111111] flex items-center justify-center text-[#037A68]">
          <CardTypeIcon type={selected.type} />
        </div>
        <div>
          <h1 className="text-xl font-bold text-white">{selected.label}</h1>
          <p className="text-[#666666] text-xs">{selected.desc}</p>
        </div>
      </div>

      {/* Preview at top */}
      <CardPreview
        type={selectedType!}
        title={
          selectedType === 'business_card' ? (bcData.name || 'Full Name') :
          selectedType === 'social_media' ? (SOCIAL_PLATFORMS.find(p => p.key === socialPlatform)?.label ?? 'Social Media') :
          (title || 'Card Title')
        }
        subtitle={
          selectedType === 'social_media' && socialHandle ? socialHandle :
          bcPreviewSubtitle
        }
        colorHex={colorHex}
        isDark={isDark}
      />

      {/* Business card fields */}
      {selectedType === 'business_card' && (
        <div className="space-y-3">
          <div>
            <p className="text-[#666666] text-xs mb-2">Fields to include</p>
            <div className="flex flex-wrap gap-2">
              {SOCIAL_FIELDS.map(f => {
                const on = enabledFields.has(f.key as string)
                return (
                  <button
                    key={f.key}
                    onClick={() => toggleField(f.key as string)}
                    title={f.label}
                    className="w-10 h-10 rounded-2xl flex items-center justify-center overflow-hidden transition-all border"
                    style={{
                      background: on ? `${f.color}22` : '#1A1A1A',
                      borderColor: on ? `${f.color}66` : '#383838',
                      opacity: on ? 1 : 0.45,
                    }}
                  >
                    {f.icon
                      ? <img src={f.icon} alt={f.label} className="w-5 h-5 object-contain" />
                      : <span className="text-sm" style={{ color: on ? f.color : '#555555' }}>{f.emoji}</span>
                    }
                  </button>
                )
              })}
            </div>
          </div>

          <input type="text" value={bcData.name} onChange={e => updateBc('name', e.target.value)}
            placeholder="Full Name *"
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />

          {SOCIAL_FIELDS.filter(f => enabledFields.has(f.key as string)).map(f => (
            <input key={f.key}
              type={f.key === 'email' ? 'email' : f.key === 'phone' || f.key === 'whatsapp' ? 'tel' : 'text'}
              value={(bcData as unknown as Record<string, string>)[f.key as string] || ''}
              onChange={e => updateBc(f.key, e.target.value)}
              placeholder={f.label}
              className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
          ))}
        </div>
      )}

      {/* Link / Custom */}
      {(selectedType === 'link' || selectedType === 'custom') && (
        <div className="space-y-3">
          <input type="text" value={title} onChange={e => setTitle(e.target.value)}
            placeholder="Title *"
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
          <input type={selectedType === 'link' ? 'url' : 'text'}
            value={content} onChange={e => setContent(e.target.value)}
            placeholder={selectedType === 'link' ? 'URL *' : 'Content'}
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
        </div>
      )}

      {/* Social Media - platform picker */}
      {selectedType === 'social_media' && (
        <div className="space-y-3">
          <div>
            <p className="text-[#666666] text-xs mb-2">Choose platform</p>
            <div className="flex flex-wrap gap-2">
              {SOCIAL_PLATFORMS.map(p => {
                const on = socialPlatform === p.key
                return (
                  <button key={p.key} onClick={() => setSocialPlatform(p.key)} title={p.label}
                    className="w-10 h-10 rounded-2xl flex items-center justify-center overflow-hidden border transition-all"
                    style={{
                      background: on ? `${p.color}22` : '#1A1A1A',
                      borderColor: on ? `${p.color}88` : '#383838',
                      opacity: on ? 1 : 0.5,
                      transform: on ? 'scale(1.1)' : 'scale(1)',
                    }}
                  >
                    <img src={p.icon!} alt={p.label} className="w-5 h-5 object-contain" />
                  </button>
                )
              })}
            </div>
          </div>
          {socialPlatform && (() => {
            const p = SOCIAL_PLATFORMS.find(x => x.key === socialPlatform)!
            return (
              <input type="text" value={socialHandle} onChange={e => setSocialHandle(e.target.value)}
                placeholder={p.placeholder}
                className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
            )
          })()}
        </div>
      )}

      {/* File - paste URL for now */}
      {selectedType === 'file' && (
        <div className="space-y-3">
          <input type="text" value={title} onChange={e => setTitle(e.target.value)}
            placeholder="Title *"
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
          <input type="url" value={content} onChange={e => setContent(e.target.value)}
            placeholder="File URL *"
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-2xl px-4 py-3.5 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors" />
          <p className="text-[#444444] text-xs px-1">Direct file upload coming soon. Paste a file URL for now.</p>
        </div>
      )}

      {/* Card Shape */}
      <ToggleRow label="Card Shape" left="Card" right="Coin" value={cardShape} onChange={setCardShape} />

      {/* Card Mode */}
      <ToggleRow label="Card Mode" left="Light" right="Dark" value={cardMode} onChange={setCardMode} />

      {/* Card Color */}
      <ColorSwatches selected={colorHex} onChange={setColorHex} />

      {error && <p className="text-red-400 text-xs">{error}</p>}

      <button
        onClick={handleSave}
        disabled={saving}
        className="w-full py-4 rounded-2xl text-white font-bold text-sm transition-all disabled:opacity-40"
        style={{ background: saving ? '#025E50' : '#037A68' }}
      >
        {saving ? 'Saving...' : 'Add Card'}
      </button>
    </div>
  )
}
