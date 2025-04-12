BEGIN TRANSACTION;

DROP TABLE IF EXISTS "android_metadata";
CREATE TABLE "android_metadata" (
	"locale"	TEXT
);


DROP TABLE IF EXISTS "tbl_note";
CREATE TABLE "tbl_note" (
	id INTEGER,
	user_id	INTEGER NOT NULL,
	title TEXT NOT NULL,
	content TEXT,
	category_id INTEGER NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("user_id") REFERENCES "tbl_user"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_photo";
CREATE TABLE "tbl_note_photo" (
	id INTEGER,
	note_id INTEGER NOT NULL,
	photo_uri TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_reminder";
CREATE TABLE "tbl_note_reminder" (
	id INTEGER,
	note_id INTEGER NOT NULL,
	date TEXT,
	time TEXT,
	days_before INTEGER DEFAULT 0,
	is_repeat BOOLEAN DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_note_tag";
CREATE TABLE "tbl_note_tag" (
	id INTEGER,
	note_id	INTEGER NOT NULL,
	tag_text TEXT NOT NULL,
	tag_color TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("note_id") REFERENCES "tbl_note"("id") ON DELETE CASCADE
);


DROP TABLE IF EXISTS "tbl_task";
CREATE TABLE tbl_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT,
    priority INTEGER NOT NULL,
	is_completed BOOL DEFAULT FALSE,
	category_id INTEGER NOT NULL,
	FOREIGN KEY (category_id) REFERENCES tbl_category(id),
	FOREIGN KEY (user_id) REFERENCES tbl_user(id)
);
DROP TABLE IF EXISTS "tbl_task_photo";
CREATE TABLE "tbl_task_photo" (
	id INTEGER,
	task_id INTEGER NOT NULL,
	photo_uri TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("task_id") REFERENCES "tbl_task"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_task_reminder";
CREATE TABLE "tbl_task_reminder" (
	id INTEGER,
	task_id INTEGER NOT NULL,
	date TEXT,
	time TEXT,
	days_before INTEGER DEFAULT 0,
	is_repeat BOOLEAN DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("task_id") REFERENCES "tbl_task"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_task_tag";
CREATE TABLE "tbl_task_tag" (
	id INTEGER,
	task_id	INTEGER NOT NULL,
	tag_text TEXT NOT NULL,
	tag_color TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("task_id") REFERENCES "tbl_task"("id") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "tbl_category";
CREATE TABLE tbl_category (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL UNIQUE,
	list_id INTEGER NOT NULL,
	FOREIGN KEY (list_id) REFERENCES tbl_list(id)
);

DROP TABLE IF EXISTS "tbl_list";
CREATE TABLE tbl_list (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL UNIQUE,
	icon TEXT
);

DROP TABLE IF EXISTS "tbl_habit";
CREATE TABLE tbl_habit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    quote TEXT,
    frequency TEXT,
    week_days TEXT,        -- JSON string of boolean array
    goal TEXT,
    start_date TEXT,       -- Store as ISO8601 string
    goal_days TEXT,
    section TEXT,
    reminder TEXT,
    auto_popup INTEGER     -- 0/1
);

DROP TABLE IF EXISTS "tbl_user";
CREATE TABLE "tbl_user" (
	id INTEGER,
	email TEXT NOT NULL,
	password_hash TEXT,
	isGoogle INTEGER DEFAULT 0,
	is_premium INTEGER DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT)
);

DROP TABLE IF EXISTS "tbl_weights";
CREATE TABLE "tbl_weights" (
    class_id INTEGER,
    feature_index INTEGER,
    weight REAL,
    PRIMARY KEY (class_id, feature_index)
);

INSERT INTO "android_metadata" VALUES ('en_US');
INSERT INTO "tbl_user" VALUES (1,'alice@example.com','hashed_password_123',0);
INSERT INTO "tbl_user" VALUES (2,'bob@example.com','hashed_password_456',0);
INSERT INTO "tbl_user" VALUES (3,'alice@example.com',NULL,1);

INSERT INTO tbl_list VALUES
(1, 'none' , NULL),
(2, 'Hôm nay', NULL),
(3, 'Welcome', NULL);

INSERT INTO tbl_category VALUES
(1, 'none', 1),
(2, 'Weekend', 3),
(3, 'Daily work', 3);

INSERT INTO "tbl_note" VALUES (1,1,'Tourism','Da Nang
Nha Trang
Phan Thiet', 2);
INSERT INTO "tbl_note" VALUES (2,1,'Haha','ascnaocwnaocwnoawncoanwcaoiamacnon
caoihwdohawdhaowdihoasdjpdsjapdwmawdpo', 2);

INSERT INTO "tbl_note_photo" VALUES (13,1,'file:///data/user/0/com.example.project/files/note_img_1743490583956.jpg');

INSERT INTO "tbl_note_reminder" VALUES (21,2,'Ngày 1, tháng 4','15:15',0,0);
INSERT INTO "tbl_note_reminder" VALUES (22,1,'Ngày 30, tháng 4','09:00',0,0);

INSERT INTO "tbl_note_tag" VALUES (25,1,'excited ','#8E44AD');
INSERT INTO "tbl_note_tag" VALUES (26,1,'😊','#E67E22');

INSERT INTO tbl_task (user_id,title, content, priority, category_id)
VALUES
    (1,'Hoàn thành báo cáo', 'Báo cáo tài chính quý 1', 1, 3),
    (1,'Mua sách mới', 'Sách về lập trình Android', 3, 2);

INSERT INTO "tbl_task_photo" VALUES (30,1,'file:///data/user/0/com.example.project/files/note_img_1743490583956.jpg');

INSERT INTO "tbl_task_reminder" VALUES (21,2,'Ngày 1, tháng 4','15:15',0,0);
INSERT INTO "tbl_task_reminder" VALUES (22,1,'Ngày 30, tháng 4','09:00',0,0);

INSERT INTO "tbl_task_tag" VALUES (25,1,'excited ','#8E44AD');
INSERT INTO "tbl_task_tag" VALUES (26,1,'😊','#E67E22');

	INSERT INTO tbl_habit (name, quote, frequency, week_days, goal, start_date, goal_days, section, reminder, auto_popup) VALUES
('Morning Jog', 'Start your day with energy!', 'daily', '[true, true, true, true, true, false, false]', 'Run 3km', '2024-04-01', '30', 'Health', '07:00 AM', 1),
('Reading', 'A book a day keeps ignorance away.', 'weekly', '[false, false, true, false, false, true, false]', 'Read 20 pages', '2024-03-15', '60', 'Education', '09:00 PM', 0);
COMMIT;

-- Class 0
INSERT INTO "tbl_weights" VALUES (0, 0, -2.189159382724869);
INSERT INTO "tbl_weights" VALUES (0, 1, -1.3128789784743151);
INSERT INTO "tbl_weights" VALUES (0, 2, -3.5283748238456183);
INSERT INTO "tbl_weights" VALUES (0, 3, -3.289643787018639);
INSERT INTO "tbl_weights" VALUES (0, 4, -3.706564747176178);
INSERT INTO "tbl_weights" VALUES (0, 5, -4.100838928860778);
INSERT INTO "tbl_weights" VALUES (0, 6, -5.187676360306409);
INSERT INTO "tbl_weights" VALUES (0, 7, -5.737979925122579);
INSERT INTO "tbl_weights" VALUES (0, 8, -9.136539244350658);

-- Class 1
INSERT INTO "tbl_weights" VALUES (1, 0, -2.4385431510118716);
INSERT INTO "tbl_weights" VALUES (1, 1, -4.037066977039492);
INSERT INTO "tbl_weights" VALUES (1, 2, -3.24140352026251);
INSERT INTO "tbl_weights" VALUES (1, 3, -2.9445039776875035);
INSERT INTO "tbl_weights" VALUES (1, 4, -3.3183069213717586);
INSERT INTO "tbl_weights" VALUES (1, 5, -3.7232724963545207);
INSERT INTO "tbl_weights" VALUES (1, 6, -7.028582237447608);
INSERT INTO "tbl_weights" VALUES (1, 7, -8.445607978476197);
INSERT INTO "tbl_weights" VALUES (1, 8, -5.251555465853737);

-- Class 2
INSERT INTO "tbl_weights" VALUES (2, 0, -1.6707346252996615);
INSERT INTO "tbl_weights" VALUES (2, 1, -1.5354013059132938);
INSERT INTO "tbl_weights" VALUES (2, 2, -3.7585407373227078);
INSERT INTO "tbl_weights" VALUES (2, 3, -2.865228577893336);
INSERT INTO "tbl_weights" VALUES (2, 4, -4.014978396233632);
INSERT INTO "tbl_weights" VALUES (2, 5, -3.9133410313119543);
INSERT INTO "tbl_weights" VALUES (2, 6, -9.700525425805623);
INSERT INTO "tbl_weights" VALUES (2, 7, -2.7116033012331995);
INSERT INTO "tbl_weights" VALUES (2, 8, -7.641629536867922);

-- Class 3
INSERT INTO "tbl_weights" VALUES (3, 0, -1.2262161288484035);
INSERT INTO "tbl_weights" VALUES (3, 1, 2.7576094503183737);
INSERT INTO "tbl_weights" VALUES (3, 2, -3.2564156241907782);
INSERT INTO "tbl_weights" VALUES (3, 3, -2.8784029073445447);
INSERT INTO "tbl_weights" VALUES (3, 4, -3.3440326074512163);
INSERT INTO "tbl_weights" VALUES (3, 5, -3.8389347322144687);
INSERT INTO "tbl_weights" VALUES (3, 6, -3.8608874191840927);
INSERT INTO "tbl_weights" VALUES (3, 7, -11.285287407444331);
INSERT INTO "tbl_weights" VALUES (3, 8, -6.159176880073111);
