-- Auto-approve all existing vehicles.
-- Vehicles were previously created with is_approved = false by default,
-- but no admin approval workflow exists yet. This unblocks trip publishing
-- for all current drivers until the admin workflow is implemented.
UPDATE vehicles SET is_approved = true WHERE is_approved = false;
