INSERT INTO admins (first_name, last_name, dni, email, role, password, active)
VALUES (
    'Admin',
    'Almedin',
    '00000000',
    'admin@almedin.com',
    'ADMIN',
    '$2a$12$srZWl1T7N3/sFbbN.09UzO7QALs/fgKlPzZPw0/mTKbtnRHsvKft2',
    true
)
ON CONFLICT (email) DO NOTHING;