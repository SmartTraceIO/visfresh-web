-- drops
drop table if exists devicestates;
drop table if exists alertnotifschedules;
drop table if exists arrivalnotifschedules;
drop table if exists devicecommands;
drop table if exists systemmessages;
drop table if exists usershipments;
drop table if exists trackerevents;
drop table if exists arrivals;
drop table if exists alerts;
drop table if exists notifications;
drop table if exists userprofiles;
drop table if exists shipmentdevices;
drop table if exists autostartlocations;
drop table if exists autostartshipments;
drop table if exists shipments;
drop table if exists locationprofiles;
drop table if exists personalschedules;
drop table if exists notificationschedules;
drop table if exists temperaturerules;
drop table if exists alertprofiles;
drop table if exists devicegrouprelations;
drop table if exists devices;
drop table if exists devicegroups;
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
    description varchar(255),
    imei varchar(30) not null,
    name varchar(127) not null,
    company bigint(20),
    autostart bigint(20) default null,
    tripcount int not null default 0,
    active boolean not null default true,
    primary key (imei),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table devicegroups (
    name varchar(127) not null,
    description varchar(255),
    company bigint(20),
    primary key (name),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table devicegrouprelations (
    device varchar(30) not null,
    `group` varchar(127) not null,
    primary key (device, `group`),
    FOREIGN KEY (`device`)
        REFERENCES devices (imei)
		ON DELETE CASCADE,
    FOREIGN KEY (`group`)
        REFERENCES devicegroups (name)
		ON DELETE CASCADE
);

create table devicecommands (
    id bigint(20) auto_increment not null,
    command varchar(127) not null,
    device varchar(127) not null,
    `date` timestamp default CURRENT_TIMESTAMP,
    primary key (id),
    FOREIGN KEY (device)
        REFERENCES devices (imei)
);

create table users (
    id bigint(20) auto_increment not null,
    `password` varchar(127) default null,
    firstname varchar(127),
    lastname varchar(127),
    external boolean not null default false,
    externalcompany varchar(127) default null,
    position varchar(127),
    email varchar(127) not null,
    phone varchar(20),
    roles varchar(255) not null,
    company bigint(20) not null,
    tempunits varchar(20) not null default 'Celsius',
    timezone varchar(31) not null default 'UTC',
    devicegroup varchar(127),
    `language` varchar(20) not null default 'English',
	measureunits varchar(20) not null default 'Metric',
    scale varchar(127),
	title varchar(10),
	active boolean not null default true,
	settings longtext,
	unique (email),
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table notifications (
    id bigint(20) auto_increment not null,
    `type` varchar(20) not null,
    issue bigint(20) not null,
    user bigint(20) not null,
    isread boolean not null default false,
    primary key (id),
    FOREIGN KEY (user)
        REFERENCES users (id)
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
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table alertprofiles (
    id bigint(20) auto_increment not null,
    `name` varchar(127) not null,
    description varchar(255) default null,
    onenterbright boolean not null,
    onenterdark boolean not null,
    onmovementstart boolean not null,
    onmovementstop boolean not null,
    onbatterylow boolean not null,
    company bigint(20) not null,
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table temperaturerules (
    id bigint(20) auto_increment not null,
    `type` varchar(50) not null,
    temp float not null,
    timeout int not null,
    cumulative boolean not null default false,
    alertprofile bigint(20) not null,
    primary key (id),
    foreign key (alertprofile)
		references alertprofiles (id)
		on delete cascade
);

create table notificationschedules (
    id bigint(20) auto_increment not null,
    `name` varchar(127) not null,
    description varchar(255) default null,
    company bigint(20) not null,
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id)
);

create table personalschedules (
    id bigint(20) auto_increment not null,
    user  bigint(20) not null,
    sendapp boolean not null default false,
    sendemail boolean not null default false,
    sendsms boolean not null default false,
    weekdays varchar(50),
    fromtime int not null,
    totime int not null,
    `schedule` bigint(20) not null,
    primary key (id),
    FOREIGN KEY (`schedule`)
        REFERENCES notificationschedules (id)
		ON DELETE CASCADE,
    FOREIGN KEY (`user`)
        REFERENCES users (id)
		ON DELETE CASCADE
);

create table shipments (
    id bigint(20) auto_increment not null,
    istemplate boolean not null default true,
    isautostart boolean not null default false,
    `name` varchar(50),
    description varchar(120) default null,
    alert bigint(20),
    noalertsifcooldown int not null,
    arrivalnotifwithIn int not null,
    nonotifsifnoalerts boolean not null,
    shutdownafterarrivalminutes int default null,
	noalertsafterarrivalminutes int default null,
	shutdownafterstartminutes int default null,
    shippedfrom bigint(20),
    shippedto bigint(20),
    assettype varchar(127),
    company bigint(20) not null,
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id),
    FOREIGN KEY (alert)
        REFERENCES alertprofiles (id),
    FOREIGN KEY (shippedfrom)
        REFERENCES locationprofiles (id),
    FOREIGN KEY (shippedto)
        REFERENCES locationprofiles (id),
    adddatashipped boolean not null default false,
    detectlocation boolean not null default false,
    usecurrenttime boolean not null default true,
    device varchar(20),
    palletid varchar(31),
    assetnum varchar(31),
    shipmentdate timestamp,
    lasteventdate timestamp default '00-01-01 00:00:00',
    arrivaldate timestamp default '00-01-01 00:00:00',
    customfiels longtext,
    comments varchar(300),
    ponum int not null default 0,
    tripcount int not null default 0,
    `status` varchar(31) default null,
    siblinggroup bigint(20) default null,
    siblingcount int not null default 0,
    foreign key (device)
        references devices (imei)
);

create table autostartshipments (
    id bigint(20) auto_increment not null,
    company bigint(20) not null,
    template bigint(20) not null,
    priority int not null default 0,
    primary key (id),
    FOREIGN KEY (company)
        REFERENCES companies (id),
    FOREIGN KEY (template)
        REFERENCES shipments (id) ON DELETE CASCADE
);

create table autostartlocations (
    config bigint(20) not null,
    location bigint(20) not null,
    direction varchar(8) not null,
    primary key (config, location),
    FOREIGN KEY (location)
        REFERENCES locationprofiles (id) ON DELETE CASCADE,
    FOREIGN KEY (config)
        REFERENCES autostartshipments (id) ON DELETE CASCADE
);

create table alerts (
    id bigint(20) auto_increment not null,
    `type` varchar(50) not null,
    temperature double not null,
    minutes int not null,
    `date` timestamp not null,
	cumulative boolean not null default true,
    device varchar(127) not null,
    shipment bigint(20) not null,
    rule bigint(20) default null, -- reference to rule for temperature alerts
    event bigint(20),
    primary key (id),
    foreign key (shipment)
        references shipments (id),
    foreign key (device)
        references devices (imei)
);

create table arrivals (
    id bigint(20) auto_increment not null,
    nummeters int not null,
    `date` timestamp not null,
    device varchar(127) not null,
    shipment bigint(20) not null,
    event bigint(20),
    primary key (id),
    foreign key (shipment)
        references shipments (id),
    foreign key (device)
        references devices (imei)
);

create table trackerevents (
    id bigint(20) auto_increment not null,
    `type` varchar(20) not null,
    `time` timestamp not null,
    battery int not null,
    temperature double not null,
    latitude double not null,
    longitude double not null,
    device varchar(127) not null,
    shipment bigint(20),
    primary key (id),
    foreign key (shipment)
        references shipments (id),
    foreign key (device)
        references devices (imei)
);

create table alertnotifschedules (
    shipment bigint(20) not null,
    notification bigint(20) not null,
    primary key (shipment , notification),
    foreign key (shipment)
        references shipments (id)
        ON DELETE CASCADE,
    foreign key (notification)
        references notificationschedules (id)
        ON DELETE CASCADE
);

create table arrivalnotifschedules (
    shipment bigint(20) not null,
    notification bigint(20) not null,
    primary key (shipment , notification),
    foreign key (shipment)
        references shipments (id)
        ON DELETE CASCADE,
    foreign key (notification)
        references notificationschedules (id)
        ON DELETE CASCADE
);

create table systemmessages (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    type varchar(128) NOT NULL,
    time datetime NOT NULL,
    processor varchar(32),
    retryon timestamp NOT NULL default CURRENT_TIMESTAMP,
    numretry int not null default 0,
    message varchar(512) not null,
    PRIMARY KEY (id)
);

create table devicestates (
    device varchar(20),
    state longtext,
    primary key (device),
    foreign key (device)
        references devices (imei) on delete cascade
);