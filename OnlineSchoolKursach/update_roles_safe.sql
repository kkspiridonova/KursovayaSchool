BEGIN;
SELECT id, email, username, role FROM users ORDER BY id;

UPDATE users 
SET role = 'ADMIN' 
WHERE email = 'admin@gmail.com' AND username = 'admin';

UPDATE users 
SET role = 'TEACHER' 
WHERE email = 'teacher@gmail.com' AND username = 'teacher';
SELECT id, email, username, role FROM users ORDER BY id;
COMMIT;

