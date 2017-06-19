alter table temperaturerules add column corractions bigint(20);
alter table temperaturerules add foreign key (corractions) references correctiveactions(id);

alter table alertprofiles add column lightonactions bigint(20);
alter table alertprofiles add foreign key (lightonactions) references correctiveactions(id);

alter table alertprofiles add column batterylowactions bigint(20);
alter table alertprofiles add foreign key (batterylowactions) references correctiveactions(id);
