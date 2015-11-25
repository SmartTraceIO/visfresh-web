-- add active flag.
alter table users add column active boolean;
update users set active = true where id <> 0; -- id <> 0 for suppress safe mode
alter table users modify column active boolean not null default true;

-- add cumulative flag
alter table alerts add column cumulative boolean;
update alerts set cumulative = false where id <> 0; -- id <> 0 for suppress safe mode
alter table alerts modify column cumulative boolean not null default false;
