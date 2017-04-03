create table shipmentstats (
    shipment bigint(20) not null,
    total bigint(20) not null,
    avg double,
    devitation double,
    min double,
    max double,
    timebelowlimit double not null,
    timeabovelimit double not null,
    collector TEXT not null,
    primary key (shipment),
    FOREIGN KEY (shipment)
        REFERENCES shipments (id) ON DELETE CASCADE
);
