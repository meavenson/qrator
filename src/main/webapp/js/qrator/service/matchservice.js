var MatchService = function(app){
	this.app = app;
	this.address = "service/match";
	this.util = new ServiceUtil(app, this.address);
};

MatchService.prototype = {

	stop: function(after){
		var t = this;
		var action = "stop";
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	list: function(structure, after){
		var t = this;
		var action = "list/"+structure;
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	}

};