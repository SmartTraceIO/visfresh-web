-- 8_354430070002291 |
-- 8_354430070010948 |
-- 8_354430070011003 |

-- create device backup 
-- insert into devices (description, imei, name, company, tripcount, active)
-- select description, '8_354430070002291', name, ${new_company}, tripcount, active from devices where imei = '354430070002291';
-- delete device commands
delete from devicecommands where device = '8_354430070011003';
-- update arrivals 
update arrivals set device = '354430070011003' where device = '8_354430070011003';
-- update alerts
update alerts set device = '354430070011003' where device = '8_354430070011003';
-- update tracker events
update trackerevents set device = '354430070011003' where device = '8_354430070011003';
-- update shipments
update shipments set device = '354430070011003' where device = '8_354430070011003';

update shipments set company = 11 where device in ('354430070002291', '354430070010948', '354430070011003');