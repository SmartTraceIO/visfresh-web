create table actiontakens (
    id bigint(20) auto_increment not null,
    shipment bigint(20) not null,
    alert bigint(20) not null,
    confirmedby bigint(20) not null,
    verifiedby bigint(20),
    `action` longtext not null,
    comments longtext,
    `time` timestamp null default null,
    primary key (id),
    foreign key (shipment)
        references shipments (id) on delete cascade,
    foreign key (alert)
        references alerts (id) on delete cascade,
    foreign key (confirmedby)
        references users (id),
    foreign key (verifiedby)
        references users (id)
);

