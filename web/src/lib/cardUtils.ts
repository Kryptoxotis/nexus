import type { CardType } from './types'

export function cardIcon(type: CardType): string {
  switch (type) {
    case 'link': return '🔗'
    case 'contact': return '👤'
    case 'social_media': return '📱'
    case 'file': return '📎'
    case 'custom': return '✨'
    default: return '🃏'
  }
}

export function cardLabel(type: CardType): string {
  switch (type) {
    case 'link': return 'Link'
    case 'contact': return 'Contact card'
    case 'social_media': return 'Social media'
    case 'file': return 'File'
    case 'custom': return 'Custom'
    default: return 'Card'
  }
}
