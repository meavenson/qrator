var AssignType = function(app){
	Filter.call(this, app);
	var t = this;
		
	// setup tree select
	var treeOpts = {};
	var callback = function(resp){
		var trees = resp["objs"];
		
		for(var i=0; i<trees.length; i++){
			treeOpts[trees[i].name.replace(/_/g," ")] = trees[i].id;
		}
		var treeSelect = t.generateSelect(treeOpts);
		
		// setup type chaining select
		var typeFunc = function(param, after){
			var callback = function(resp){
				var types = resp["objs"];
				var typeOpts = {};
				for(var i=0; i<types.length; i++){
					typeOpts[types[i].name] = types[i].id;
				}
				if(t.fields["type"]) t.fields["type"].input.remove();
				var typeSelect = t.generateSelect(typeOpts);
				var field = { "id":"type", "name":"Subtype", "elmt":typeSelect };
				t.generateField(field);
				if(after) after();
			};
			if(param) t.app.treeService.listTypes(param, callback);
			else if(t.fields["type"]) t.fields["type"].input.remove();
		};
		
		var field = { "id":"tree", "name":"Type", "elmt":treeSelect };
		t.generateField(field);
		t.chainSelect("tree", "type", typeFunc);
	};
	app.treeService.listTrees(callback);

};

AssignType.prototype = new Filter();