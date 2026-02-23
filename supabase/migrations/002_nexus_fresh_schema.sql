-- Migration: Nexus fresh schema
-- Drops all old tables and creates the new Nexus schema from scratch.
-- WARNING: This is destructive. All old data will be lost.

-- ============================================================
-- DROP OLD TABLES (CASCADE to remove dependent objects)
-- ============================================================
DROP TABLE IF EXISTS access_logs CASCADE;
DROP TABLE IF EXISTS business_members CASCADE;
DROP TABLE IF EXISTS businesses CASCADE;
DROP TABLE IF EXISTS passes CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;

-- Drop old trigger
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user();

-- ============================================================
-- 1. PROFILES
-- ============================================================
CREATE TABLE profiles (
    id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email text,
    full_name text,
    phone text,
    avatar_url text,
    account_type text NOT NULL DEFAULT 'individual'
        CHECK (account_type IN ('individual', 'business', 'admin')),
    status text NOT NULL DEFAULT 'active'
        CHECK (status IN ('active', 'suspended', 'deactivated')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile"
    ON profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile"
    ON profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile"
    ON profiles FOR INSERT WITH CHECK (auth.uid() = id);

-- ============================================================
-- 2. BUSINESS REQUESTS
-- ============================================================
CREATE TABLE business_requests (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    business_name text NOT NULL,
    business_type text,
    contact_email text,
    message text,
    status text NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'approved', 'rejected')),
    reviewed_by uuid REFERENCES profiles(id),
    reviewed_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE business_requests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own requests"
    ON business_requests FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own requests"
    ON business_requests FOR INSERT WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 3. ORGANIZATIONS
-- ============================================================
CREATE TABLE organizations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    type text,
    description text,
    logo_url text,
    owner_id uuid NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    enrollment_mode text NOT NULL DEFAULT 'open'
        CHECK (enrollment_mode IN ('open', 'pin', 'invite', 'closed')),
    static_pin text,
    allow_self_enrollment boolean NOT NULL DEFAULT true,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can view active organizations"
    ON organizations FOR SELECT USING (is_active = true);
CREATE POLICY "Owners can manage their organization"
    ON organizations FOR ALL USING (auth.uid() = owner_id);

-- ============================================================
-- 4. PERSONAL CARDS
-- ============================================================
CREATE TABLE personal_cards (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    card_type text NOT NULL DEFAULT 'custom'
        CHECK (card_type IN ('link', 'file', 'contact', 'social_media', 'custom')),
    title text NOT NULL,
    content text,
    icon text,
    color text,
    is_active boolean NOT NULL DEFAULT false,
    order_index integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE personal_cards ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own cards"
    ON personal_cards FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own cards"
    ON personal_cards FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own cards"
    ON personal_cards FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own cards"
    ON personal_cards FOR DELETE USING (auth.uid() = user_id);

-- ============================================================
-- 5. BUSINESS PASSES
-- ============================================================
CREATE TABLE business_passes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    status text NOT NULL DEFAULT 'active'
        CHECK (status IN ('active', 'expired', 'revoked', 'suspended')),
    expires_at timestamptz,
    use_count integer NOT NULL DEFAULT 0,
    metadata jsonb DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE(user_id, organization_id)
);

ALTER TABLE business_passes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own passes"
    ON business_passes FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own passes"
    ON business_passes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Org owners can view passes"
    ON business_passes FOR SELECT
    USING (organization_id IN (SELECT id FROM organizations WHERE owner_id = auth.uid()));
CREATE POLICY "Org owners can manage passes"
    ON business_passes FOR ALL
    USING (organization_id IN (SELECT id FROM organizations WHERE owner_id = auth.uid()));

-- ============================================================
-- 6. ENROLLMENT PINS
-- ============================================================
CREATE TABLE enrollment_pins (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    pin_code text NOT NULL,
    is_used boolean NOT NULL DEFAULT false,
    used_by uuid REFERENCES profiles(id),
    expires_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE enrollment_pins ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Org owners can manage pins"
    ON enrollment_pins FOR ALL
    USING (organization_id IN (SELECT id FROM organizations WHERE owner_id = auth.uid()));

-- ============================================================
-- 7. FILE STORAGE LINKS
-- ============================================================
CREATE TABLE file_storage_links (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    original_filename text NOT NULL,
    storage_path text NOT NULL,
    public_url text,
    file_size bigint,
    mime_type text,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE file_storage_links ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own files"
    ON file_storage_links FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own files"
    ON file_storage_links FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete own files"
    ON file_storage_links FOR DELETE USING (auth.uid() = user_id);

-- ============================================================
-- 8. ACCESS LOGS
-- ============================================================
CREATE TABLE access_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id uuid,
    card_type text CHECK (card_type IN ('personal', 'business')),
    user_id uuid REFERENCES profiles(id) ON DELETE SET NULL,
    organization_id uuid REFERENCES organizations(id) ON DELETE SET NULL,
    access_granted boolean NOT NULL DEFAULT true,
    metadata jsonb DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE access_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own logs"
    ON access_logs FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Org owners can view org logs"
    ON access_logs FOR SELECT
    USING (organization_id IN (SELECT id FROM organizations WHERE owner_id = auth.uid()));

-- ============================================================
-- AUTO-PROFILE CREATION TRIGGER
-- ============================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email)
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================
-- UPDATED_AT TRIGGER FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER profiles_updated_at BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER business_requests_updated_at BEFORE UPDATE ON business_requests
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER personal_cards_updated_at BEFORE UPDATE ON personal_cards
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER business_passes_updated_at BEFORE UPDATE ON business_passes
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
