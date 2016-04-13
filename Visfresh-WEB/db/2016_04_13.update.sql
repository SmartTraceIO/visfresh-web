drop table if exists devicegrouprelations;
drop table if exists devicegroups;

create table devicegroups (
    id bigint(20) auto_increment not null,
    name varchar(127) not null,
    description varchar(255),
    company bigint(20),
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table devicegrouprelations (
    device varchar(30) not null,
    `group` bigint(20) auto_increment not null,
    primary key (device, `group`),
    FOREIGN KEY (`device`)
        REFERENCES devices (imei)
		ON DELETE CASCADE,
    FOREIGN KEY (`group`)
        REFERENCES devicegroups (id)
		ON DELETE CASCADE
);
