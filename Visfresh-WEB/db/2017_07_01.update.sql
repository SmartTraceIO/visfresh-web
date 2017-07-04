alter table grouplocks drop column lastupdate;
alter table grouplocks add column unlockon timestamp null default null;