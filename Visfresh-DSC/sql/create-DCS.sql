drop TABLE if exists devicemsg;
CREATE TABLE devicemsg (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  imei varchar(15) NOT NULL,
  beacon varchar(15),
  type varchar(4) NOT NULL,
  time datetime NOT NULL,
  battery int NOT NULL,
  temperature float NOT NULL,
  processor varchar(32),
  retryon timestamp NOT NULL default CURRENT_TIMESTAMP,
  numretry int not null default 0,
  stations varchar(256) NOT NULL,
  humidity TINYINT,
  radio varchar(15),
  PRIMARY KEY (id)
);

drop table if exists snapshoots;
create table snapshoots (
  imei varchar(15) NOT NULL,
  signature varchar(255) not null,
  primary key (imei, signature)
);

alter table devicemsg add column radio varchar(15);
