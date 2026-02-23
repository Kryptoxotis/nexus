-- Migration: Add profiles, businesses, business_members tables
-- Also adds link and business_id columns to passes

-- Add link column to passes (exists in Room DB but not Supabase)
ALTER TABLE passes ADD COLUMN IF NOT EXISTS link text;

-- Create profiles table
CREATE TABLE IF NOT EXISTS profiles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL UNIQUE,
  display_name text,
  email text,
  avatar_url text,
  current_role text DEFAULT 'personal' CHECK (current_role IN ('personal', 'business')),
  created_at timestamp DEFAULT now(),
  updated_at timestamp DEFAULT now()
);
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view own profile" ON profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Create businesses table
CREATE TABLE IF NOT EXISTS businesses (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id uuid REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  name text NOT NULL,
  description text,
  category text,
  logo_url text,
  is_active boolean DEFAULT true,
  created_at timestamp DEFAULT now(),
  updated_at timestamp DEFAULT now()
);
ALTER TABLE businesses ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can view active businesses" ON businesses FOR SELECT USING (is_active = true);
CREATE POLICY "Owners can manage their business" ON businesses FOR ALL USING (auth.uid() = owner_id);

-- Now add business_id to passes (businesses table must exist first)
ALTER TABLE passes ADD COLUMN IF NOT EXISTS business_id uuid REFERENCES businesses(id) ON DELETE SET NULL;

-- Create business_members table
CREATE TABLE IF NOT EXISTS business_members (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  business_id uuid REFERENCES businesses(id) ON DELETE CASCADE NOT NULL,
  user_id uuid REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  role text DEFAULT 'member' CHECK (role IN ('owner', 'admin', 'member')),
  status text DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'pending')),
  joined_at timestamp DEFAULT now(),
  UNIQUE(business_id, user_id)
);
ALTER TABLE business_members ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Members can view their business members" ON business_members FOR SELECT
  USING (business_id IN (SELECT business_id FROM business_members WHERE user_id = auth.uid()));
CREATE POLICY "Owners can manage members" ON business_members FOR ALL
  USING (business_id IN (SELECT id FROM businesses WHERE owner_id = auth.uid()));
CREATE POLICY "Users can view their own memberships" ON business_members FOR SELECT
  USING (auth.uid() = user_id);

-- Auto-create profile on signup (trigger)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.profiles (user_id, email, display_name)
  VALUES (NEW.id, NEW.email, COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
