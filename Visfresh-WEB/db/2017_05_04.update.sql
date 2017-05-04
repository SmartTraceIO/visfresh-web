create table restproperties (
    session bigint(20) not null,
    name varchar(50) not null,
    value varchar(50) not null,
    primary key (session, name),
    FOREIGN KEY (session)
        REFERENCES restsessions (id)
		on delete cascade
);
