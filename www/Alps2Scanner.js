var exec = require('cordova/exec');

var PLUGIN_NAME = 'Alps2Scanner';

var Alps2Scanner = {
	getIsInitialized: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "getMServiceInit");
	},
	uninitializeService: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "uninitMService");
	},
	initializeService: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "initMService");
	},
	triggerUHFContinuous: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "triggerUHFContinuous");
	},
	triggerUHFSingle: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "triggerUHFSingle");
	},
	triggerUHFEnd: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "triggerUHFEnd");
	},
	getUHFLoop: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "getUHFLoop");
	},
	scanBarcode: function (success, failure) {
		exec(success, failure, PLUGIN_NAME, "scanBarcode");
	}
}

module.exports = Alps2Scanner;
