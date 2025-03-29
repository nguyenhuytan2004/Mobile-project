-- Tạo bảng người dùng
DROP TABLE IF EXISTS tbl_user;
CREATE TABLE tbl_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

-- Thêm dữ liệu mẫu vào bảng người dùng
INSERT INTO tbl_user (username, email, password_hash) VALUES
('alice', 'alice@example.com', 'hashed_password_123'),
('bob', 'bob@example.com', 'hashed_password_456');

-- Tạo bảng ghi chú
DROP TABLE IF EXISTS tbl_note;
CREATE TABLE tbl_note (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu vào bảng ghi chú
INSERT INTO tbl_note (user_id, title, content) VALUES
(1, 'Lập kế hoạch tuần', 'Hoàn thành checklist công việc.'),
(1, 'Mua sắm', 'Cần mua laptop mới và bàn phím.'),
(2, 'Dự án mới', 'Lên ý tưởng cho sản phẩm AI.');

-- Tạo bảng thẻ (label) cho ghi chú
DROP TABLE IF EXISTS tbl_note_tag;
CREATE TABLE tbl_note_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu vào bảng thẻ
INSERT INTO tbl_note_tag (name, user_id) VALUES
('Công việc', 1),
('Cá nhân', 1),
('Dự án', 2);

-- Bảng trung gian để liên kết ghi chú với thẻ
DROP TABLE IF EXISTS tbl_note_detail;
CREATE TABLE tbl_note_detail (
    note_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tbl_note_tag(id) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu vào bảng chi tiết ghi chú
INSERT INTO tbl_note_detail (note_id, tag_id) VALUES
(1, 1), -- "Lập kế hoạch tuần" có thẻ "Công việc"
(2, 2), -- "Mua sắm" có thẻ "Cá nhân"
(3, 3); -- "Dự án mới" có thẻ "Dự án"

-- Tạo bảng tệp đính kèm
DROP TABLE IF EXISTS tbl_note_attachment;
CREATE TABLE tbl_note_attachment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,
    file_name TEXT NOT NULL,
    file_url TEXT NOT NULL,
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu vào bảng tệp đính kèm
INSERT INTO tbl_note_attachment (note_id, file_name, file_url) VALUES
(1, 'plan.pdf', 'https://example.com/plan.pdf'),
(3, 'idea.txt', 'https://example.com/idea.txt');

-- Tạo bảng nhắc nhở
DROP TABLE IF EXISTS tbl_note_reminder;
CREATE TABLE tbl_note_reminder (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,
    reminder_date TEXT NOT NULL,  -- Lưu dưới dạng chuỗi ISO 8601
    remind_before_days INTEGER DEFAULT 0,
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu vào bảng nhắc nhở
INSERT INTO tbl_note_reminder (note_id, reminder_date, remind_before_days) VALUES
(1, '2025-04-01 08:00:00', 1),
(2, '2025-04-02 10:00:00', 2);
