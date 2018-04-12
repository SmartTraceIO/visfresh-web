select d.*,
(select  
    JSON_OBJECT(
        'id', pp.id,
        'company', pp.company,
        'imei', pp.imei,
        'active', IF(pp.active, 'true', 'false')
    )
from pairedphones pp where pp.beaconid = d.imei and pp.active limit 1) as ppa,
(select  
    JSON_OBJECT(
        'id', pp.id,
        'company', pp.company,
        'imei', pp.imei,
        'active', IF(pp.active, 'true', 'false')
    )
from pairedphones pp where pp.beaconid = d.imei and not pp.active limit 1) as ppb

from devices d where d.imei = :imei;