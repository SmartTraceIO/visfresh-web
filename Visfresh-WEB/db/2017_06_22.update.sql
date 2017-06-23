create table grouplocks (
    `type` varchar(20) not null,
    `group` varchar(128) not null,
    locker varchar(128) not null,
    lastupdate timestamp null default null,
    primary key (`type`, `group`)
);
