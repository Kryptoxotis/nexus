'use client'

import { createClient } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'

export default function AuthButton({ email }: { email?: string }) {
  const router = useRouter()
  const supabase = createClient()

  const handleSignOut = async () => {
    await supabase.auth.signOut()
    router.push('/')
  }

  return (
    <div className="flex items-center gap-3">
      {email && (
        <span className="text-sm text-gray-600 truncate max-w-[200px]">{email}</span>
      )}
      <button
        onClick={handleSignOut}
        className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
      >
        Sign Out
      </button>
    </div>
  )
}
