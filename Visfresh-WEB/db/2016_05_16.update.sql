create table restsessions (
    id bigint(20) auto_increment not null,
    user bigint(20) not null,
    token varchar(50) not null unique,
    expiredon timestamp NULL default NULL,
    createdon timestamp NULL default NULL,
    primary key (id),
    FOREIGN KEY (user)
        REFERENCES users (id)
		on delete cascade
);
