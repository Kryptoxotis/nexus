export type AccountType = 'individual' | 'business' | 'admin'
export type CardType = 'link' | 'file' | 'contact' | 'social_media' | 'custom' | 'business_card'
export type EnrollmentMode = 'open' | 'pin' | 'invite' | 'closed'

export interface Profile {
  id: string
  email: string | null
  full_name: string | null
  phone: string | null
  avatar_url: string | null
  account_type: AccountType
  status: 'active' | 'suspended' | 'deactivated'
  created_at: string
  updated_at: string
}

export interface PersonalCard {
  id: string
  user_id: string
  card_type: CardType
  title: string
  content: string | null
  icon: string | null
  color: string | null
  image_url: string | null
  card_shape: string
  is_active: boolean
  order_index: number
  created_at: string
  updated_at: string
}

export interface BusinessCardData {
  name: string
  jobTitle: string
  company: string
  phone: string
  email: string
  website: string
  address: string
  linkedin: string
  instagram: string
  twitter: string
  github: string
  facebook: string
  youtube: string
  tiktok: string
  discord: string
  twitch: string
  whatsapp: string
}

export interface Organization {
  id: string
  name: string
  type: string | null
  description: string | null
  logo_url: string | null
  owner_id: string
  enrollment_mode: EnrollmentMode
  static_pin: string | null
  allow_self_enrollment: boolean
  is_active: boolean
  created_at: string
  updated_at: string
}

export interface BusinessPass {
  id: string
  user_id: string
  organization_id: string
  status: 'active' | 'expired' | 'revoked' | 'suspended'
  expires_at: string | null
  use_count: number
  metadata: Record<string, unknown>
  created_at: string
  updated_at: string
}

export interface BusinessRequest {
  id: string
  user_id: string
  business_name: string
  business_type: string | null
  contact_email: string | null
  message: string | null
  status: 'pending' | 'approved' | 'rejected'
  reviewed_by: string | null
  reviewed_at: string | null
  created_at: string
  updated_at: string
}

export interface AccessLog {
  id: string
  card_id: string | null
  card_type: 'personal' | 'business' | null
  user_id: string | null
  organization_id: string | null
  access_granted: boolean
  metadata: Record<string, unknown>
  created_at: string
}

// Color palette matching Android app
export const CARD_COLORS = [
  { name: 'Kryptoxotis Teal', hex: '#0A7968', dark: '#064D42' },
  { name: 'Blaze Orange',     hex: '#F95B1A', dark: '#A83A0E' },
  { name: 'Deep Navy',        hex: '#3355CC', dark: '#11257E' },
  { name: 'Red',              hex: '#FF1744', dark: '#D50000' },
  { name: 'Forest Green',     hex: '#388E3C', dark: '#124116' },
  { name: 'Cyan',             hex: '#00E5FF', dark: '#00838F' },
  { name: 'Purple',           hex: '#B388FF', dark: '#6A1B9A' },
  { name: 'Pink',             hex: '#FF4081', dark: '#AD1457' },
]

export function parseCardColor(stored: string | null): { hex: string; isDark: boolean } {
  if (!stored) return { hex: CARD_COLORS[0].hex, isDark: false }
  const parts = stored.split(':')
  return { hex: parts[0] || CARD_COLORS[0].hex, isDark: parts[1] === 'dark' }
}

export function encodeCardColor(hex: string, isDark: boolean): string {
  return isDark ? `${hex}:dark` : hex
}

export function parseBusinessCard(content: string | null): BusinessCardData {
  if (!content) return emptyBusinessCard()
  try {
    return { ...emptyBusinessCard(), ...JSON.parse(content) }
  } catch {
    return emptyBusinessCard()
  }
}

export function emptyBusinessCard(): BusinessCardData {
  return {
    name: '', jobTitle: '', company: '', phone: '', email: '',
    website: '', address: '', linkedin: '', instagram: '', twitter: '',
    github: '', facebook: '', youtube: '', tiktok: '', discord: '',
    twitch: '', whatsapp: '',
  }
}
