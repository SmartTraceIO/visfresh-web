alter table shipments add column arrivalreport boolean not null default true;
alter table shipments add column arrivalreportonlyifalerts boolean not null default false;
    