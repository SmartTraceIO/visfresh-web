alter table shipments add column deviceshutdowndate timestamp NULL default NULL;
alter table shipments modify column lasteventdate timestamp NULL default NULL;
