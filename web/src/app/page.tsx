'use client'

import { createClient } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'

export default function Home() {
  const router = useRouter()
  const supabase = createClient()
  const [loading, setLoading] = useState(true)
  const [signingIn, setSigningIn] = useState(false)

  useEffect(() => {
    supabase.auth.getUser().then(({ data: { user } }) => {
      if (user) router.push('/dashboard')
      else setLoading(false)
    })
  }, [])

  const handleGoogleSignIn = async () => {
    setSigningIn(true)
    const { error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: { redirectTo: `${window.location.origin}/auth/callback` },
    })
    if (error) setSigningIn(false)
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#0A0A0A]">
        <div className="w-8 h-8 border-2 border-[#037A68] border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-[#0A0A0A]">
      <div className="w-full max-w-sm px-6">
        <div className="text-center mb-10">
          <div className="flex items-center justify-center gap-3 mb-3">
            <div className="w-12 h-12 rounded-2xl bg-[#037A68] flex items-center justify-center">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.9"/>
                <rect x="13" y="3" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
                <rect x="3" y="13" width="8" height="8" rx="1.5" fill="white" opacity="0.6"/>
                <circle cx="17" cy="17" r="3" fill="#FA5700"/>
              </svg>
            </div>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Nexus</h1>
          <p className="text-[#666666] mt-1.5 text-sm">Your digital pass wallet</p>
        </div>

        <div className="bg-[#1A1A1A] rounded-2xl p-6 border border-[#383838]">
          <button
            onClick={handleGoogleSignIn}
            disabled={signingIn}
            className="w-full flex items-center justify-center gap-3 px-4 py-3 bg-white hover:bg-gray-100 active:bg-gray-200 rounded-xl text-gray-800 font-medium transition-colors disabled:opacity-50"
          >
            {signingIn ? (
              <div className="w-5 h-5 border-2 border-gray-400 border-t-transparent rounded-full animate-spin" />
            ) : (
              <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
            )}
            {signingIn ? 'Signing in...' : 'Continue with Google'}
          </button>
        </div>

        <p className="text-center text-xs text-[#444444] mt-6">
          By signing in you agree to our terms and privacy policy
        </p>
      </div>
    </main>
  )
}
