alter table companies add column address varchar(255) default null;
alter table companies add column contactperson varchar(50) default null;
alter table companies add column email varchar(127) default null;
alter table companies add column timezone varchar(31) null default 'UTC';
alter table companies add column startdate timestamp NULL default NULL;
alter table companies add column trackersemail varchar(127) default null;
alter table companies add column paymentmethod varchar(127) default null;
alter table companies add column billingperson varchar(50) default null;
alter table companies add column `language` varchar(20) null default 'English';
