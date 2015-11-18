rename table allerttemperatures to temperaturerules;
ALTER TABLE personalschedules change pushtomobileapp sendapp boolean not null default false;

-- send email
alter table personalschedules add column sendemail boolean;
update personalschedules set sendemail = false where id <> 0; -- id <> 0 for suppress safe mode
alter table personalschedules modify column sendemail boolean not null default false;

-- send SMS
alter table personalschedules add column sendsms boolean;
update personalschedules set sendsms = false where id <> 0; -- id <> 0 for suppress safe mode
alter table personalschedules modify column sendsms boolean not null default false;

