-- drops
drop table if exists usershipments;
drop table if exists shipmentdevices;
drop table if exists arrivalnotifschedules;
drop table if exists alertnotifschedules;
drop table if exists shipments;
drop table if exists personalschedules;
drop table if exists notificationschedules;
drop table if exists alertprofiles;
drop table if exists locationprofiles;
drop table if exists notifications;
drop table if exists trackerevents;
drop table if exists devicecommands;
drop table if exists arrivals;
drop table if exists alerts;
drop table if exists devices;
drop table if exists userprofiles;
drop table if exists users;
drop table if exists companies;

-- creates
create table companies (
   id bigint(20) auto_increment not null,
   `name` varchar(255) not null,
   description varchar(255) default null,
   PRIMARY KEY (`id`)
);

create table devices (
   id varchar(127) not null,
   description varchar(255),
   imei varchar(30) not null,
   name varchar(127) not null,
   sn varchar(20) default null,
   company bigint(20),
   primary key (id),
   FOREIGN KEY (company) REFERENCES companies(id)   
);

create table alerts (
   id bigint(20) auto_increment not null,
   `type` varchar(50) not null,
   `name` varchar(127) not null,
   description varchar(255) default null,
   temperature double not null,
   minutes int not null,
   `date` timestamp not null,
   device varchar(127) not null,
   primary key (id),
   FOREIGN KEY (device) REFERENCES devices(id)   
);

create table arrivals (
   id bigint(20) auto_increment not null,
   nummeters int not null,
   `date` timestamp not null,
   device varchar(127) not null,
   primary key (id),
   FOREIGN KEY (device) REFERENCES devices(id)   
);

create table devicecommands (
   id bigint(20) auto_increment not null,
   command varchar(127) not null,
   device varchar(127) not null,
   `date` timestamp default now(),
   primary key (id),
   FOREIGN KEY (device) REFERENCES devices(id)   
);

create table users (
   username varchar(127) not null,
   `password` varchar(127) default null, -- encripted password
   fullname varchar(255) not null,
   roles  varchar(255) not null,
   company bigint(20) not null,
   primary key (username),
   FOREIGN KEY (company) REFERENCES companies(id)   
);

create table userprofiles(
   user varchar(127) not null,
   primary key (user),
   FOREIGN KEY (user) REFERENCES users(username) ON DELETE CASCADE  
);

create table trackerevents (
   id bigint(20) auto_increment not null,
   `type` varchar(20) not null,
   `time` timestamp not null,
   battery int not null,
   temperature double not null,
   device varchar(127) not null,
   primary key (id),
   FOREIGN KEY (device) REFERENCES devices(id)   
);

create table notifications (
   id bigint(20) auto_increment not null,
   `type` varchar(20) not null,
   issue bigint(20) not null,
   user varchar(20) not null,
   primary key (id),
   FOREIGN KEY (user) REFERENCES users(username)
);

create table locationprofiles (
   id bigint(20) auto_increment not null,
   `name` varchar(127) not null,
   companydetails varchar(255) default null,
   notes varchar(255) default null,
   address varchar(127) default null,
   company bigint(20) not null,
   `start` boolean not null,
   `stop` boolean not null,
   interim boolean not null,
   latitude double not null,
   longitude double not null,
   radius int not null,
   primary key (id),
   FOREIGN KEY (company) REFERENCES companies(id)   
);

create table alertprofiles (
   id bigint(20) auto_increment not null,
   `name` varchar(127) not null,
   description varchar(255) default null,
   lowtemp float not null,
   criticallowtem float not null,
   lowtempformorethen int not null,
   criticallowtempformorethen int not null,
   hightemp float not null,
   criticalhightemp float not null,
   hightempformorethen int not null,
   criticalhightempformorethen int not null,
   onenterbright boolean not null,
   onenterdark boolean not null,
   onshock boolean not null,
   onbatterylow boolean not null,
   company bigint(20) not null,
   primary key (id),
   FOREIGN KEY (company) REFERENCES companies(id)   
);

create table notificationschedules(
   id bigint(20) auto_increment not null,
   `name` varchar(127) not null,
   description varchar(255) default null,
   company bigint(20) not null,
   primary key (id),
   FOREIGN KEY (company) REFERENCES companies(id)   
);

create table personalschedules(
   id bigint(20) auto_increment not null,
   firstname varchar(31),
   lastname varchar(31),
   company varchar(127),
   position varchar(127),
   sms varchar(31),
   email varchar(31),
   pushtomobileapp boolean not null,
   weekdays varchar(50),
   fromtime int not null,
   totime int not null,
   `schedule` bigint(20) not null,
   primary key (id),
   FOREIGN KEY (`schedule`) REFERENCES notificationschedules(id)   
);

create table shipments(
   -- common fiels
   id bigint(20) auto_increment not null,
   istemplate boolean not null default true,
   `name` varchar(127) not null,
   description varchar(255) default null,
   alert bigint(20) not null,
   noalertsifcooldown int not null,
   arrivalnotifwithIn int not null,
   nonotifsifnoalerts boolean not null,
   shutdowntimeout int not null,
   shippedfrom bigint(20) not null,
   shippedto bigint(20) not null,
   company bigint(20) not null,
   primary key (id),
   FOREIGN KEY (company) REFERENCES companies(id),
   FOREIGN KEY (alert) REFERENCES alertprofiles(id),
   FOREIGN KEY (shippedfrom) REFERENCES locationprofiles(id),
   FOREIGN KEY (shippedto) REFERENCES locationprofiles(id),

   -- template fields
   adddatashipped boolean not null default false,
   detectlocation boolean not null default false,
   usecurrenttime boolean not null default true,

   -- Shipment fields
   palletid varchar(31) default null,
   ponum varchar(31) default null,
   descriptiondate timestamp,
   customfiels longtext default null,
   `status` varchar(31) default null
);

create table alertnotifschedules(
   shipment bigint(20) not null,
   notification bigint(20) not null,
   primary key (shipment, notification),
   foreign key (shipment)
      references shipments(id) ON DELETE CASCADE,
   foreign key (notification)
      references notificationschedules(id) ON DELETE CASCADE
);

create table arrivalnotifschedules(
   shipment bigint(20) not null,
   notification bigint(20) not null,
   primary key (shipment, notification),
   foreign key (shipment)
      references shipments(id) ON DELETE CASCADE,
   foreign key (notification)
      references notificationschedules(id) ON DELETE CASCADE
);

create table shipmentdevices(
   shipment bigint(20) not null,
   device varchar(20) not null,
   primary key (shipment, device),
   foreign key (shipment)
      references shipments(id) ON DELETE CASCADE,
   foreign key (device)
      references devices(id) ON DELETE CASCADE
);

create table usershipments(
   shipment bigint(20) not null,
   user varchar(20) not null,
   primary key (shipment, user),
   foreign key (shipment)
      references shipments(id) ON DELETE CASCADE,
   foreign key (user)
      references userprofiles(user) ON DELETE CASCADE
);
