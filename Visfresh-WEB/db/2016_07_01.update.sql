update shipments set lasteventdate = (
	select time from trackerevents
	where trackerevents.shipment = shipments.id
	order by id desc limit 1
) where lasteventdate is NULL and id <> 0;
