-- create shipment sessions table
create table shipmentsessions (
    shipment bigint(20) NOT NULL,
    state longtext,
    primary key (shipment),
    foreign key (shipment)
        references shipments (id) on delete cascade
);

-- copy data from device states
insert into shipmentsessions(shipment, state)
select 
substring(state,
 locate('"shipmentId":', state) + 13,
 locate(',', substring(state, locate('"shipmentId":', state) + 13)) - 1)
 as shipment,
state from devicestates where locate('"shipmentId":', state) > 0;

insert into shipmentsessions(shipment, state)
select 
substring(state,
 locate('_DS_shipmentId":"', state) + 17,
 locate('"', substring(state, locate('_DS_shipmentId":"', state) + 17)) - 1)
 as shipment,
state from devicestates where locate('_DS_shipmentId":"', state) > 0;
