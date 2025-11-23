
INSERT INTO course_statuses (course_status_id, status_name) 
SELECT 4, 'Архивный'
WHERE NOT EXISTS (SELECT 1 FROM course_statuses WHERE course_status_id = 4);

UPDATE courses SET course_status_id = 4 WHERE course_status_id = 3 
  AND EXISTS (SELECT 1 FROM course_statuses WHERE course_status_id = 3 AND status_name = 'Завершен');

UPDATE courses SET course_status_id = 4 WHERE course_status_id NOT IN (1, 2, 3, 4);

UPDATE course_statuses SET status_name = 'Идет набор' WHERE course_status_id = 1;
UPDATE course_statuses SET status_name = 'Заполнен' WHERE course_status_id = 2;
UPDATE course_statuses SET status_name = 'Активен' WHERE course_status_id = 3;
UPDATE course_statuses SET status_name = 'Архивный' WHERE course_status_id = 4;

SELECT setval('course_statuses_course_status_id_seq', 4, true);

