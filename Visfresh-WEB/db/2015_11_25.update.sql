-- add active flag.
alter table users add column active boolean;
update users set active = true where id <> 0; -- id <> 0 for suppress safe mode
alter table users modify column active boolean not null default true;

