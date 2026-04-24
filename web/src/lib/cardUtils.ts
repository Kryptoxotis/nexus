import type { CardType, BusinessCardData } from './types'

export function cardIcon(type: CardType): string {
  switch (type) {
    case 'link': return '🔗'
    case 'social_media': return '📱'
    case 'business_card': return '🪪'
    case 'file': return '📎'
    case 'custom': return '✨'
    case 'contact': return '👤'
    default: return '🃏'
  }
}

export function cardLabel(type: CardType): string {
  switch (type) {
    case 'link': return 'Link'
    case 'social_media': return 'Social media'
    case 'business_card': return 'Nexus card'
    case 'file': return 'File'
    case 'custom': return 'Custom'
    case 'contact': return 'Contact'
    default: return 'Card'
  }
}

export function businessCardSubtitle(data: BusinessCardData): string {
  const parts = [data.jobTitle, data.company].filter(Boolean)
  return parts.length > 0 ? parts.join(' at ') : data.email || data.phone || ''
}

export const SOCIAL_FIELDS: {
  key: keyof BusinessCardData
  label: string
  placeholder: string
  color: string
  icon: string | null  // path to /icons/*.svg, null = use emoji fallback
  emoji: string
}[] = [
  { key: 'jobTitle',  label: 'Job Title',   placeholder: 'Software Engineer',   color: '#B0BEC5', icon: null,                   emoji: '💼' },
  { key: 'company',   label: 'Company',     placeholder: 'Kryptoxotis',         color: '#90A4AE', icon: null,                   emoji: '🏢' },
  { key: 'phone',     label: 'Phone',       placeholder: '+1 555 000 0000',     color: '#037A68', icon: null,                   emoji: '📞' },
  { key: 'email',     label: 'Email',       placeholder: 'you@company.com',     color: '#FA5700', icon: null,                   emoji: '✉️' },
  { key: 'website',   label: 'Website',     placeholder: 'yourwebsite.com',     color: '#037A68', icon: null,                   emoji: '🌐' },
  { key: 'linkedin',  label: 'LinkedIn',    placeholder: 'username',            color: '#0A66C2', icon: '/icons/linkedin.svg',  emoji: 'in' },
  { key: 'instagram', label: 'Instagram',   placeholder: '@username',           color: '#D62976', icon: '/icons/instagram.svg', emoji: '📸' },
  { key: 'twitter',   label: 'X / Twitter', placeholder: '@username',           color: '#EFEFEF', icon: '/icons/x.svg',         emoji: '𝕏'  },
  { key: 'github',    label: 'GitHub',      placeholder: 'username',            color: '#EFEFEF', icon: '/icons/github.svg',    emoji: '⌨️' },
  { key: 'facebook',  label: 'Facebook',    placeholder: 'username',            color: '#1877F2', icon: '/icons/facebook.svg',  emoji: 'fb' },
  { key: 'youtube',   label: 'YouTube',     placeholder: '@channel',            color: '#FF0000', icon: '/icons/youtube.svg',   emoji: '▶️' },
  { key: 'tiktok',    label: 'TikTok',      placeholder: '@username',           color: '#EE1D52', icon: '/icons/tiktok.svg',    emoji: '🎵' },
  { key: 'discord',   label: 'Discord',     placeholder: 'username or invite',  color: '#5865F2', icon: '/icons/discord.svg',   emoji: '💬' },
  { key: 'twitch',    label: 'Twitch',      placeholder: 'username',            color: '#9146FF', icon: '/icons/twitch.svg',    emoji: '🎮' },
  { key: 'whatsapp',  label: 'WhatsApp',    placeholder: '+1 555 000 0000',     color: '#25D366', icon: '/icons/whatsapp.svg',  emoji: '💚' },
]
