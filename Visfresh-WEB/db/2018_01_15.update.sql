alter table devices add column model varchar(20) not null default 'SmartTrace';
update devices set model = 'SmartTrace';
