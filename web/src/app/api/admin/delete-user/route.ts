import { createClient as createServerClient } from '@/lib/supabase/server'
import { createClient as createSupabaseClient } from '@supabase/supabase-js'
import { NextResponse } from 'next/server'

export async function POST(req: Request) {
  // Verify the caller is an admin
  const supabase = createServerClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { data: profile } = await supabase
    .schema('nexus')
    .from('profiles')
    .select('account_type')
    .eq('id', user.id)
    .single()

  if (profile?.account_type !== 'admin') {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  }

  const { userId } = await req.json()
  if (!userId || userId === user.id) {
    return NextResponse.json({ error: 'Invalid user ID' }, { status: 400 })
  }

  // Use service role client to delete auth user (cascades to profile via DB trigger)
  const admin = createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.SUPABASE_SERVICE_ROLE_KEY!
  )

  // Delete cards and requests first, then the auth user (profile cascades)
  await admin.schema('nexus').from('personal_cards').delete().eq('user_id', userId)
  await admin.schema('nexus').from('business_requests').delete().eq('user_id', userId)
  await admin.schema('nexus').from('profiles').delete().eq('id', userId)

  const { error } = await admin.auth.admin.deleteUser(userId)
  if (error) return NextResponse.json({ error: error.message }, { status: 500 })

  return NextResponse.json({ success: true })
}
