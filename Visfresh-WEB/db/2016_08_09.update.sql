alter table alertprofiles add column lowertemplimit double not null default 0;
alter table alertprofiles add column uppertemplimit double not null default 5;
