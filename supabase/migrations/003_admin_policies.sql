-- Migration: Admin RLS policies
-- Admins can read/update all profiles, business requests, organizations, and logs.

-- Admin helper function
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM profiles
        WHERE id = auth.uid() AND account_type = 'admin'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Profiles: admin can view and update all
CREATE POLICY "Admins can view all profiles"
    ON profiles FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update all profiles"
    ON profiles FOR UPDATE USING (public.is_admin());

-- Business requests: admin can view and update all
CREATE POLICY "Admins can view all business requests"
    ON business_requests FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update all business requests"
    ON business_requests FOR UPDATE USING (public.is_admin());

-- Organizations: admin can view and manage all
CREATE POLICY "Admins can view all organizations"
    ON organizations FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update all organizations"
    ON organizations FOR UPDATE USING (public.is_admin());

-- Business passes: admin can view all
CREATE POLICY "Admins can view all business passes"
    ON business_passes FOR SELECT USING (public.is_admin());

-- Access logs: admin can view all
CREATE POLICY "Admins can view all access logs"
    ON access_logs FOR SELECT USING (public.is_admin());
