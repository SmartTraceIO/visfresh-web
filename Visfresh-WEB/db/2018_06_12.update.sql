alter table shipments add column nearestdevice varchar(30) default NULL;
alter table shipments add foreign key (nearestdevice) REFERENCES devices (imei); 
