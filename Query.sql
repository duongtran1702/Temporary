use it211_ss18_b1;

-- Đã chỉnh sửa để khớp với code tìm kiếm role trong Java (ROLE_USER, ROLE_ADMIN)
INSERT INTO role(name)
VALUES
    ('ROLE_USER'),
    ('ROLE_STAFF'),
    ('ROLE_ADMIN');

-- BỔ SUNG TỪ DỰ ÁN TEACHER: Chèn người dùng mẫu vào bảng user (mật khẩu mặc định: 123456)
INSERT INTO user (username, password, email, phone, created_at)
VALUES
    ('admin01', '$2a$10$p.oD0jT1i/RyclnwsIePk.9zLPEAR1u7YCs17NXndZDZ6kpZFuhvS', 'admin01@example.com', '0123456789', NOW()),
    ('user01', '$2a$10$p.oD0jT1i/RyclnwsIePk.9zLPEAR1u7YCs17NXndZDZ6kpZFuhvS', 'user01@example.com', '0987654321', NOW());

-- BỔ SUNG TỪ DỰ ÁN TEACHER: Gán quyền cho người dùng mẫu trong bảng user_roles (Admin có quyền ROLE_USER và ROLE_ADMIN, User có quyền ROLE_USER)
INSERT INTO user_roles (user_id, role_id)
VALUES
    (1, 1), -- admin01 có quyền ROLE_USER
    (1, 3), -- admin01 có quyền ROLE_ADMIN
    (2, 1); -- user01 có quyền ROLE_USER