-- Add devices
INSERT INTO devices
(imei, company, active)
VALUES
('354430070001467', 2, true),
('354430070001541', 2, true),
('354430070001558', 2, true),
('354430070001921', 2, true),
('354430070002564', 2, true),
('354430070002788', 2, true),
('354430070005534', 2, true),
('354430070005807', 2, true),
('354430070006680', 2, true),
('354430070007001', 2, true),
('354430070007555', 2, true),
('354430070010542', 2, true);

-- Set devce names
update devices set name = imei where imei <> '';
