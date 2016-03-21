alter table trackerevents modify column `time` timestamp null default null;
alter table alerts modify column `date` timestamp null default null;
alter table arrivals modify column `date` timestamp null default null;
