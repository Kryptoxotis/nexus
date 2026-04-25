'use client'

import { createClient, nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import type { CardType, BusinessCardData } from '@/lib/types'
import { CARD_COLORS, encodeCardColor, emptyBusinessCard } from '@/lib/types'
import { SOCIAL_FIELDS } from '@/lib/cardUtils'
import { Link2, Share2, Paperclip, CreditCard, BadgeCheck } from 'lucide-react'

const CardTypeIcon = ({ type, size = 22 }: { type: CardType; size?: number }) => {
  const cls = `w-[${size}px] h-[${size}px]`
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
  { type: 'business_card', label: 'Nexus',        desc: 'Your digital identity card'   },
  { type: 'link',          label: 'Link',          desc: 'Opens a URL when tapped'      },
  { type: 'social_media',  label: 'Social Media',  desc: 'Link to your social profile'  },
  { type: 'file',          label: 'File',          desc: 'Share a file via QR or tap'   },
  { type: 'custom',        label: 'Custom',        desc: 'Custom text or data'          },
]

const DEFAULT_FIELDS = new Set(['name', 'jobTitle', 'company', 'phone', 'email'])

function ColorPicker({ selected, isDark, onChange, onDarkToggle }: {
  selected: string
  isDark: boolean
  onChange: (hex: string) => void
  onDarkToggle: () => void
}) {
  return (
    <div>
      <label className="block text-xs text-[#666666] mb-2">Color</label>
      <div className="flex flex-wrap gap-2 mb-2">
        {CARD_COLORS.map(c => (
          <button
            key={c.hex}
            onClick={() => onChange(c.hex)}
            className="w-7 h-7 rounded-full border-2 transition-all"
            style={{
              background: `linear-gradient(135deg, ${c.hex}, ${c.dark})`,
              borderColor: selected === c.hex ? 'white' : 'transparent',
            }}
          />
        ))}
      </div>
      <button
        onClick={onDarkToggle}
        className={`text-xs px-3 py-1.5 rounded-lg border transition-colors ${
          isDark
            ? 'border-[#037A68] text-[#037A68] bg-[#037A68]/10'
            : 'border-[#383838] text-[#666666]'
        }`}
      >
        {isDark ? '🌙 Dark mode' : '☀️ Light mode'}
      </button>
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
  const [isDark, setIsDark] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  // Business card state
  const [bcData, setBcData] = useState<BusinessCardData>(emptyBusinessCard())
  const [enabledFields, setEnabledFields] = useState<Set<string>>(new Set(DEFAULT_FIELDS))
  const [autoUser, setAutoUser] = useState('')
  const [autoEmail, setAutoEmail] = useState('')

  const toggleField = (key: string) => {
    setEnabledFields(prev => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  const deriveUsername = (company: string) =>
    company.trim().toLowerCase().replace(/[^a-z0-9]/g, '')

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
          if (enabledFields.has(k) && (!prev[k] || prev[k] === autoUser)) {
            (next as Record<string, string>)[k] = newUser
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
      card_shape: 'card',
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
      <div>
        <div className="mb-6">
          <h1 className="text-xl font-bold text-white">Create Card</h1>
          <p className="text-[#037A68] text-sm mt-0.5">Choose a type</p>
        </div>
        <div className="grid grid-cols-2 gap-3">
          {CARD_TYPES.map(ct => (
            <button
              key={ct.type}
              onClick={() => { setSelectedType(ct.type); setStep('details') }}
              className={`bg-[#1A1A1A] rounded-2xl border p-4 text-left hover:border-[#037A68] active:scale-[0.98] transition-all ${
                ct.type === 'business_card' ? 'border-[#037A68]/40 col-span-2' : 'border-[#383838]'
              }`}
            >
              <div className="w-10 h-10 rounded-2xl bg-[#111111] flex items-center justify-center mb-3 text-[#EEEEEE]">
                <CardTypeIcon type={ct.type} />
              </div>
              <p className="text-white font-semibold text-sm">{ct.label}</p>
              <p className="text-[#666666] text-xs mt-0.5 leading-snug">{ct.desc}</p>
            </button>
          ))}
        </div>
      </div>
    )
  }

  const selected = CARD_TYPES.find(c => c.type === selectedType)!

  return (
    <div className="space-y-5">
      <button
        onClick={() => setStep('type')}
        className="flex items-center gap-1 text-[#666666] text-sm hover:text-white transition-colors"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
        Back
      </button>

      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-2xl bg-[#111111] flex items-center justify-center text-[#EEEEEE]">
          <CardTypeIcon type={selected.type} />
        </div>
        <div>
          <h1 className="text-xl font-bold text-white">{selected.label}</h1>
          <p className="text-[#666666] text-xs">{selected.desc}</p>
        </div>
      </div>

      {/* Business card form */}
      {selectedType === 'business_card' && (
        <div className="space-y-4">
          <div>
            <label className="block text-xs text-[#666666] mb-2">Fields to include</label>
            <div className="flex flex-wrap gap-2">
              {SOCIAL_FIELDS.map(f => {
                const on = enabledFields.has(f.key as string)
                return (
                  <button
                    key={f.key}
                    onClick={() => toggleField(f.key as string)}
                    title={f.label}
                    className="w-9 h-9 rounded-xl flex items-center justify-center text-sm transition-all border overflow-hidden"
                    style={{
                      background: on ? `${f.color}22` : '#1A1A1A',
                      borderColor: on ? `${f.color}66` : '#383838',
                      color: on ? f.color : '#444444',
                      opacity: on ? 1 : 0.4,
                    }}
                  >
                    {f.icon
                      ? <img src={f.icon} alt={f.label} className="w-5 h-5 object-contain" />
                      : <span>{f.emoji}</span>
                    }
                  </button>
                )
              })}
            </div>
          </div>

          <input
            type="text"
            value={bcData.name}
            onChange={e => updateBc('name', e.target.value)}
            placeholder="Full Name *"
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
          />

          {SOCIAL_FIELDS.filter(f => enabledFields.has(f.key as string)).map(f => (
            <input
              key={f.key}
              type={f.key === 'email' ? 'email' : f.key === 'phone' || f.key === 'whatsapp' ? 'tel' : 'text'}
              value={(bcData as unknown as Record<string, string>)[f.key as string] || ''}
              onChange={e => updateBc(f.key, e.target.value)}
              placeholder={f.label}
              className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
            />
          ))}
        </div>
      )}

      {/* Other card types */}
      {selectedType !== 'business_card' && (
        <div className="space-y-4">
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder={selectedType === 'social_media' ? 'Instagram' : 'Title *'}
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
          />
          {selectedType !== 'file' && (
            <input
              type={selectedType === 'link' || selectedType === 'social_media' ? 'url' : 'text'}
              value={content}
              onChange={e => setContent(e.target.value)}
              placeholder={
                selectedType === 'link' ? 'https://yourwebsite.com' :
                selectedType === 'social_media' ? 'https://instagram.com/yourhandle' :
                'Content'
              }
              className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
            />
          )}
          {selectedType === 'file' && (
            <p className="text-[#444444] text-xs">File upload coming soon. Use a link card to share a file URL for now.</p>
          )}
        </div>
      )}

      {/* Color picker */}
      <ColorPicker
        selected={colorHex}
        isDark={isDark}
        onChange={setColorHex}
        onDarkToggle={() => setIsDark(p => !p)}
      />

      {/* Preview */}
      <div
        className="rounded-2xl p-4 border"
        style={{
          background: isDark ? '#111111' : `linear-gradient(135deg, ${colorHex}, ${CARD_COLORS.find(c => c.hex === colorHex)?.dark ?? colorHex})`,
          borderColor: colorHex + '44',
        }}
      >
        <p className="text-xs font-medium opacity-60 mb-1" style={{ color: isDark ? colorHex : 'white' }}>
          {selected.label}
        </p>
        <p className="font-semibold" style={{ color: isDark ? colorHex : 'white' }}>
          {selectedType === 'business_card' ? (bcData.name || 'Full Name') : (title || 'Card title')}
        </p>
        {selectedType === 'business_card' && bcData.jobTitle && (
          <p className="text-xs mt-0.5 opacity-70" style={{ color: isDark ? colorHex : 'white' }}>
            {[bcData.jobTitle, bcData.company].filter(Boolean).join(' at ')}
          </p>
        )}
      </div>

      {error && <p className="text-red-400 text-xs">{error}</p>}

      <button
        onClick={handleSave}
        disabled={saving}
        className="w-full py-3 rounded-xl bg-[#037A68] hover:bg-[#025E50] text-white font-semibold text-sm transition-colors disabled:opacity-50"
      >
        {saving ? 'Saving...' : 'Save Card'}
      </button>
    </div>
  )
}
