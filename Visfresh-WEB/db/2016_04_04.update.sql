create table simulators (
    source varchar(30) not null,
    target varchar(30) not null,
    user bigint(20) not null,
    user bigint(20) not null,
    primary key (user),
    FOREIGN KEY (user) REFERENCES users (id),
    FOREIGN KEY (target) REFERENCES devices (imei) ON DELETE CASCADE
);

alter table simulators add column started boolean not null default false;
