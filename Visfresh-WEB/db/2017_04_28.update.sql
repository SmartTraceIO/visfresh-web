create table externalusers (
    shipment bigint(20) not null,
    user bigint(20) not null,
    primary key (shipment , user),
    foreign key (shipment)
        references shipments (id)
        ON DELETE CASCADE,
    foreign key (user)
        references users (id)
        ON DELETE CASCADE
);

create table externalcompanies (
    shipment bigint(20) not null,
    company bigint(20) not null,
    primary key (shipment , company),
    foreign key (shipment)
        references shipments (id)
        ON DELETE CASCADE,
    foreign key (company)
        references companies (id)
        ON DELETE CASCADE
);
