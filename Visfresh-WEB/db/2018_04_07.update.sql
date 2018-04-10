create table pairedphones (
    id bigint(20) auto_increment not null,
    company bigint(20) not null,
    imei varchar(30) not null,
    beaconid varchar(30) not null,
    active boolean not null default true,
    description varchar(255) default null,
    primary key (id),
    constraint beacon_imei unique (imei, beaconid),
    foreign key (company)
        references companies (id)
        ON DELETE CASCADE
);

alter table trackerevents drop column beacon;
alter table shipments drop column beacon;
