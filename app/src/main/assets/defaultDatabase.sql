BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Books" (
	"row_id"	INTEGER NOT NULL,
	"title"	TEXT NOT NULL,
	"url"	TEXT NOT NULL,
	"bookList"	TEXT NOT NULL,
	PRIMARY KEY("row_id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "CONFIG" (
	"row_id"	INTEGER NOT NULL,
	"domain"	TEXT NOT NULL,
	"mainXPath"	TEXT NOT NULL,
	"prevXPath"	TEXT NOT NULL,
	"nextXPath"	TEXT NOT NULL,
	PRIMARY KEY("row_id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "Lists" (
	"name"	TEXT NOT NULL,
	PRIMARY KEY("name")
);
INSERT INTO "CONFIG" VALUES (1,'readnovelfull.com','.chr-c','.prev_chap','.next_chap');
INSERT INTO "CONFIG" VALUES (2,'royalroad.com','.chapter-inner','div.col-md-4:nth-child(1) > a:nth-child(1)','.col-md-offset-4 > a:nth-child(1)');
INSERT INTO "CONFIG" VALUES (3,'scribblehub.com','#chp_raw','div.prenext > a:nth-child(1)','div.prenext > a:nth-child(2)');
INSERT INTO "CONFIG" VALUES (4,'readlightnovel.org','.hidden','.prev-link','.next-link');
INSERT INTO "Lists" VALUES ('Add A BookList');
INSERT INTO "Lists" VALUES ('Books');
COMMIT;
