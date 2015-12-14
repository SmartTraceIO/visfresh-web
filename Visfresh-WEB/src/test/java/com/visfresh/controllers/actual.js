var response = {
    "shipmentId" : 525,
	"deviceSN" : "394857",
	"deviceName" : "Device Name",
	"tripCount" : 1,
	"shipmentDescription" : "Any Description",
	"palletId" : "palettid",
	"assetNum" : "10515",
	"assetType" : "SeaContainer",
	"status" : "In Progress",
	"alertProfileId" : 371,
	"alertSuppressionMinutes" : 55,
	"alertsNotificationSchedules" : [ {
		"notificationScheduleId" : 538,
		"notificationScheduleName" : "Sched",
		"notificationScheduleDescription" : "JUnit schedule",
		"peopleToNotify" : "Alexander Suvorov, Alexander Suvorov"
	} ],

	"shippedFrom" : 533,
	"shippedTo" : 534,
	"shipmentDate" : null,
	"currentLocation" : "Not determined",
	"estArrivalDate" : "2015-12-13T10:25",
	"actualArrivalDate" : "2015-12-13T10:25",
	"percentageComplete" : 0,
	"alertProfileName" : "AnyAlert",
	"maxTimesAlertFires" : 0,
	"alertSummary" : {
		"Hot" : "1",
		"Battery" : "1"
	},
	"arrivalNotificationWithinKm" : 111,
	"excludeNotificationIfNoAlerts" : false,
	"arrivalNotificationSchedules" : [ {
		"notificationScheduleId" : 539,
		"notificationScheduleName" : "Sched",
		"notificationScheduleDescription" : "JUnit schedule",
		"peopleToNotify" : "Mikhael Kutuzov, Mikhael Kutuzov"
	} ],
	"commentsForReceiver" : "Comments for receiver",
	"items" : [
			{
				"timestamp" : "2015-12-11T10:25",
				"location" : {
					"latitude" : 50.5,
					"longitude" : 51.51
				},
				"temperature" : 56.0,
				"type" : "AUT",
				"alerts" : [
						{
							"description" : "Battery low at 06:38",
							"type" : "Battery"
						},
						{
							"description" : "Too hot alert - tracker 394857(1) went above 5.0°C degrees for 55 min",
							"type" : "Hot"
						} ],
				"arrivas" : [ {
					"numberOfMetersOfArrival" : 400,
					"arrivalReportSentTo" : ""
				} ]
			}, {
				"timestamp" : "2015-12-11T10:25",
				"location" : {
					"latitude" : 50.5,
					"longitude" : 51.51
				},
				"temperature" : 56.0,
				"type" : "AUT",
				"alerts" : [],
				"arrivas" : []
			} ]
};
