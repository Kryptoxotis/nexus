export interface Profile {
  id: string
  user_id: string
  display_name: string | null
  email: string | null
  avatar_url: string | null
  current_role: 'personal' | 'business'
  created_at: string
  updated_at: string
}

export interface Pass {
  id: string
  user_id: string
  pass_id: string
  pass_name: string
  organization: string
  is_active: boolean
  expiry_date: string | null
  link: string | null
  business_id: string | null
  created_at: string
  updated_at: string
}

export interface Business {
  id: string
  owner_id: string
  name: string
  description: string | null
  category: string | null
  logo_url: string | null
  is_active: boolean
  created_at: string
  updated_at: string
}

export interface BusinessMember {
  id: string
  business_id: string
  user_id: string
  role: 'owner' | 'admin' | 'member'
  status: 'active' | 'inactive' | 'pending'
  joined_at: string
}

export interface AccessLog {
  id: string
  pass_id: string
  user_id: string
  action: string
  timestamp: string
  metadata: Record<string, unknown> | null
}
