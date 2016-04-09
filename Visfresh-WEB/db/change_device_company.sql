-- create device backup 
insert into devices (description, imei, name, company, tripcount, active)
select description, '${oldcompany}_${deviceImei}', name, ${new_company}, tripcount, active from devices where imei = '${deviceImei}';

-- delete device commands
delete from devicecommands where device = '${deviceImei}';

-- update arrivals 
update arrivals set device = '${oldcompany}_${deviceImei}' where device = '${deviceImei}';

-- update alerts
update alerts set device = '${oldcompany}_${deviceImei}' where device = '${deviceImei}';

-- update tracker events
update trackerevents set device = '${oldcompany}_${deviceImei}' where device = '${deviceImei}';

-- update shipments
update shipments set device = '${oldcompany}_${deviceImei}' where device = '${deviceImei}';
