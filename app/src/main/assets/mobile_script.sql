BEGIN TRANSACTION;

DROP TABLE IF EXISTS "android_metadata";
CREATE TABLE "android_metadata" (
	"locale"	TEXT
);

DROP TABLE IF EXISTS "tbl_note";
CREATE TABLE "tbl_note" (
	"id"	INTEGER,
	"user_id"	INTEGER NOT NULL,
	"title"	TEXT NOT NULL,
	"content"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("user_id") REFERENCES "tbl_user"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_photo";
CREATE TABLE "tbl_note_photo" (
	"id"	INTEGER,
	"note_id"	INTEGER NOT NULL,
	"photo_uri"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_reminder";
CREATE TABLE "tbl_note_reminder" (
	"id"	INTEGER,
	"note_id"	INTEGER NOT NULL,
	"date"	TEXT,
	"time"	TEXT,
	"days_before"	INTEGER DEFAULT 0,
	"is_repeat"	BOOLEAN DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_tag";
CREATE TABLE "tbl_note_tag" (
	"id"	INTEGER,
	"note_id"	INTEGER NOT NULL,
	"tag_text"	TEXT NOT NULL,
	"tag_color"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);
DROP TABLE IF EXISTS "tbl_user";
CREATE TABLE "tbl_user" (
	"id"	INTEGER,
	"email"	TEXT NOT NULL,
	"password_hash"	TEXT,
	"isGoogle"	INTEGER DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT)
);
-- 🏷 Tạo bảng lưu trữ task
DROP TABLE IF EXISTS "tbl_task";
CREATE TABLE tbl_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    priority INTEGER NOT NULL,  -- 1-4 representing quadrants
    reminder_date TEXT          -- Date for reminder (optional)
);

INSERT INTO "android_metadata" VALUES ('en_US');
INSERT INTO "tbl_note" VALUES (1,1,'Tourism','Da Nang
Nha Trang
Phan Thiet');
INSERT INTO "tbl_note" VALUES (2,1,'Haha','ascnaocwnaocwnoawncoanwcaoiamacnon
caoihwdohawdhaowdihoasdjpdsjapdwmawdpo');
INSERT INTO "tbl_note_photo" VALUES (13,1,'file:///data/user/0/com.example.project/files/note_img_1743490583956.jpg');
INSERT INTO "tbl_note_reminder" VALUES (21,2,'Ngày 1, tháng 4','15:14',0,0);
INSERT INTO "tbl_note_reminder" VALUES (22,1,'Ngày 1, tháng 4','15:15',0,0);
INSERT INTO "tbl_note_tag" VALUES (25,1,'excited ','#8E44AD');
INSERT INTO "tbl_note_tag" VALUES (26,1,'😊','#E67E22');
INSERT INTO "tbl_user" VALUES (1,'alice@example.com','hashed_password_123',0);
INSERT INTO "tbl_user" VALUES (2,'bob@example.com','hashed_password_456',0);
INSERT INTO "tbl_user" VALUES (3,'alice@example.com',NULL,1);
INSERT INTO tbl_task (title, description, priority, reminder_date)
VALUES
    ('Hoàn thành báo cáo', 'Báo cáo tài chính quý 1', 1, '2025-04-05'),
    ('Mua sách mới', 'Sách về lập trình Android', 3, '2025-04-10');

COMMIT;
