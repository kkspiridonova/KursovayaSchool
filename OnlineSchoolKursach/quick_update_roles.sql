UPDATE users SET role = 'ADMIN' WHERE email = 'admin@gmail.com';
UPDATE users SET role = 'TEACHER' WHERE email = 'teacher@gmail.com';
SELECT id, email, username, role FROM users;

