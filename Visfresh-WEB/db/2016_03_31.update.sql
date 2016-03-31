create table notes (
    shipment bigint(20) NOT NULL,
    notenum int NOT NULL,
    notetext longtext NOT NULL,
    timeonchart timestamp NULL default NULL,
    notetype varchar(31) NOT NULL,
    createdon timestamp NULL default NULL,
    createdby varchar(127) default null,
    primary key (shipment, notenum),
    foreign key (shipment)
        references shipments (id) on delete cascade
);

alter table notes add column active boolean not NULL default true;
alter table notes modify column notetype varchar(31) default NULL;