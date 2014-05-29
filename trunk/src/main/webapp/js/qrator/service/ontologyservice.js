var OntologyService = function(app){
	this.app = app;
	this.address = "service/ontology";
	this.util = new ServiceUtil(app, this.address);
};

OntologyService.prototype = {

	tree: function(tree, after){
		var t = this;
		var action = "tree";		
		var data = { "name": tree };		
		t.util.request(true, action, data, after);
	}

};