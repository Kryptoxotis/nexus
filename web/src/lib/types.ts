export type AccountType = 'individual' | 'business' | 'admin'
export type CardType = 'link' | 'file' | 'contact' | 'social_media' | 'custom'
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
  is_active: boolean
  order_index: number
  created_at: string
  updated_at: string
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
