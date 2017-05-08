create table shipmentaudits (
    id bigint(20) auto_increment not null,
    time timestamp NULL default NULL,
    user bigint(20) default null,
    shipment bigint(20) not null,
    action varchar(127) not null,
	info longtext not null,
	
    primary key (id),
    FOREIGN KEY (shipment) REFERENCES shipments (id) on delete cascade,
    FOREIGN KEY (user) REFERENCES users (id) on delete cascade
);
