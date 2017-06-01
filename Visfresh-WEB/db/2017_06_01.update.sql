create table criticalactions (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(256) NOT NULL,
    company bigint(20) not null,
    actions longtext not null,
    primary key (id),
    foreign key (company)
        references companies (id)
        ON DELETE CASCADE
);
