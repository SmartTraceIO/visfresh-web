alter table autostartlocations add column sortorder int not null default 0;
create table alternativelocations (
    shipment bigint(20) not null,
    location bigint(20) not null,
    loctype varchar(8) not null,
    primary key (shipment, location, loctype),
    FOREIGN KEY (location)
        REFERENCES locationprofiles (id) ON DELETE CASCADE,
    FOREIGN KEY (shipment)
        REFERENCES shipments (id) ON DELETE CASCADE
);
