-- This company is virtual and is not linked to any GTSE accaunt
INSERT INTO companies (`name`, description)
VALUES('Developers','Virtual Company for Visfresh developers');
-- Global administrator
INSERT INTO users (email, `password`, firstname, lastname, roles, company)
select 'globaladmin@visfresh.com', '2ced879c244faaaa6f00c05f3aa11f94', 'GlobalAdmin', 'Administrator','SmartTraceAdmin', id
   from companies where `name` = 'Developers' limit 1;
   
-- Create Shipment company
INSERT INTO companies (`name`, description)
VALUES('Demo','Work company for rest client');
