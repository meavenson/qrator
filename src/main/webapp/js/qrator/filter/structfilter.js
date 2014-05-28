var StructureFilter = function(app, options){
	Filter.call(this, app);
	var t = this;
	
	// search by id
	var source = $("<input type=\"hidden\"/>");
	var refId = $("<input type=\"text\"/>").addClass("form-control");
	
	var group = $("<div/>").addClass("input-group");
	var groupBtn = $("<div/>").addClass("input-group-btn");
	var btn = $("<button type=\"button\" data-toggle=\"dropdown\"/>").addClass("btn btn-default dropdown-toggle").dropdown();
	var dd = $("<ul/>").addClass("dropdown-menu");
	
	group.append(groupBtn.append(btn).append(dd)).append(refId);
	
	var switchSource = function(opt){
		return function(){
			btn.html(opt.text()+" <span class=\"caret\"/>");
			btn.dropdown("toggle");
			source.val(opt.data("id"));
		}
	};
	
	t.app.structService.sources(function(resp){
		if(resp["objs"]){
			var objs = resp["objs"];
			$.each(objs, function(index, val){
				var opt = $("<li><a href=\"#\">"+val.name+"</a></li>");
				opt.data("uri", val.uri);
				opt.data("id", val.id);
				opt.click(switchSource(opt));
				dd.append(opt);
			});
			var selected = dd.children().first();
			btn.html(selected.text()+" <span class=\"caret\"/>");
			source.val(resp["objs"][0].id);
		}
	});
	
	t.generateField({ "id":"reference", "name":"ID", 	 "elmt":group });
	t.generateField({ "id":"source", 	"name":"Source", "elmt":source });
	
	// end search by id
	
	t.container.append("<hr/>");
	
	if(!options || (options && options["status"])){
		var statusOpts = {
			"any":undefined, 
			"pending":"pending", 
			"reviewed":"reviewed", 
			"deferred":"deferred", 
			"approved":"approved", 
			"rejected":"rejected",
			"committed":"committed"
		};
		var status = t.generateSelect(statusOpts);
		var field = { "id":"status", "name":"Review Status", "elmt":status };
		t.generateField(field);
	}
	
	if(!options || (options && options["tree"])){
		// setup tree select
		var treeOpts = { "any": undefined };
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
					var typeOpts = { "any" : undefined };
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
			t.fields[field.id] = field;
			t.generateField(field);
			t.chainSelect("tree", "type", typeFunc);
		};
		app.treeService.listTrees(callback);
	}

};

StructureFilter.prototype = new Filter();