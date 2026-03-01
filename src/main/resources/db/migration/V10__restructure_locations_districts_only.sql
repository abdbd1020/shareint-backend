-- V10: Replace hierarchical location data (Division → District → Upazilla)
--      with a flat list of exactly 64 districts of Bangladesh.
--      TRUNCATE CASCADE removes all dependent trips, bookings, seat records, etc.

TRUNCATE TABLE locations CASCADE;

-- Insert 64 districts (flat — no parent_id)
INSERT INTO locations (id, name_en, name_bn, parent_id, is_active, is_distance_considered)
VALUES
    -- Dhaka Division (13)
    (gen_random_uuid(), 'Dhaka',        'ঢাকা',           NULL, true, true),
    (gen_random_uuid(), 'Gazipur',      'গাজীপুর',        NULL, true, true),
    (gen_random_uuid(), 'Manikganj',    'মানিকগঞ্জ',      NULL, true, true),
    (gen_random_uuid(), 'Munshiganj',   'মুন্সিগঞ্জ',     NULL, true, true),
    (gen_random_uuid(), 'Narayanganj',  'নারায়ণগঞ্জ',    NULL, true, true),
    (gen_random_uuid(), 'Narsingdi',    'নরসিংদী',        NULL, true, true),
    (gen_random_uuid(), 'Faridpur',     'ফরিদপুর',        NULL, true, true),
    (gen_random_uuid(), 'Gopalganj',    'গোপালগঞ্জ',      NULL, true, true),
    (gen_random_uuid(), 'Madaripur',    'মাদারীপুর',      NULL, true, true),
    (gen_random_uuid(), 'Rajbari',      'রাজবাড়ী',       NULL, true, true),
    (gen_random_uuid(), 'Shariatpur',   'শরীয়তপুর',      NULL, true, true),
    (gen_random_uuid(), 'Kishoreganj',  'কিশোরগঞ্জ',      NULL, true, true),
    (gen_random_uuid(), 'Tangail',      'টাঙ্গাইল',       NULL, true, true),

    -- Chattagram Division (11)
    (gen_random_uuid(), 'Chattagram',       'চট্টগ্রাম',       NULL, true, true),
    (gen_random_uuid(), 'Cox''s Bazar',     'কক্সবাজার',       NULL, true, true),
    (gen_random_uuid(), 'Rangamati',        'রাঙ্গামাটি',      NULL, true, true),
    (gen_random_uuid(), 'Bandarban',        'বান্দরবান',       NULL, true, true),
    (gen_random_uuid(), 'Khagrachhari',     'খাগড়াছড়ি',      NULL, true, true),
    (gen_random_uuid(), 'Feni',             'ফেনী',            NULL, true, true),
    (gen_random_uuid(), 'Lakshmipur',       'লক্ষ্মীপুর',     NULL, true, true),
    (gen_random_uuid(), 'Noakhali',         'নোয়াখালী',       NULL, true, true),
    (gen_random_uuid(), 'Comilla',          'কুমিল্লা',        NULL, true, true),
    (gen_random_uuid(), 'Brahmanbaria',     'ব্রাহ্মণবাড়িয়া', NULL, true, true),
    (gen_random_uuid(), 'Chandpur',         'চাঁদপুর',         NULL, true, true),

    -- Rajshahi Division (8)
    (gen_random_uuid(), 'Rajshahi',         'রাজশাহী',         NULL, true, true),
    (gen_random_uuid(), 'Bogura',           'বগুড়া',          NULL, true, true),
    (gen_random_uuid(), 'Joypurhat',        'জয়পুরহাট',       NULL, true, true),
    (gen_random_uuid(), 'Naogaon',          'নওগাঁ',           NULL, true, true),
    (gen_random_uuid(), 'Natore',           'নাটোর',           NULL, true, true),
    (gen_random_uuid(), 'Chapai Nawabganj', 'চাঁপাইনবাবগঞ্জ', NULL, true, true),
    (gen_random_uuid(), 'Pabna',            'পাবনা',           NULL, true, true),
    (gen_random_uuid(), 'Sirajganj',        'সিরাজগঞ্জ',       NULL, true, true),

    -- Khulna Division (10)
    (gen_random_uuid(), 'Khulna',       'খুলনা',     NULL, true, true),
    (gen_random_uuid(), 'Bagerhat',     'বাগেরহাট',  NULL, true, true),
    (gen_random_uuid(), 'Chuadanga',    'চুয়াডাঙ্গা', NULL, true, true),
    (gen_random_uuid(), 'Jashore',      'যশোর',      NULL, true, true),
    (gen_random_uuid(), 'Jhenaidah',    'ঝিনাইদহ',   NULL, true, true),
    (gen_random_uuid(), 'Kushtia',      'কুষ্টিয়া',  NULL, true, true),
    (gen_random_uuid(), 'Magura',       'মাগুরা',    NULL, true, true),
    (gen_random_uuid(), 'Meherpur',     'মেহেরপুর',  NULL, true, true),
    (gen_random_uuid(), 'Narail',       'নড়াইল',    NULL, true, true),
    (gen_random_uuid(), 'Satkhira',     'সাতক্ষীরা', NULL, true, true),

    -- Barishal Division (6)
    (gen_random_uuid(), 'Barishal',     'বরিশাল',    NULL, true, true),
    (gen_random_uuid(), 'Barguna',      'বরগুনা',    NULL, true, true),
    (gen_random_uuid(), 'Bhola',        'ভোলা',      NULL, true, true),
    (gen_random_uuid(), 'Jhalokati',    'ঝালকাঠি',   NULL, true, true),
    (gen_random_uuid(), 'Patuakhali',   'পটুয়াখালী', NULL, true, true),
    (gen_random_uuid(), 'Pirojpur',     'পিরোজপুর',  NULL, true, true),

    -- Sylhet Division (4)
    (gen_random_uuid(), 'Sylhet',       'সিলেট',        NULL, true, true),
    (gen_random_uuid(), 'Habiganj',     'হবিগঞ্জ',      NULL, true, true),
    (gen_random_uuid(), 'Moulvibazar',  'মৌলভীবাজার',   NULL, true, true),
    (gen_random_uuid(), 'Sunamganj',    'সুনামগঞ্জ',    NULL, true, true),

    -- Rangpur Division (8)
    (gen_random_uuid(), 'Rangpur',      'রংপুর',        NULL, true, true),
    (gen_random_uuid(), 'Dinajpur',     'দিনাজপুর',     NULL, true, true),
    (gen_random_uuid(), 'Gaibandha',    'গাইবান্ধা',    NULL, true, true),
    (gen_random_uuid(), 'Kurigram',     'কুড়িগ্রাম',   NULL, true, true),
    (gen_random_uuid(), 'Lalmonirhat',  'লালমনিরহাট',   NULL, true, true),
    (gen_random_uuid(), 'Nilphamari',   'নীলফামারী',    NULL, true, true),
    (gen_random_uuid(), 'Panchagarh',   'পঞ্চগড়',      NULL, true, true),
    (gen_random_uuid(), 'Thakurgaon',   'ঠাকুরগাঁও',    NULL, true, true),

    -- Mymensingh Division (4)
    (gen_random_uuid(), 'Mymensingh',   'ময়মনসিংহ',    NULL, true, true),
    (gen_random_uuid(), 'Jamalpur',     'জামালপুর',     NULL, true, true),
    (gen_random_uuid(), 'Netrokona',    'নেত্রকোণা',    NULL, true, true),
    (gen_random_uuid(), 'Sherpur',      'শেরপুর',       NULL, true, true);
