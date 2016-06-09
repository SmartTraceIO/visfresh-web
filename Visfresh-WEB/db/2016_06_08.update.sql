alter table shipments add column siblings longtext default NULL;

-- create tmp table
create table tmpsiblings(
    id bigint(20),
    siblings longtext default NULL,
    primary key (id),
    FOREIGN KEY (id)
        REFERENCES shipments (id)
);

insert into tmpsiblings (id, siblings)
select
 s.id,
 (select group_concat(s1.id separator ',') from shipments s1
 where s1.siblinggroup = s.siblinggroup and s1.id <> s.id)
 as siblings 
from shipments s where not s.siblinggroup is NULL;

update shipments set siblings = 
(select siblings from tmpsiblings where tmpsiblings.id = shipments.id)
where not siblinggroup is NULL and id <> -1;

drop table tmpsiblings;
alter table shipments drop column siblinggroup;
