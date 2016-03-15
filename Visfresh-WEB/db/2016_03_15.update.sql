create table autostartlocations_tmp (
    config bigint(20) not null,
    location bigint(20) not null,
    direction varchar(8) not null,
    primary key (config, location, direction),
    FOREIGN KEY (location)
        REFERENCES locationprofiles (id) ON DELETE CASCADE,
    FOREIGN KEY (config)
        REFERENCES autostartshipments (id) ON DELETE CASCADE
);

insert into autostartlocations_tmp (config, location, direction)
select config, location, direction from autostartlocations;

drop table autostartlocations;
alter table autostartlocations_tmp rename autostartlocations;
