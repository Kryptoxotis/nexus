import { redirect } from 'next/navigation'
import { createClient, nexus } from '@/lib/supabase/server'
import type { Profile } from '@/lib/types'
import DashboardNav from '@/components/DashboardNav'

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) redirect('/')

  const db = nexus(supabase)
  const { data: profile } = await db
    .from('profiles')
    .select('*')
    .eq('id', user.id)
    .single<Profile>()

  if (!profile) redirect('/')

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white">
      <DashboardNav profile={profile} />
      <main className="max-w-2xl mx-auto px-4 py-6">
        {children}
      </main>
    </div>
  )
}
