-- ============================================================================
-- СКРИПТ СОЗДАНИЯ БАЗЫ ДАННЫХ ДЛЯ ОНЛАЙН ШКОЛЫ
-- Версия: финальная (с исправлениями)
-- ============================================================================

-- Удаление существующих объектов (для чистой установки)

DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS files CASCADE;
DROP TABLE IF EXISTS certificates CASCADE;
DROP TABLE IF EXISTS gift_cards CASCADE;
DROP TABLE IF EXISTS checks CASCADE;
DROP TABLE IF EXISTS solutions CASCADE;
DROP TABLE IF EXISTS grades CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP VIEW IF EXISTS v_course_statistics CASCADE;
DROP VIEW IF EXISTS v_user_enrollments CASCADE;
DROP VIEW IF EXISTS v_teacher_courses_summary CASCADE;

DROP FUNCTION IF EXISTS calculate_course_revenue(INT) CASCADE;
DROP FUNCTION IF EXISTS update_course_status_automatically() CASCADE;
DROP FUNCTION IF EXISTS get_student_progress(INT, INT) CASCADE;
DROP FUNCTION IF EXISTS audit_users_trigger_function() CASCADE;
DROP FUNCTION IF EXISTS audit_courses_trigger_function() CASCADE;
DROP FUNCTION IF EXISTS audit_enrollments_trigger_function() CASCADE;

DROP TABLE IF EXISTS certificate_statuses CASCADE;
DROP TABLE IF EXISTS gift_card_statuses CASCADE;
DROP TABLE IF EXISTS payment_statuses CASCADE;
DROP TABLE IF EXISTS solution_statuses CASCADE;
DROP TABLE IF EXISTS task_statuses CASCADE;
DROP TABLE IF EXISTS enrollment_statuses CASCADE;
DROP TABLE IF EXISTS lesson_statuses CASCADE;
DROP TABLE IF EXISTS course_statuses CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ============================================================================
-- 1. ТАБЛИЦЫ СТАТУСОВ И СПРАВОЧНИКИ
-- ============================================================================

-- Создание таблицы ролей
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_role_name_length CHECK (LENGTH(role_name) > 0)
);

-- Создание таблицы категорий курсов
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT chk_category_name_length CHECK (LENGTH(category_name) > 0)
);

