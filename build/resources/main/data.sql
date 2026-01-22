--USERS

-- you can user gen_random_uuid () to generate random IDs, use this only to generate testdata


insert into users (id, email,first_name,last_name, password)
values ('ba804cb9-fa14-42a5-afaf-be488742fc54', 'admin@example.com', 'James','Bond', '$2a$10$TM3PAYG3b.H98cbRrHqWa.BM7YyCqV92e/kUTBfj85AjayxGZU7d6' ), -- Password: 1234
('0d8fa44c-54fd-4cd0-ace9-2a7da57992de', 'user@example.com', 'Tyler','Durden', '$2a$10$TM3PAYG3b.H98cbRrHqWa.BM7YyCqV92e/kUTBfj85AjayxGZU7d6') -- Password: 1234
 ON CONFLICT DO NOTHING;


--ROLES
INSERT INTO role(id, name)
VALUES ('d29e709c-0ff1-4f4c-a7ef-09f656c390f1', 'DEFAULT'),
('ab505c92-7280-49fd-a7de-258e618df074', 'ADMIN'),
('c6aee32d-8c35-4481-8b3e-a876a39b0c02', 'USER')
ON CONFLICT DO NOTHING;

-- AUTHORITIES
INSERT INTO authority(id, name)
VALUES
    ('2ebf301e-6c61-4076-98e3-2a38b31daf86', 'USER_CREATE'),
    ('76d2cbf6-5845-470e-ad5f-2edb9e09a868', 'USER_READ'),
    ('21c942db-a275-43f8-bdd6-d048c21bf5ab', 'USER_DEACTIVATE'),
    ('b964fc23-9fea-4ba2-9000-94fad5f0dbe0', 'USER_MODIFY'),
    ('6e12227a-f6bf-4529-86d1-df9b41fe28fb', 'USER_DELETE_OWN_PROFILE')
    ON CONFLICT DO NOTHING;

--assign roles to users
insert into users_role (users_id, role_id)
values ('ba804cb9-fa14-42a5-afaf-be488742fc54', 'd29e709c-0ff1-4f4c-a7ef-09f656c390f1'),
       ('0d8fa44c-54fd-4cd0-ace9-2a7da57992de', 'd29e709c-0ff1-4f4c-a7ef-09f656c390f1'),
       ('ba804cb9-fa14-42a5-afaf-be488742fc54', 'ab505c92-7280-49fd-a7de-258e618df074'),
       ('ba804cb9-fa14-42a5-afaf-be488742fc54', 'c6aee32d-8c35-4481-8b3e-a876a39b0c02')
 ON CONFLICT DO NOTHING;

--assign authorities to roles
INSERT INTO role_authority(role_id, authority_id)
VALUES ('d29e709c-0ff1-4f4c-a7ef-09f656c390f1', '2ebf301e-6c61-4076-98e3-2a38b31daf86'),
('ab505c92-7280-49fd-a7de-258e618df074', '76d2cbf6-5845-470e-ad5f-2edb9e09a868'),
('c6aee32d-8c35-4481-8b3e-a876a39b0c02', '21c942db-a275-43f8-bdd6-d048c21bf5ab')
 ON CONFLICT DO NOTHING;

-- USER role gets USER_READ
INSERT INTO role_authority(role_id, authority_id)
VALUES (
           'c6aee32d-8c35-4481-8b3e-a876a39b0c02', -- USER role
           '76d2cbf6-5845-470e-ad5f-2edb9e09a868'  -- USER_READ
       )
    ON CONFLICT DO NOTHING;

-- USER permissions
INSERT INTO role_authority(role_id, authority_id)
VALUES
    (
        'c6aee32d-8c35-4481-8b3e-a876a39b0c02',
        'b964fc23-9fea-4ba2-9000-94fad5f0dbe0' -- USER_MODIFY
    ),
    (
        'c6aee32d-8c35-4481-8b3e-a876a39b0c02',
        '6e12227a-f6bf-4529-86d1-df9b41fe28fb' -- USER_DELETE_OWN_PROFILE
    )
    ON CONFLICT DO NOTHING;

-- ADMIN gets all authorities
INSERT INTO role_authority(role_id, authority_id)
SELECT
    'ab505c92-7280-49fd-a7de-258e618df074', -- ADMIN role
    a.id
FROM authority a
    ON CONFLICT DO NOTHING;


-- USER PROFILES
-- create profiles for users without profile (age 18–60)
INSERT INTO user_profiles (id, user_id, address, birth_date, profile_image_url)
SELECT
    gen_random_uuid(),
    u.id,
    'Test Street ' || row_number() OVER (),
    CURRENT_DATE - ((18 + floor(random() * 42)) * INTERVAL '1 year'),
    'https://example.com/avatar.png'
FROM users u
         LEFT JOIN user_profiles up ON up.user_id = u.id
WHERE up.user_id IS NULL
ON CONFLICT DO NOTHING;

-- create 30 test users
INSERT INTO users (id, email, first_name, last_name, password)
SELECT
    gen_random_uuid(),
    'user' || gs || '@example.com'        AS email,
    'FirstName' || gs                     AS first_name,
    'LastName' || gs                      AS last_name,
    '$2a$10$TM3PAYG3b.H98cbRrHqWa.BM7YyCqV92e/kUTBfj85AjayxGZU7d6' AS password
FROM generate_series(1, 30) gs
    ON CONFLICT DO NOTHING;

-- assign USER role to all users who don't have a role yet
INSERT INTO users_role (users_id, role_id)
SELECT
    u.id,
    'c6aee32d-8c35-4481-8b3e-a876a39b0c02' -- USER role id
FROM users u
         LEFT JOIN users_role ur ON ur.users_id = u.id
WHERE ur.users_id IS NULL
    ON CONFLICT DO NOTHING;

-- create profiles for users without profile (age >= 18)
INSERT INTO user_profiles (id, user_id, address, birth_date, profile_image_url)
SELECT
    gen_random_uuid(),
    u.id,
    'Test Street ' || row_number() OVER (),
    CURRENT_DATE - ((18 + floor(random() * 42)) * INTERVAL '1 year'),
    'https://example.com/avatar.png'
FROM users u
         LEFT JOIN user_profiles up ON up.user_id = u.id
WHERE up.user_id IS NULL
ON CONFLICT DO NOTHING;

