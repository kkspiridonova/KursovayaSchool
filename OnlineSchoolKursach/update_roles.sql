UPDATE users 
SET role = 'ADMIN' 
WHERE id = 3 AND email = 'admin@gmail.com' AND username = 'admin';
UPDATE users 
SET role = 'TEACHER' 
WHERE id = 2 AND email = 'teacher@gmail.com' AND username = 'teacher';
SELECT id, email, username, role FROM users ORDER BY id;

