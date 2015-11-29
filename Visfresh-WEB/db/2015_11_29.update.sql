-- add external company.
alter table users add column externalcompany varchar(127) default null;

alter table users add column external boolean;
update users set external = false where id <> 0; -- id <> 0 for suppress safe mode
alter table users modify column external boolean not null default false;
