# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "blog" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "personId" INTEGER NOT NULL,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL,
  "title" VARCHAR(254) NOT NULL,
  "description" Text NOT NULL,
  "projectId" INTEGER
);
create index "blog_idx_personId" on "blog" ("personId");
create index "blog_idx_projectId" on "blog" ("projectId");

create table "cv" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "personId" INTEGER NOT NULL,
  "imgId" INTEGER,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL,
  "body" BYTEA NOT NULL
);
create index "cv_idx_personId" on "cv" ("personId");

create table "img" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR(254) NOT NULL,
  "desc" TEXT,
  "personId" INTEGER,
  "projectId" INTEGER,
  "fileName" VARCHAR(254) NOT NULL,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL
);
create index "img_idx_name" on "img" ("name");
create unique index "img_idx_fileName" on "img" ("fileName");
create index "img_idx_projectId" on "img" ("projectId");
create index "img_idx_personId" on "img" ("personId");

create table "item" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "personId" INTEGER,
  "timestamp" TIMESTAMP NOT NULL,
  "desc" VARCHAR(254),
  "itemId" INTEGER NOT NULL,
  "action" VARCHAR(254) NOT NULL,
  "itemType" VARCHAR(254) NOT NULL
);
create index "item_idx_timestamp" on "item" ("timestamp");

create table "person" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "login" VARCHAR(254) NOT NULL,
  "fullname" VARCHAR(254) NOT NULL,
  "email" VARCHAR(254) NOT NULL,
  "img" INTEGER,
  "bio" VARCHAR(254),
  "password" VARCHAR(254) NOT NULL,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL
);
create unique index "person_idx_login" on "person" ("login");

create table "post" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "blogId" INTEGER NOT NULL,
  "personId" INTEGER NOT NULL,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL,
  "title" VARCHAR(254) NOT NULL,
  "body" Text NOT NULL,
  "refId" INTEGER
);
create index "post_idx_Id" on "post" ("personId");
create index "post_idx_blogId" on "post" ("blogId");
create index "post_ids_refId" on "post" ("refId");

create table "project" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR(254) NOT NULL,
  "desc" Text NOT NULL,
  "imgId" INTEGER,
  "personId" INTEGER NOT NULL,
  "created" TIMESTAMP NOT NULL,
  "updated" TIMESTAMP NOT NULL
);
create index "project_idx_name" on "project" ("name");
create index "project_idx_personId" on "project" ("personId");

alter table "blog" add constraint "blog_person_fk" foreign key("personId") references "person"("id") on update NO ACTION on delete NO ACTION;
alter table "cv" add constraint "cv_person_fk" foreign key("personId") references "person"("id") on update NO ACTION on delete NO ACTION;
alter table "item" add constraint "item_person_fk" foreign key("personId") references "person"("id") on update NO ACTION on delete NO ACTION;
alter table "post" add constraint "post_person_fk" foreign key("personId") references "person"("id") on update NO ACTION on delete NO ACTION;
alter table "post" add constraint "post_blog_fk" foreign key("blogId") references "blog"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "blog" drop constraint "blog_person_fk";
alter table "cv" drop constraint "cv_person_fk";
alter table "item" drop constraint "item_person_fk";
alter table "post" drop constraint "post_person_fk";
alter table "post" drop constraint "post_blog_fk";
drop table "blog";
drop table "cv";
drop table "img";
drop table "item";
drop table "person";
drop table "post";
drop table "project";

