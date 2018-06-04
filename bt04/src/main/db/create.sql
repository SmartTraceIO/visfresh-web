create table beaconchannels (
    beacon varchar(30) not null,
    gateway varchar(30) not null,
    unlockon datetime not null,
    primary key (beacon)
);
