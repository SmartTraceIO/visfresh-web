-- This company is virtual and is not linked to any GTSE accaunt
INSERT INTO companies (`name`, description)
VALUES('Developers','Virtual Company for Visfresh developers');
-- Global administrator
INSERT INTO users (username, `password`, fullname, roles, company)
select 'globaladmin', '2ced879c244faaaa6f00c05f3aa11f94', 'Global Administrator','GlobalAdmin', id
   from companies where `name` = 'Developers' limit 1;
   
-- Create Shipment company
INSERT INTO companies (`name`, description)
VALUES('Developers','Virtual Company for Visfresh developers');

