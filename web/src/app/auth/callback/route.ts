import { createClient, nexus } from '@/lib/supabase/server'
import { NextResponse } from 'next/server'

export async function GET(request: Request) {
  const { searchParams, origin } = new URL(request.url)
  const code = searchParams.get('code')

  if (code) {
    const supabase = createClient()
    const { data: { user }, error } = await supabase.auth.exchangeCodeForSession(code)

    if (!error && user) {
      const db = nexus(supabase)

      const { data: profile } = await db
        .from('profiles')
        .select('id')
        .eq('id', user.id)
        .single()

      if (!profile) {
        await db.from('profiles').insert({
          id: user.id,
          email: user.email,
          full_name: user.user_metadata?.full_name ?? user.email,
          account_type: 'individual',
          status: 'active',
        })
      }

      return NextResponse.redirect(`${origin}/dashboard`)
    }
  }

  return NextResponse.redirect(`${origin}/?error=auth`)
}
