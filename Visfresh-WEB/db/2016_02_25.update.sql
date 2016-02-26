alter table shipments add column isautostart boolean not null default false;
alter table devices add column active boolean not null default true;
