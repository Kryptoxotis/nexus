'use client'

import { createClient, nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import type { BusinessCardData } from '@/lib/types'
import { CARD_COLORS, encodeCardColor, emptyBusinessCard } from '@/lib/types'
import { SOCIAL_FIELDS } from '@/lib/cardUtils'
import Link from 'next/link'

const DEFAULT_FIELDS = new Set(['name', 'jobTitle', 'company', 'phone', 'email'])

function ColorSwatches({ selected, onChange }: { selected: string; onChange: (hex: string) => void }) {
  const current = CARD_COLORS.find(c => c.hex === selected)
  return (
    <div>
      <p className="text-[#666666] text-xs mb-2">Color</p>
      <div className="flex gap-2 flex-wrap mb-1">
        {CARD_COLORS.map(c => (
          <button
            key={c.hex}
            onClick={() => onChange(c.hex)}
            className="w-10 h-10 rounded-full transition-all"
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

export default function NexusCreatePage() {
  const router = useRouter()
  const [bcData, setBcData] = useState<BusinessCardData>(emptyBusinessCard())
  const [enabledFields, setEnabledFields] = useState<Set<string>>(new Set(DEFAULT_FIELDS))
  const [colorHex, setColorHex] = useState(CARD_COLORS[0].hex)
  const [cardMode, setCardMode] = useState<'light' | 'dark'>('light')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [autoUser, setAutoUser] = useState('')
  const [autoEmail, setAutoEmail] = useState('')

  const isDark = cardMode === 'dark'
  const colorEntry = CARD_COLORS.find(c => c.hex === colorHex)
  const bg = isDark ? '#111111' : `linear-gradient(135deg, ${colorHex}, ${colorEntry?.dark ?? colorHex})`
  const textColor = isDark ? colorHex : 'white'

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
    if (!bcData.name.trim()) { setError('Full Name is required'); return }

    setSaving(true)
    setError('')

    const supabase = createClient()
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) { router.push('/'); return }

    const filtered: Partial<BusinessCardData> = { name: bcData.name }
    enabledFields.forEach(k => {
      const val = (bcData as unknown as Record<string, string>)[k]
      if (val) (filtered as unknown as Record<string, string>)[k] = val
    })

    const db = nexus(supabase)
    const { error: err } = await db.from('personal_cards').insert({
      user_id: user.id,
      card_type: 'business_card',
      title: bcData.name.trim(),
      content: JSON.stringify(filtered),
      color: encodeCardColor(colorHex, isDark),
      is_active: true,
      order_index: 0,
      card_shape: 'card',
    })

    if (err) {
      setError('Failed to save. Try again.')
      setSaving(false)
    } else {
      router.push('/dashboard/nexus')
      router.refresh()
    }
  }

  const previewSubtitle = [bcData.jobTitle, bcData.company].filter(Boolean).join(' at ') || undefined

  return (
    <div className="space-y-5 pb-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard/nexus" className="flex items-center gap-1 text-[#666666] text-sm hover:text-white transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 18l-6-6 6-6"/></svg>
          Back
        </Link>
      </div>

      <div>
        <h1 className="text-xl font-bold text-white">My Nexus</h1>
        <p className="text-[#037A68] text-sm">Your digital identity card</p>
      </div>

      {/* Card Preview */}
      <div className="rounded-2xl p-5 aspect-video flex flex-col justify-between border relative overflow-hidden" style={{ background: bg, borderColor: colorHex + '44' }}>
        <div />
        <div>
          <p className="font-bold text-2xl leading-tight" style={{ color: textColor }}>{bcData.name || 'Full Name'}</p>
          {previewSubtitle && <p className="text-sm opacity-70 mt-0.5" style={{ color: textColor }}>{previewSubtitle}</p>}
        </div>
        <div className="absolute bottom-3 right-3 opacity-50">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke={textColor} strokeWidth="1.5">
            <rect x="3" y="3" width="18" height="18" rx="3"/>
            <path d="M8.5 15.5a5 5 0 0 1 0-7"/>
            <path d="M11 13a2 2 0 0 1 0-2.8"/>
            <path d="M6 17.5a8 8 0 0 1 0-11"/>
          </svg>
        </div>
      </div>

      {/* Fields to include */}
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

      {/* Form fields */}
      <div className="space-y-3">
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

      {/* Color */}
      <ColorSwatches selected={colorHex} onChange={setColorHex} />

      {/* Light/Dark mode */}
      <div>
        <p className="text-[#666666] text-xs mb-2">Card Mode</p>
        <div className="grid grid-cols-2 gap-2">
          {(['light', 'dark'] as const).map(mode => (
            <button
              key={mode}
              onClick={() => setCardMode(mode)}
              className="py-3 rounded-2xl text-sm font-semibold capitalize transition-colors border"
              style={{
                background: cardMode === mode ? '#1A1A1A' : '#111111',
                color: cardMode === mode ? '#EEEEEE' : '#666666',
                borderColor: cardMode === mode ? '#383838' : '#222222',
              }}
            >
              {mode}
            </button>
          ))}
        </div>
      </div>

      {error && <p className="text-red-400 text-xs">{error}</p>}

      <button
        onClick={handleSave}
        disabled={saving}
        className="w-full py-4 rounded-2xl text-white font-bold text-sm transition-all disabled:opacity-40"
        style={{ background: saving ? '#025E50' : '#037A68' }}
      >
        {saving ? 'Saving...' : 'Save Card'}
      </button>
    </div>
  )
}
