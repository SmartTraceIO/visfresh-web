-- add isRead flag.
alter table notifications add column isread boolean;
update notifications set isread = false where id <> 0; -- id <> 0 for suppress safe mode
alter table notifications modify column isread boolean not null default false;

