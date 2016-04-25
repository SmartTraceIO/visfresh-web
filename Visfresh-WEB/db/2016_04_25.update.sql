-- Add created on to tracker events
alter table trackerevents add column createdon timestamp null default null;
update trackerevents set createdon = `time` where id <> -1; -- id <> -1 for prevent safe mode protection

-- Add device colors
alter table devices add column color varchar(30) default null;
