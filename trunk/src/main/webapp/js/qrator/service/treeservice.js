var TreeService = function(app){
	this.app = app;
	this.util = new ServiceUtil(app, "service/tree");
};

TreeService.prototype = {

	spec: function(tree, after){
		var t = this;
		var action = "spec";
		var data = { "name": tree };
		t.util.request(true, action, data, after);
	},
	
	loadTrees: function(after){
		var t = this;
		var action = "loadTrees";
		var data = { "ssid": t.app.uid }
		t.util.request(true, action, data, after);
	},

	listTrees: function(after){
		var t = this;
		var action = "list";
		t.util.request(true, action, {}, after);
	},

	listTypes: function(tree, after){
		var t = this;
		var action = "types/"+tree;
		t.util.request(true, action, {}, after);
	}
	
};