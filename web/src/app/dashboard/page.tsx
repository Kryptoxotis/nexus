import { redirect } from 'next/navigation'
import { createClient } from '@/lib/supabase/server'
import type { Profile } from '@/lib/types'

export default async function DashboardHome() {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()

  if (!user) redirect('/')

  const { data: profile } = await supabase
    .from('profiles')
    .select('*')
    .eq('user_id', user.id)
    .single<Profile>()

  // Redirect based on role
  if (profile?.current_role === 'business') {
    redirect('/dashboard/business')
  }

  redirect('/dashboard/passes')
}
