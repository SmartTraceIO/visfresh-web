-- add external company.
alter table users drop column scale;
alter table users add column settings longtext;
