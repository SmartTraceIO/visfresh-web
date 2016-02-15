alter table shipments add column shutdownafterarrivalminutes int default null;
update shipments set shutdownafterarrivalminutes = shutdowntimeout where id <> -1;
alter table shipments drop column shutdowntimeout;

alter table shipments add column shutdownafterstartminutes int default null;
alter table shipments add column shutdownafterarrivalminutes int default null;
alter table shipments add column noalertsafterarrivalminutes int default null;

