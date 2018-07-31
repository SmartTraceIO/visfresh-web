CREATE TABLE locationrequests (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  -- geo location request fields
  userdata longtext, -- data supplied by requestor.
  buffer longtext NOT NULL, -- request/response
  `type` varchar(32) NOT NULL, -- request type (UnwiredLabs or other)
  sender varchar(32) NOT NULL, -- request sender. The sender identifies self by given field
  status varchar(16), -- error | success | NULL
  -- retryable fields
  retryon timestamp NULL default NULL,
  numretry int not null default 0,
  PRIMARY KEY (id)
);