-- Создание таблицы статусов курсов
CREATE TABLE course_statuses (
    course_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_course_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов уроков
CREATE TABLE lesson_statuses (
    lesson_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_lesson_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов записей
CREATE TABLE enrollment_statuses (
    enrollment_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_enrollment_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов заданий
CREATE TABLE task_statuses (
    task_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_task_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов решений
CREATE TABLE solution_statuses (
    solution_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_solution_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов платежей
CREATE TABLE payment_statuses (
    payment_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_payment_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов подарочных карт
CREATE TABLE gift_card_statuses (
    gift_card_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_gift_card_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- Создание таблицы статусов сертификатов
CREATE TABLE certificate_statuses (
    certificate_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT chk_certificate_status_name_length CHECK (LENGTH(status_name) > 0)
);

-- ============================================================================
-- 2. ОСНОВНЫЕ ТАБЛИЦЫ
-- ============================================================================

-- Создание таблицы пользователей
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    middle_name VARCHAR(25),
    email VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    image_url VARCHAR(1000),
    CONSTRAINT chk_first_name_length CHECK (LENGTH(first_name) > 0 AND LENGTH(first_name) <= 25),
    CONSTRAINT chk_last_name_length CHECK (LENGTH(last_name) > 0 AND LENGTH(last_name) <= 25),
    CONSTRAINT chk_middle_name_length CHECK (middle_name IS NULL OR (LENGTH(middle_name) > 0 AND LENGTH(middle_name) <= 25)),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_hash_length CHECK (LENGTH(password_hash) >= 10),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);

-- Создание таблицы курсов
CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    teacher_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    course_status_id INT NOT NULL,
    category_id INT,
    image_url VARCHAR(1000),
    capacity INT,
    start_date DATE,
    end_date DATE,
    CONSTRAINT chk_title_length CHECK (LENGTH(title) > 0 AND LENGTH(title) <= 100),
    CONSTRAINT chk_description_length CHECK (LENGTH(description) > 0),
    CONSTRAINT chk_price_positive CHECK (price > 0),
    CONSTRAINT chk_capacity_positive CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT chk_dates_valid CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_courses_status FOREIGN KEY (course_status_id) REFERENCES course_statuses(course_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_courses_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- Создание таблицы уроков
CREATE TABLE lessons (
    lesson_id SERIAL PRIMARY KEY,
    course_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    attached_file VARCHAR(255),
    lesson_status_id INT NOT NULL,
    CONSTRAINT chk_lesson_title_length CHECK (LENGTH(title) > 0 AND LENGTH(title) <= 100),
    CONSTRAINT chk_lesson_content_length CHECK (LENGTH(content) > 0),
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_lessons_status FOREIGN KEY (lesson_status_id) REFERENCES lesson_statuses(lesson_status_id) ON DELETE RESTRICT
);

-- Создание таблицы заданий
CREATE TABLE tasks (
    task_id SERIAL PRIMARY KEY,
    lesson_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    deadline DATE NOT NULL,
    attached_file VARCHAR(255),
    task_status_id INT NOT NULL,
    CONSTRAINT chk_task_title_length CHECK (LENGTH(title) > 0 AND LENGTH(title) <= 100),
    CONSTRAINT chk_task_description_length CHECK (LENGTH(description) > 0),
    CONSTRAINT fk_tasks_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_status FOREIGN KEY (task_status_id) REFERENCES task_statuses(task_status_id) ON DELETE RESTRICT
);

-- Создание таблицы записей на курсы (ИСПРАВЛЕНО: только enrollment_date, без date)
CREATE TABLE enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    enrollment_status_id INT NOT NULL,
    CONSTRAINT chk_enrollment_date CHECK (enrollment_date <= CURRENT_DATE),
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_status FOREIGN KEY (enrollment_status_id) REFERENCES enrollment_statuses(enrollment_status_id) ON DELETE RESTRICT,
    CONSTRAINT uk_enrollments_user_course UNIQUE (user_id, course_id)
);

-- Создание таблицы оценок (ИСПРАВЛЕНО: пятибалльная система 0-5, без feedback)
CREATE TABLE grades (
    grade_id SERIAL PRIMARY KEY,
    grade_value INT NOT NULL,
    CONSTRAINT chk_grade_value_range CHECK (grade_value >= 0 AND grade_value <= 5)
);

-- Создание таблицы решений
CREATE TABLE solutions (
    solution_id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    answer_text TEXT,
    answer_file VARCHAR(255),
    submit_date DATE NOT NULL DEFAULT CURRENT_DATE,
    solution_status_id INT NOT NULL,
    grade_id INT,
    CONSTRAINT chk_solution_submit_date CHECK (submit_date <= CURRENT_DATE),
    CONSTRAINT fk_solutions_task FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_solutions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_solutions_status FOREIGN KEY (solution_status_id) REFERENCES solution_statuses(solution_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_solutions_grade FOREIGN KEY (grade_id) REFERENCES grades(grade_id) ON DELETE SET NULL
);

-- Создание таблицы чеков
CREATE TABLE checks (
    check_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    payment_status_id INT NOT NULL,
    CONSTRAINT chk_check_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_check_payment_date CHECK (payment_date <= CURRENT_DATE),
    CONSTRAINT fk_checks_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_checks_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL,
    CONSTRAINT fk_checks_status FOREIGN KEY (payment_status_id) REFERENCES payment_statuses(payment_status_id) ON DELETE RESTRICT
);

-- Создание таблицы подарочных карт
CREATE TABLE gift_cards (
    gift_card_id SERIAL PRIMARY KEY,
    card_number VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    amount DECIMAL(10,2) NOT NULL,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date DATE,
    user_id INT,
    course_id INT,
    gift_card_status_id INT NOT NULL,
    CONSTRAINT chk_gift_card_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_gift_card_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_gift_card_balance_not_exceed_amount CHECK (balance <= amount),
    CONSTRAINT chk_gift_card_expiry_date CHECK (expiry_date IS NULL OR expiry_date >= issue_date),
    CONSTRAINT fk_gift_cards_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_gift_cards_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL,
    CONSTRAINT fk_gift_cards_status FOREIGN KEY (gift_card_status_id) REFERENCES gift_card_statuses(gift_card_status_id) ON DELETE RESTRICT
);

-- Создание таблицы сертификатов
CREATE TABLE certificates (
    certificate_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    certificate_number VARCHAR(100) NOT NULL UNIQUE,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    file_path VARCHAR(1000),
    document_file VARCHAR(1000) NOT NULL,
    certificate_status_id INT NOT NULL,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent_date DATE,
    CONSTRAINT chk_certificate_number_length CHECK (LENGTH(certificate_number) > 0),
    CONSTRAINT chk_certificate_document_file_length CHECK (LENGTH(document_file) > 0),
    CONSTRAINT chk_certificate_email_sent_date CHECK (email_sent_date IS NULL OR email_sent_date >= issue_date),
    CONSTRAINT fk_certificates_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_status FOREIGN KEY (certificate_status_id) REFERENCES certificate_statuses(certificate_status_id) ON DELETE RESTRICT
);

-- Создание таблицы файлов
CREATE TABLE files (
    id SERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    uploaded_by INT NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    CONSTRAINT chk_file_original_name_length CHECK (LENGTH(original_name) > 0),
    CONSTRAINT chk_file_path_length CHECK (LENGTH(file_path) > 0),
    CONSTRAINT chk_file_size_positive CHECK (file_size IS NULL OR file_size > 0),
    CONSTRAINT fk_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Создание таблицы комментариев
CREATE TABLE comments (
    comment_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    lesson_id INT,
    task_id INT,
    parent_comment_id INT,
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_comment_text_length CHECK (LENGTH(text) > 0),
    CONSTRAINT chk_comment_reference CHECK ((lesson_id IS NOT NULL) OR (task_id IS NOT NULL)),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE
);

-- Создание таблицы токенов сброса пароля
CREATE TABLE password_reset_tokens (
    token_id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_token_length CHECK (LENGTH(token) > 0),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ============================================================================
-- 3. ТАБЛИЦА АУДИТА
-- ============================================================================

CREATE TABLE audit_log (
    audit_id SERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id INT NOT NULL,
    action VARCHAR(10) NOT NULL, 
    user_id INT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_values JSONB,
    new_values JSONB,
    CONSTRAINT chk_audit_action CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Индексы для аудита
CREATE INDEX idx_audit_log_table_record ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_changed_at ON audit_log(changed_at);

-- ============================================================================
-- 4. ПРЕДСТАВЛЕНИЯ (VIEW) ДЛЯ ОТЧЁТНОСТИ
-- ============================================================================

-- Представление статистики по курсам
CREATE VIEW v_course_statistics AS
SELECT 
    c.course_id,
    c.title,
    c.price,
    cs.status_name AS course_status,
    cat.category_name,
    u.first_name || ' ' || u.last_name AS teacher_name,
    COUNT(DISTINCT e.enrollment_id) AS enrolled_students,
    c.capacity,
    CASE 
        WHEN c.capacity IS NOT NULL THEN 
            ROUND((COUNT(DISTINCT e.enrollment_id)::DECIMAL / c.capacity::DECIMAL) * 100, 2)
        ELSE NULL
    END AS fill_percentage,
    COUNT(DISTINCT l.lesson_id) AS lessons_count,
    COUNT(DISTINCT t.task_id) AS tasks_count,
    COUNT(DISTINCT cert.certificate_id) AS certificates_issued,
    COALESCE(SUM(ch.amount), 0) AS total_revenue,
    c.start_date,
    c.end_date
FROM courses c
LEFT JOIN course_statuses cs ON c.course_status_id = cs.course_status_id
LEFT JOIN categories cat ON c.category_id = cat.category_id
LEFT JOIN users u ON c.teacher_id = u.user_id
LEFT JOIN enrollments e ON c.course_id = e.course_id 
    AND e.enrollment_status_id = (SELECT enrollment_status_id FROM enrollment_statuses WHERE status_name = 'Активный')
LEFT JOIN lessons l ON c.course_id = l.course_id
LEFT JOIN tasks t ON l.lesson_id = t.lesson_id
LEFT JOIN certificates cert ON c.course_id = cert.course_id
LEFT JOIN checks ch ON c.course_id = ch.course_id 
    AND ch.payment_status_id = (SELECT payment_status_id FROM payment_statuses WHERE status_name = 'Оплачено')
GROUP BY c.course_id, c.title, c.price, cs.status_name, cat.category_name, 
         u.first_name, u.last_name, c.capacity, c.start_date, c.end_date;

-- Представление записей студентов на курсы с деталями
CREATE VIEW v_user_enrollments AS
SELECT 
    e.enrollment_id,
    u.user_id,
    u.first_name || ' ' || u.last_name AS student_name,
    u.email AS student_email,
    c.course_id,
    c.title AS course_title,
    cat.category_name,
    cs.status_name AS course_status,
    es.status_name AS enrollment_status,
    e.enrollment_date,
    c.start_date AS course_start_date,
    c.end_date AS course_end_date,
    c.price AS course_price,
    COUNT(DISTINCT sol.solution_id) AS solutions_submitted,
    COUNT(DISTINCT t.task_id) AS total_tasks,
    CASE 
        WHEN COUNT(DISTINCT t.task_id) > 0 THEN
            ROUND((COUNT(DISTINCT sol.solution_id)::DECIMAL / COUNT(DISTINCT t.task_id)::DECIMAL) * 100, 2)
        ELSE 0
    END AS completion_percentage,
    CASE 
        WHEN cert.certificate_id IS NOT NULL THEN TRUE
        ELSE FALSE
    END AS has_certificate
FROM enrollments e
JOIN users u ON e.user_id = u.user_id
JOIN courses c ON e.course_id = c.course_id
LEFT JOIN categories cat ON c.category_id = cat.category_id
LEFT JOIN course_statuses cs ON c.course_status_id = cs.course_status_id
LEFT JOIN enrollment_statuses es ON e.enrollment_status_id = es.enrollment_status_id
LEFT JOIN lessons l ON c.course_id = l.course_id
LEFT JOIN tasks t ON l.lesson_id = t.lesson_id
LEFT JOIN solutions sol ON t.task_id = sol.task_id AND sol.user_id = u.user_id
LEFT JOIN certificates cert ON c.course_id = cert.course_id AND cert.user_id = u.user_id
GROUP BY e.enrollment_id, u.user_id, u.first_name, u.last_name, u.email,
         c.course_id, c.title, cat.category_name, cs.status_name, es.status_name,
         e.enrollment_date, c.start_date, c.end_date, c.price, cert.certificate_id;

-- Представление сводки курсов преподавателя
CREATE VIEW v_teacher_courses_summary AS
SELECT 
    u.user_id AS teacher_id,
    u.first_name || ' ' || u.last_name AS teacher_name,
    u.email AS teacher_email,
    COUNT(DISTINCT c.course_id) AS total_courses,
    COUNT(DISTINCT CASE WHEN cs.status_name = 'Активный' THEN c.course_id END) AS active_courses,
    COUNT(DISTINCT CASE WHEN cs.status_name = 'Идет набор' THEN c.course_id END) AS recruiting_courses,
    COUNT(DISTINCT CASE WHEN cs.status_name = 'Завершен' THEN c.course_id END) AS completed_courses,
    COUNT(DISTINCT e.enrollment_id) AS total_enrollments,
    COUNT(DISTINCT cert.certificate_id) AS certificates_issued,
    COALESCE(SUM(ch.amount), 0) AS total_revenue,
    AVG(CASE WHEN c.capacity IS NOT NULL AND c.capacity > 0 THEN 
        (SELECT COUNT(*) FROM enrollments e2 WHERE e2.course_id = c.course_id)::DECIMAL / c.capacity::DECIMAL * 100
    END) AS avg_fill_percentage
FROM users u
JOIN courses c ON u.user_id = c.teacher_id
LEFT JOIN course_statuses cs ON c.course_status_id = cs.course_status_id
LEFT JOIN enrollments e ON c.course_id = e.course_id
LEFT JOIN certificates cert ON c.course_id = cert.course_id
LEFT JOIN checks ch ON c.course_id = ch.course_id 
    AND ch.payment_status_id = (SELECT payment_status_id FROM payment_statuses WHERE status_name = 'Оплачено')
WHERE u.role_id = (SELECT role_id FROM roles WHERE role_name = 'Преподаватель')
GROUP BY u.user_id, u.first_name, u.last_name, u.email;

-- ============================================================================
-- 5. ХРАНИМЫЕ ПРОЦЕДУРЫ
-- ============================================================================

-- Процедура 1: Расчёт выручки по курсу
CREATE OR REPLACE FUNCTION calculate_course_revenue(course_id_param INT)
RETURNS DECIMAL(10,2) AS $$
DECLARE
    total_revenue DECIMAL(10,2);
BEGIN
    SELECT COALESCE(SUM(amount), 0)
    INTO total_revenue
    FROM checks
    WHERE course_id = course_id_param
    AND payment_status_id = (SELECT payment_status_id FROM payment_statuses WHERE status_name = 'Оплачено');
    
    RETURN total_revenue;
END;
$$ LANGUAGE plpgsql;

-- Процедура 2: Автоматическое обновление статуса курса на основе дат
CREATE OR REPLACE FUNCTION update_course_status_automatically()
RETURNS INT AS $$
DECLARE
    updated_count INT := 0;
    rows_updated INT;
    active_status_id INT;
    completed_status_id INT;
    recruiting_status_id INT;
BEGIN
    -- Получаем ID статусов
    SELECT course_status_id INTO active_status_id FROM course_statuses WHERE status_name = 'Активный';
    SELECT course_status_id INTO completed_status_id FROM course_statuses WHERE status_name = 'Завершен';
    SELECT course_status_id INTO recruiting_status_id FROM course_statuses WHERE status_name = 'Идет набор';
    
    -- Обновляем курсы, которые должны быть активными (начались, но не закончились)
    UPDATE courses
    SET course_status_id = active_status_id
    WHERE start_date IS NOT NULL 
    AND end_date IS NOT NULL
    AND CURRENT_DATE >= start_date 
    AND CURRENT_DATE < end_date
    AND course_status_id != active_status_id;
    
    GET DIAGNOSTICS rows_updated = ROW_COUNT;
    updated_count := updated_count + rows_updated;
    
    -- Обновляем курсы, которые должны быть завершёнными
    UPDATE courses
    SET course_status_id = completed_status_id
    WHERE end_date IS NOT NULL
    AND CURRENT_DATE >= end_date
    AND course_status_id != completed_status_id;
    
    GET DIAGNOSTICS rows_updated = ROW_COUNT;
    updated_count := updated_count + rows_updated;
    
    RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

-- Процедура 3: Получение прогресса студента по курсу
CREATE OR REPLACE FUNCTION get_student_progress(student_id_param INT, course_id_param INT)
RETURNS TABLE(
    total_tasks INT,
    completed_tasks INT,
    graded_tasks INT,
    average_grade DECIMAL(5,2),
    completion_percentage DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(DISTINCT t.task_id)::INT AS total_tasks,
        COUNT(DISTINCT CASE WHEN sol.solution_id IS NOT NULL THEN t.task_id END)::INT AS completed_tasks,
        COUNT(DISTINCT CASE WHEN sol.grade_id IS NOT NULL THEN t.task_id END)::INT AS graded_tasks,
        ROUND(AVG(g.grade_value), 2) AS average_grade,
        CASE 
            WHEN COUNT(DISTINCT t.task_id) > 0 THEN
                ROUND((COUNT(DISTINCT CASE WHEN sol.solution_id IS NOT NULL THEN t.task_id END)::DECIMAL / 
                       COUNT(DISTINCT t.task_id)::DECIMAL) * 100, 2)
            ELSE 0
        END AS completion_percentage
    FROM courses c
    JOIN lessons l ON c.course_id = l.course_id
    JOIN tasks t ON l.lesson_id = t.lesson_id
    LEFT JOIN solutions sol ON t.task_id = sol.task_id AND sol.user_id = student_id_param
    LEFT JOIN grades g ON sol.grade_id = g.grade_id
    WHERE c.course_id = course_id_param;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 6. ТРИГГЕРЫ ДЛЯ АУДИТА
-- ============================================================================

-- Функция для аудита изменений в таблице users
CREATE OR REPLACE FUNCTION audit_users_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values)
        VALUES ('users', OLD.user_id, 'DELETE', 
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values, new_values)
        VALUES ('users', NEW.user_id, 'UPDATE',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, new_values)
        VALUES ('users', NEW.user_id, 'INSERT',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(NEW));
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Функция для аудита изменений в таблице courses
CREATE OR REPLACE FUNCTION audit_courses_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values)
        VALUES ('courses', OLD.course_id, 'DELETE', 
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values, new_values)
        VALUES ('courses', NEW.course_id, 'UPDATE',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, new_values)
        VALUES ('courses', NEW.course_id, 'INSERT',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(NEW));
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Функция для аудита изменений в таблице enrollments
CREATE OR REPLACE FUNCTION audit_enrollments_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values)
        VALUES ('enrollments', OLD.enrollment_id, 'DELETE', 
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, old_values, new_values)
        VALUES ('enrollments', NEW.enrollment_id, 'UPDATE',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_log (table_name, record_id, action, user_id, new_values)
        VALUES ('enrollments', NEW.enrollment_id, 'INSERT',
                COALESCE(current_setting('app.current_user_id', TRUE)::INT, NULL),
                to_jsonb(NEW));
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Создание триггеров для аудита
CREATE TRIGGER audit_users_changes
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_users_trigger_function();

CREATE TRIGGER audit_courses_changes
    AFTER INSERT OR UPDATE OR DELETE ON courses
    FOR EACH ROW EXECUTE FUNCTION audit_courses_trigger_function();

CREATE TRIGGER audit_enrollments_changes
    AFTER INSERT OR UPDATE OR DELETE ON enrollments
    FOR EACH ROW EXECUTE FUNCTION audit_enrollments_trigger_function();

-- ============================================================================
-- 7. ИНДЕКСЫ
-- ============================================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_courses_status ON courses(course_status_id);
CREATE INDEX idx_courses_category ON courses(category_id);
CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(enrollment_status_id);
CREATE INDEX idx_solutions_user ON solutions(user_id);
CREATE INDEX idx_solutions_task ON solutions(task_id);
CREATE INDEX idx_certificates_user ON certificates(user_id);
CREATE INDEX idx_certificates_course ON certificates(course_id);
CREATE INDEX idx_checks_user ON checks(user_id);
CREATE INDEX idx_checks_course ON checks(course_id);
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- ============================================================================
-- 8. КОММЕНТАРИИ К ТАБЛИЦАМ И КОЛОНКАМ
-- ============================================================================

COMMENT ON TABLE users IS 'Таблица пользователей системы (студенты, преподаватели, администраторы)';
COMMENT ON TABLE courses IS 'Таблица курсов онлайн школы';
COMMENT ON TABLE enrollments IS 'Таблица записей студентов на курсы';
COMMENT ON TABLE certificates IS 'Таблица сертификатов о прохождении курсов';
COMMENT ON TABLE audit_log IS 'Таблица журнала аудита для отслеживания изменений данных';
COMMENT ON TABLE grades IS 'Таблица оценок (пятибалльная система: 0-5)';

COMMENT ON COLUMN users.password_hash IS 'Хэш пароля пользователя (BCrypt)';
COMMENT ON COLUMN courses.capacity IS 'Максимальное количество мест на курсе';
COMMENT ON COLUMN certificates.email_sent IS 'Флаг отправки сертификата на email';
COMMENT ON COLUMN grades.grade_value IS 'Значение оценки от 0 до 5 (пятибалльная система)';

-- ============================================================================
-- 9. ТЕСТОВЫЕ ДАННЫЕ
-- ============================================================================

-- Вставка ролей
INSERT INTO roles (role_id, role_name) VALUES
(1, 'Администратор'),
(2, 'Преподаватель'),
(3, 'Студент')
ON CONFLICT (role_id) DO UPDATE SET role_name = EXCLUDED.role_name;

SELECT setval('roles_role_id_seq', 3, true);

-- Вставка статусов курсов (ИСПРАВЛЕНО: правильные статусы)
INSERT INTO course_statuses (course_status_id, status_name) VALUES
(1, 'Идет набор'),
(2, 'Заполнен'),
(3, 'Активен'),
(4, 'Архивный')
ON CONFLICT (course_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('course_statuses_course_status_id_seq', 4, true);

-- Вставка статусов уроков
INSERT INTO lesson_statuses (lesson_status_id, status_name) VALUES
(1, 'Черновик'),
(2, 'Опубликован'),
(3, 'Архив')
ON CONFLICT (lesson_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('lesson_statuses_lesson_status_id_seq', 3, true);

-- Вставка статусов записей
INSERT INTO enrollment_statuses (enrollment_status_id, status_name) VALUES
(1, 'Активный'),
(2, 'Завершен'),
(3, 'Отменен')
ON CONFLICT (enrollment_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('enrollment_statuses_enrollment_status_id_seq', 3, true);

-- Вставка статусов заданий (ИСПРАВЛЕНО: добавлен статус "Прошел")
INSERT INTO task_statuses (task_status_id, status_name) VALUES
(1, 'Активное'),
(2, 'Завершено'),
(3, 'Отменено'),
(4, 'Прошел')
ON CONFLICT (task_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('task_statuses_task_status_id_seq', 4, true);

-- Вставка статусов решений (ИСПРАВЛЕНО: все статусы, используемые в коде)
INSERT INTO solution_statuses (solution_status_id, status_name) VALUES
(1, 'Сдано'),
(2, 'Проверено'),
(3, 'Возвращено на доработку'),
(4, 'Сдано с опозданием'),
(5, 'Назначено'),
(6, 'Просрочено')
ON CONFLICT (solution_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('solution_statuses_solution_status_id_seq', 6, true);

-- Вставка статусов платежей
INSERT INTO payment_statuses (payment_status_id, status_name) VALUES
(1, 'Ожидает оплаты'),
(2, 'Оплачено'),
(3, 'Отменено'),
(4, 'Возврат')
ON CONFLICT (payment_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('payment_statuses_payment_status_id_seq', 4, true);

-- Вставка статусов подарочных карт
INSERT INTO gift_card_statuses (gift_card_status_id, status_name) VALUES
(1, 'Активна'),
(2, 'Использована'),
(3, 'Истекла'),
(4, 'Отменена')
ON CONFLICT (gift_card_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('gift_card_statuses_gift_card_status_id_seq', 4, true);

-- Вставка статусов сертификатов
INSERT INTO certificate_statuses (certificate_status_id, status_name) VALUES
(1, 'Выдан'),
(2, 'Отменен')
ON CONFLICT (certificate_status_id) DO UPDATE SET status_name = EXCLUDED.status_name;

SELECT setval('certificate_statuses_certificate_status_id_seq', 2, true);

-- Вставка категорий
INSERT INTO categories (category_id, category_name, description) VALUES
(1, 'Программирование', 'Курсы по программированию и разработке ПО'),
(2, 'Дизайн', 'Курсы по графическому и веб-дизайну'),
(3, 'Маркетинг', 'Курсы по маркетингу и продвижению'),
(4, 'Бизнес', 'Курсы по бизнесу и управлению'),
(5, 'Языки', 'Курсы по изучению иностранных языков')
ON CONFLICT (category_id) DO UPDATE SET category_name = EXCLUDED.category_name, description = EXCLUDED.description;

SELECT setval('categories_category_id_seq', 5, true);

-- Вставка пользователей
-- Пароль для всех пользователей: 123456 (BCrypt hash: $2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS)
INSERT INTO users (user_id, first_name, last_name, middle_name, email, password_hash, role_id, registration_date) VALUES
(1, 'Админ', 'Админов', NULL, 'admin@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 1, CURRENT_DATE),
(2, 'Иван', 'Преподавателев', 'Петрович', 'teacher@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 2, CURRENT_DATE),
(3, 'Мария', 'Учителева', 'Сергеевна', 'teacher2@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 2, CURRENT_DATE - INTERVAL '30 days'),
(4, 'Петр', 'Студентов', 'Иванович', 'student1@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 3, CURRENT_DATE - INTERVAL '60 days'),
(5, 'Анна', 'Ученикова', 'Александровна', 'student2@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 3, CURRENT_DATE - INTERVAL '45 days'),
(6, 'Дмитрий', 'Обучающийся', 'Владимирович', 'student3@gmail.com', '$2a$12$wjEu.g2QTGYKpSzr./uw4.RAEkwbrF9JYTmWY7vAiQpAST4wAdoPS', 3, CURRENT_DATE - INTERVAL '20 days')
ON CONFLICT (user_id) DO UPDATE SET 
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    middle_name = EXCLUDED.middle_name,
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    role_id = EXCLUDED.role_id,
    registration_date = EXCLUDED.registration_date;

SELECT setval('users_user_id_seq', 6, true);

-- Вставка оценок (пятибалльная система: 0-5)
INSERT INTO grades (grade_id, grade_value) VALUES
(1, 0),
(2, 1),
(3, 2),
(4, 3),
(5, 4),
(6, 5)
ON CONFLICT (grade_id) DO UPDATE SET grade_value = EXCLUDED.grade_value;

SELECT setval('grades_grade_id_seq', 6, true);

-- Вставка курсов (ИСПРАВЛЕНО: обновлены статусы курсов)
INSERT INTO courses (course_id, title, description, teacher_id, price, course_status_id, category_id, capacity, start_date, end_date) VALUES
(1, 'Основы Java программирования', 'Комплексный курс по изучению языка программирования Java с нуля. Изучите основы ООП, коллекции, многопоточность и многое другое.', 2, 5000.00, 3, 1, 30, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '50 days'),
(2, 'Веб-дизайн для начинающих', 'Изучение основ веб-дизайна, работа с Figma и Adobe XD. Создание современных и красивых интерфейсов.', 3, 4500.00, 2, 2, 25, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '55 days'),
(3, 'Digital Marketing', 'Современные инструменты интернет-маркетинга и SMM. Научитесь продвигать бизнес в социальных сетях.', 2, 6000.00, 1, 3, 40, CURRENT_DATE + INTERVAL '20 days', CURRENT_DATE + INTERVAL '80 days'),
(4, 'Английский язык для IT', 'Специализированный курс английского языка для IT-специалистов. Техническая лексика и коммуникация в IT-среде.', 3, 3500.00, 3, 5, 20, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '30 days'),
(5, 'Основы бизнес-анализа', 'Введение в бизнес-анализ и управление проектами. Изучите методологии и инструменты бизнес-анализа.', 2, 5500.00, 4, 4, 35, CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '10 days')
ON CONFLICT (course_id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    teacher_id = EXCLUDED.teacher_id,
    price = EXCLUDED.price,
    course_status_id = EXCLUDED.course_status_id,
    category_id = EXCLUDED.category_id,
    capacity = EXCLUDED.capacity,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date;

SELECT setval('courses_course_id_seq', 5, true);

-- Вставка уроков
INSERT INTO lessons (lesson_id, course_id, title, content, lesson_status_id) VALUES
(1, 1, 'Введение в Java', 'В этом уроке мы изучим основы языка Java, его историю и основные концепции. Познакомимся с синтаксисом и структурой программы.', 2),
(2, 1, 'Переменные и типы данных', 'Изучение переменных, примитивных типов данных и их использование. Работа с числами, строками и булевыми значениями.', 2),
(3, 1, 'Условные операторы', 'Работа с if-else, switch и тернарным оператором. Логические операции и условия.', 2),
(4, 2, 'Введение в веб-дизайн', 'Основные принципы веб-дизайна и современные тренды. Цветовая теория и композиция.', 2),
(5, 2, 'Работа с цветом и типографикой', 'Изучение цветовых схем и правил типографики. Создание гармоничных дизайнов.', 2),
(6, 3, 'Основы маркетинга', 'Введение в digital marketing и его инструменты. Стратегии продвижения в интернете.', 1),
(7, 4, 'IT Vocabulary', 'Специализированная лексика для IT-специалистов. Термины программирования, тестирования и управления проектами.', 2),
(8, 5, 'Введение в бизнес-анализ', 'Основные концепции и методологии бизнес-анализа. Работа с требованиями и документацией.', 2)
ON CONFLICT (lesson_id) DO UPDATE SET
    course_id = EXCLUDED.course_id,
    title = EXCLUDED.title,
    content = EXCLUDED.content,
    lesson_status_id = EXCLUDED.lesson_status_id;

SELECT setval('lessons_lesson_id_seq', 8, true);

-- Вставка заданий
INSERT INTO tasks (task_id, lesson_id, title, description, deadline, task_status_id) VALUES
(1, 1, 'Первая программа на Java', 'Напишите программу Hello World на Java. Программа должна выводить приветствие в консоль.', CURRENT_DATE + INTERVAL '5 days', 1),
(2, 2, 'Работа с переменными', 'Создайте программу с различными типами переменных: int, double, String, boolean. Продемонстрируйте их использование.', CURRENT_DATE + INTERVAL '10 days', 1),
(3, 3, 'Калькулятор', 'Реализуйте простой калькулятор с использованием условных операторов. Поддержите операции: сложение, вычитание, умножение, деление.', CURRENT_DATE + INTERVAL '15 days', 1),
(4, 4, 'Макет главной страницы', 'Создайте макет главной страницы сайта в Figma. Используйте современные принципы дизайна.', CURRENT_DATE + INTERVAL '7 days', 1),
(5, 5, 'Цветовая палитра', 'Разработайте цветовую палитру для веб-сайта. Обоснуйте выбор цветов и их сочетание.', CURRENT_DATE + INTERVAL '12 days', 1),
(6, 7, 'Перевод IT-терминов', 'Переведите список IT-терминов с английского на русский. Минимум 20 терминов.', CURRENT_DATE + INTERVAL '3 days', 1),
(7, 8, 'Анализ бизнес-процесса', 'Проведите анализ предложенного бизнес-процесса. Опишите проблемы и предложите улучшения.', CURRENT_DATE - INTERVAL '5 days', 2)
ON CONFLICT (task_id) DO UPDATE SET
    lesson_id = EXCLUDED.lesson_id,
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    deadline = EXCLUDED.deadline,
    task_status_id = EXCLUDED.task_status_id;

SELECT setval('tasks_task_id_seq', 7, true);

-- Вставка записей на курсы (ИСПРАВЛЕНО: используется только enrollment_date)
INSERT INTO enrollments (enrollment_id, user_id, course_id, enrollment_date, enrollment_status_id) VALUES
(1, 4, 1, CURRENT_DATE - INTERVAL '8 days', 1),
(2, 5, 1, CURRENT_DATE - INTERVAL '7 days', 1),
(3, 6, 1, CURRENT_DATE - INTERVAL '5 days', 1),
(4, 4, 2, CURRENT_DATE - INTERVAL '3 days', 1),
(5, 5, 2, CURRENT_DATE - INTERVAL '2 days', 1),
(6, 4, 4, CURRENT_DATE - INTERVAL '25 days', 1),
(7, 5, 4, CURRENT_DATE - INTERVAL '20 days', 1),
(8, 4, 5, CURRENT_DATE - INTERVAL '95 days', 2)
ON CONFLICT (enrollment_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    course_id = EXCLUDED.course_id,
    enrollment_date = EXCLUDED.enrollment_date,
    enrollment_status_id = EXCLUDED.enrollment_status_id;

SELECT setval('enrollments_enrollment_id_seq', 8, true);

-- Вставка решений (ИСПРАВЛЕНО: используются правильные статусы решений)
INSERT INTO solutions (solution_id, task_id, user_id, answer_text, submit_date, solution_status_id, grade_id) VALUES
(1, 1, 4, 'public class HelloWorld { public static void main(String[] args) { System.out.println("Hello World"); } }', CURRENT_DATE - INTERVAL '3 days', 1, 5),
(2, 1, 5, 'public class HelloWorld { public static void main(String[] args) { System.out.println("Hello, World!"); } }', CURRENT_DATE - INTERVAL '2 days', 1, 4),
(3, 2, 4, 'int x = 10; double y = 3.14; String name = "Java"; boolean isActive = true;', CURRENT_DATE - INTERVAL '1 day', 1, 5),
(4, 4, 4, 'Создан макет главной страницы в Figma с использованием современных принципов дизайна', CURRENT_DATE - INTERVAL '1 day', 1, NULL),
(5, 6, 4, 'Algorithm - Алгоритм, Database - База данных, Framework - Фреймворк, API - Программный интерфейс приложения', CURRENT_DATE - INTERVAL '2 days', 1, 5),
(6, 7, 4, 'Проведен анализ бизнес-процесса. Выявлены узкие места и предложены улучшения.', CURRENT_DATE - INTERVAL '10 days', 2, 4)
ON CONFLICT (solution_id) DO UPDATE SET
    task_id = EXCLUDED.task_id,
    user_id = EXCLUDED.user_id,
    answer_text = EXCLUDED.answer_text,
    submit_date = EXCLUDED.submit_date,
    solution_status_id = EXCLUDED.solution_status_id,
    grade_id = EXCLUDED.grade_id;

SELECT setval('solutions_solution_id_seq', 6, true);

-- Вставка чеков
INSERT INTO checks (check_id, user_id, course_id, amount, payment_date, payment_status_id) VALUES
(1, 4, 1, 5000.00, CURRENT_DATE - INTERVAL '8 days', 2),
(2, 5, 1, 5000.00, CURRENT_DATE - INTERVAL '7 days', 2),
(3, 6, 1, 5000.00, CURRENT_DATE - INTERVAL '5 days', 2),
(4, 4, 2, 4500.00, CURRENT_DATE - INTERVAL '3 days', 2),
(5, 5, 2, 4500.00, CURRENT_DATE - INTERVAL '2 days', 2),
(6, 4, 4, 3500.00, CURRENT_DATE - INTERVAL '25 days', 2),
(7, 5, 4, 3500.00, CURRENT_DATE - INTERVAL '20 days', 2),
(8, 4, 5, 5500.00, CURRENT_DATE - INTERVAL '95 days', 2)
ON CONFLICT (check_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    course_id = EXCLUDED.course_id,
    amount = EXCLUDED.amount,
    payment_date = EXCLUDED.payment_date,
    payment_status_id = EXCLUDED.payment_status_id;

SELECT setval('checks_check_id_seq', 8, true);

-- Вставка подарочных карт
INSERT INTO gift_cards (gift_card_id, card_number, code, balance, amount, issue_date, expiry_date, user_id, course_id, gift_card_status_id) VALUES
(1, 'GC-2024-001', 'GIFT2024ABC', 5000.00, 5000.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '335 days', NULL, NULL, 1),
(2, 'GC-2024-002', 'GIFT2024XYZ', 0.00, 3000.00, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '10 days', 4, 2, 2),
(3, 'GC-2024-003', 'GIFT2024DEF', 2000.00, 5000.00, CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '350 days', 5, NULL, 1)
ON CONFLICT (gift_card_id) DO UPDATE SET
    card_number = EXCLUDED.card_number,
    code = EXCLUDED.code,
    balance = EXCLUDED.balance,
    amount = EXCLUDED.amount,
    issue_date = EXCLUDED.issue_date,
    expiry_date = EXCLUDED.expiry_date,
    user_id = EXCLUDED.user_id,
    course_id = EXCLUDED.course_id,
    gift_card_status_id = EXCLUDED.gift_card_status_id;

SELECT setval('gift_cards_gift_card_id_seq', 3, true);

-- Вставка сертификатов
INSERT INTO certificates (certificate_id, user_id, course_id, certificate_number, issue_date, document_file, certificate_status_id, email_sent, email_sent_date) VALUES
(1, 4, 5, 'CERT-2024-001', CURRENT_DATE - INTERVAL '5 days', '/certificates/cert-2024-001.pdf', 1, TRUE, CURRENT_DATE - INTERVAL '5 days'),
(2, 4, 4, 'CERT-2024-002', CURRENT_DATE - INTERVAL '2 days', '/certificates/cert-2024-002.pdf', 1, FALSE, NULL)
ON CONFLICT (certificate_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    course_id = EXCLUDED.course_id,
    certificate_number = EXCLUDED.certificate_number,
    issue_date = EXCLUDED.issue_date,
    document_file = EXCLUDED.document_file,
    certificate_status_id = EXCLUDED.certificate_status_id,
    email_sent = EXCLUDED.email_sent,
    email_sent_date = EXCLUDED.email_sent_date;

SELECT setval('certificates_certificate_id_seq', 2, true);

-- Вставка файлов
INSERT INTO files (id, original_name, file_path, file_size, content_type, uploaded_by, upload_date, description) VALUES
(1, 'lesson1.pdf', '/files/lessons/lesson1.pdf', 1024000, 'application/pdf', 2, CURRENT_DATE - INTERVAL '10 days', 'Материалы первого урока'),
(2, 'task1_template.docx', '/files/tasks/task1_template.docx', 512000, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 2, CURRENT_DATE - INTERVAL '8 days', 'Шаблон для первого задания'),
(3, 'presentation.pptx', '/files/presentations/presentation.pptx', 2048000, 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 3, CURRENT_DATE - INTERVAL '5 days', 'Презентация по веб-дизайну')
ON CONFLICT (id) DO UPDATE SET
    original_name = EXCLUDED.original_name,
    file_path = EXCLUDED.file_path,
    file_size = EXCLUDED.file_size,
    content_type = EXCLUDED.content_type,
    uploaded_by = EXCLUDED.uploaded_by,
    upload_date = EXCLUDED.upload_date,
    description = EXCLUDED.description;

SELECT setval('files_id_seq', 3, true);

-- Вставка комментариев
INSERT INTO comments (comment_id, user_id, lesson_id, task_id, parent_comment_id, text, created_at) VALUES
(1, 4, 1, NULL, NULL, 'Отличный урок! Все понятно объяснено', CURRENT_TIMESTAMP - INTERVAL '8 days'),
(2, 5, 1, NULL, NULL, 'Спасибо за материал', CURRENT_TIMESTAMP - INTERVAL '7 days'),
(3, 4, NULL, 1, NULL, 'Вопрос по заданию: можно ли использовать другие методы?', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(4, 2, NULL, 1, 3, 'Да, можно использовать любые методы, главное чтобы программа работала', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(5, 6, 2, NULL, NULL, 'Интересная тема про переменные', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (comment_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    lesson_id = EXCLUDED.lesson_id,
    task_id = EXCLUDED.task_id,
    parent_comment_id = EXCLUDED.parent_comment_id,
    text = EXCLUDED.text,
    created_at = EXCLUDED.created_at;

SELECT setval('comments_comment_id_seq', 5, true);

-- Вставка токенов сброса пароля
INSERT INTO password_reset_tokens (token_id, token, user_id, expiry_date, used) VALUES
(1, 'reset_token_abc123xyz', 4, CURRENT_TIMESTAMP + INTERVAL '24 hours', FALSE),
(2, 'reset_token_def456uvw', 5, CURRENT_TIMESTAMP + INTERVAL '12 hours', FALSE),
(3, 'reset_token_ghi789rst', 6, CURRENT_TIMESTAMP - INTERVAL '1 hour', TRUE)
ON CONFLICT (token_id) DO UPDATE SET
    token = EXCLUDED.token,
    user_id = EXCLUDED.user_id,
    expiry_date = EXCLUDED.expiry_date,
    used = EXCLUDED.used;

SELECT setval('password_reset_tokens_token_id_seq', 3, true);

-- ============================================================================
-- ГОТОВО!
-- ============================================================================

