'use client'

import { createClient } from '@/lib/supabase/client'
import { nexus } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import type { CardType } from '@/lib/types'

const CARD_TYPES: { type: CardType; label: string; desc: string; icon: string }[] = [
  { type: 'link', label: 'Link', desc: 'Opens a URL when tapped', icon: '🔗' },
  { type: 'contact', label: 'Contact', desc: 'Share your contact card', icon: '👤' },
  { type: 'social_media', label: 'Social Media', desc: 'Link to your social profile', icon: '📱' },
  { type: 'custom', label: 'Custom', desc: 'Custom text or data', icon: '✨' },
]

export default function CreateCardPage() {
  const router = useRouter()
  const [step, setStep] = useState<'type' | 'details'>('type')
  const [selectedType, setSelectedType] = useState<CardType | null>(null)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleTypeSelect = (type: CardType) => {
    setSelectedType(type)
    setStep('details')
  }

  const handleSave = async () => {
    if (!title.trim() || !selectedType) return
    setSaving(true)
    setError('')

    const supabase = createClient()
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) { router.push('/'); return }

    const db = nexus(supabase)
    const { error: err } = await db.from('personal_cards').insert({
      user_id: user.id,
      card_type: selectedType,
      title: title.trim(),
      content: content.trim() || null,
      is_active: true,
      order_index: 0,
    })

    if (err) {
      setError('Failed to save card. Try again.')
      setSaving(false)
    } else {
      router.push('/dashboard')
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
          {CARD_TYPES.map((ct) => (
            <button
              key={ct.type}
              onClick={() => handleTypeSelect(ct.type)}
              className="bg-[#1A1A1A] rounded-2xl border border-[#383838] p-4 text-left hover:border-[#037A68] active:scale-[0.98] transition-all"
            >
              <div className="w-10 h-10 rounded-xl bg-[#111111] flex items-center justify-center text-xl mb-3">
                {ct.icon}
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
    <div>
      <button
        onClick={() => setStep('type')}
        className="flex items-center gap-1 text-[#666666] text-sm mb-6 hover:text-white transition-colors"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
        Back
      </button>

      <div className="mb-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#111111] flex items-center justify-center text-xl">
            {selected.icon}
          </div>
          <div>
            <h1 className="text-xl font-bold text-white">{selected.label}</h1>
            <p className="text-[#666666] text-xs">{selected.desc}</p>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <label className="block text-xs text-[#666666] mb-1.5">Title</label>
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder={selectedType === 'social_media' ? 'Instagram' : 'Card name'}
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
          />
        </div>

        <div>
          <label className="block text-xs text-[#666666] mb-1.5">
            {selectedType === 'link' || selectedType === 'social_media' ? 'URL' : 'Content'}
          </label>
          <input
            type={selectedType === 'link' || selectedType === 'social_media' ? 'url' : 'text'}
            value={content}
            onChange={e => setContent(e.target.value)}
            placeholder={
              selectedType === 'link' ? 'https://yourwebsite.com' :
              selectedType === 'social_media' ? 'https://instagram.com/yourhandle' :
              selectedType === 'contact' ? 'Phone, email, or note' :
              'Your content here'
            }
            className="w-full bg-[#1A1A1A] border border-[#383838] rounded-xl px-4 py-3 text-white text-sm placeholder-[#444444] focus:outline-none focus:border-[#037A68] transition-colors"
          />
        </div>

        {error && <p className="text-red-400 text-xs">{error}</p>}

        <button
          onClick={handleSave}
          disabled={!title.trim() || saving}
          className="w-full py-3 rounded-xl bg-[#037A68] hover:bg-[#025E50] text-white font-semibold text-sm transition-colors disabled:opacity-50"
        >
          {saving ? 'Saving...' : 'Save Card'}
        </button>
      </div>
    </div>
  )
}
