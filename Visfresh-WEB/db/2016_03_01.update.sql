alter table alerts add column rule bigint(20) default null;

update users set roles = 'Admin' where roles = 'CompanyAdmin';
update users set roles = 'NormalUser' where roles = 'Dispatcher';
update users set roles = 'SmartTraceAdmin' where roles = 'GlobalAdmin';
