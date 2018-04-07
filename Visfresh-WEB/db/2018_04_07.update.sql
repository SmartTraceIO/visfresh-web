create table beacongateways (
    id bigint(20) auto_increment not null,
    company bigint(20) not null,
    gateway varchar(30) not null,
    beacon varchar(30) not null,
    active boolean not null default true,
    description varchar(255) default null,
    primary key (id),
    foreign key (company)
        references companies (id)
        ON DELETE CASCADE
);

alter table trackerevents drop column beacon;
alter table shipments drop column beacon;
