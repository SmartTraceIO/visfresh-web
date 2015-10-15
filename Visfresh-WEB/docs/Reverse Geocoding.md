###This document describles how to use `http://open.gpslogger.org` to do reverse-geocoding.

###1. Rerverse geocoding
see this:
```https://en.wikipedia.org/wiki/Reverse_geocoding```

###2. Open.gpslogger.org
As of MapQuest stop its free reverse geocoding service, we deployed one server for our own at `http://open.gpslogger.org` wit a simple key-management system to share with invited one. To use this service, client needs a `KEY` from `gpslogger.org`

The `KEY` for Vifresh is `o8QMpmtvJD7qvMe56uoftAfdZ4tv43pDzM`

Example of a `GET` request:

```http://open.gpslogger.org/reverse/?format=json&limit=1&key=o8QMpmtvJD7qvMe56uoftAfdZ4tv43pDzM&accept-language=en&addressdetails=1&zoom=18&email=&lat=50.86829&lon=4.38633```

- format can be `json` or `xml`

```json
{
	"place_id":"8579327",
	"licence":"Data © OpenStreetMap contributors, ODbL 1.0. http:\/\/www.openstreetmap.org\/copyright",
	"osm_type":"way",
	"osm_id":"229118496","
	lat":"50.86832835",
	"lon":"4.38630853002619",
	"display_name":"8, Rue Willem Kuhnen - Willem Kuhnenstraat, Schaerbeek - Schaarbeek, Brussels-Capital, 1030, Belgium",
	"address": {
		"house_number":"8",
		"road":"Rue Willem Kuhnen - Willem Kuhnenstraat",
		"town":"Schaerbeek - Schaarbeek",
		"county":"Brussels-Capital",
		"state":"Brussels-Capital",
		"postcode":"1030",
		"country":"Belgium",
		"country_code":"be"
	}
}
```

In XML

```xml
<reversegeocode timestamp="Thu, 15 Oct 15 06:27:29 +0000"
	attribution="Data © OpenStreetMap contributors, ODbL 1.0. http://www.openstreetmap.org/copyright"
	querystring="format=xml&limit=1&key=o8QMpmtvJD7qvMe56uoftAfdZ4tv43pDzM&accept-language=en&addressdetails=1&zoom=18&email=&lat=50.86829&lon=4.38633">
<script/>
	<result place_id="8579327" osm_type="way" osm_id="229118496" lat="50.86832835" lon="4.38630853002619">
		8, Rue Willem Kuhnen - Willem Kuhnenstraat, Schaerbeek - Schaarbeek, Brussels-Capital, 1030, Belgium
	</result>
	<addressparts>
		<house_number>8</house_number>
		<road>Rue Willem Kuhnen - Willem Kuhnenstraat</road>
		<town>Schaerbeek - Schaarbeek</town>
		<county>Brussels-Capital</county>
		<state>Brussels-Capital</state>
		<postcode>1030</postcode>
		<country>Belgium</country>
		<country_code>be</country_code>
		</addressparts>
</reversegeocode>
```