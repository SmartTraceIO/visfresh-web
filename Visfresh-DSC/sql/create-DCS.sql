drop TABLE if exists devicemsg;
CREATE TABLE devicemsg (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  imei varchar(15) NOT NULL,
  type varchar(4) NOT NULL,
  time datetime NOT NULL,
  battery int NOT NULL,
  temperature float NOT NULL,
  processor varchar(32),
  retryon timestamp NOT NULL default CURRENT_TIMESTAMP,
  numretry int not null default 0,
  stations varchar(256) NOT NULL,
  PRIMARY KEY (id)
);
