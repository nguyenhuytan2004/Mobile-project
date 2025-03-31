-- 🏷 Xóa bảng nếu đã tồn tại để tránh lỗi khi chạy lại script
DROP TABLE IF EXISTS tbl_user;
DROP TABLE IF EXISTS tbl_note;
DROP TABLE IF EXISTS tbl_note_tag;
DROP TABLE IF EXISTS tbl_note_photo;
DROP TABLE IF EXISTS tbl_note_reminder;

-- 🧑‍💻 Tạo bảng người dùng
CREATE TABLE tbl_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL,        -- Email người dùng, có thể trùng (do đăng nhập Google)
    password_hash TEXT,         -- Mật khẩu đã hash (NULL nếu đăng nhập Google)
    isGoogle INTEGER DEFAULT 0  -- 0: Đăng ký bình thường, 1: Đăng nhập bằng Google
);

-- 📝 Thêm dữ liệu mẫu cho bảng người dùng
INSERT INTO tbl_user (email, password_hash, isGoogle) VALUES
('alice@example.com', 'hashed_password_123', 0),
('bob@example.com', 'hashed_password_456', 0),
('alice@example.com', NULL, 1); -- Alice đăng nhập bằng Google, không cần mật khẩu

-- 🗒 Tạo bảng ghi chú
CREATE TABLE tbl_note (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,  -- Mỗi ghi chú thuộc về một người dùng
    title TEXT NOT NULL,       -- Tiêu đề ghi chú
    content TEXT,              -- Nội dung ghi chú
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 🏷 Tạo bảng lưu thẻ (tags) cho ghi chú
CREATE TABLE tbl_note_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL, -- Ghi chú liên quan đến thẻ này
    tag_text TEXT NOT NULL,   -- Nội dung thẻ (ví dụ: "Công việc", "Quan trọng")
    tag_color TEXT NOT NULL,  -- Màu sắc của thẻ (ví dụ: "#FF5733")
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE
);

-- 📷 Tạo bảng lưu ảnh đính kèm với ghi chú
CREATE TABLE tbl_note_photo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL, -- Ghi chú liên quan đến ảnh này
    photo_uri TEXT NOT NULL,  -- Đường dẫn ảnh (URI hoặc URL)
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE
);

-- ⏰ Tạo bảng lưu nhắc nhở cho ghi chú
CREATE TABLE tbl_note_reminder (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,     -- Ghi chú liên quan đến nhắc nhở này
    date TEXT,                    -- Ngày nhắc nhở (YYYY-MM-DD)
    time TEXT,                    -- Giờ nhắc nhở (HH:MM)
    days_before INTEGER DEFAULT 0, -- Số ngày nhắc trước (0 = đúng ngày)
    is_repeat BOOLEAN DEFAULT 0,   -- 1: Nhắc lại định kỳ, 0: Chỉ nhắc một lần
    FOREIGN KEY (note_id) REFERENCES tbl_note(id) ON DELETE CASCADE
);
