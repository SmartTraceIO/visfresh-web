alter table alertprofiles add column lowertemplimit int not null default 0;
alter table alertprofiles add column uppertemplimit int not null default 5;
