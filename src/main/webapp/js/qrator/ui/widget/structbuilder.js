var StructBuilder = function(options){

	var builder = {};
	var base = {"D-Glcp":		{"name":"Glucose", 						"to": [2,3,4,6],	"from":1},
				"D-Manp":		{"name":"Mannose", 						"to": [2,3,4,6],	"from":1},
				"D-Galp":		{"name":"Galactose", 					"to": [2,3,4,6],	"from":1},
				"D-Gulp":		{"name":"Gulose", 					    "to": [2,3,4,6],	"from":1},
				"D-GlcpNAc":	{"name":"N-acetyl Glucosamine", 		"to": [3,4,6],		"from":1},
				"D-ManpNAc":	{"name":"N-acetyl Mannosamine", 		"to": [3,4,6],		"from":1},
				"D-GalpNAc":	{"name":"N-acetyl Galactosamine", 		"to": [3,4,6],		"from":1},
				"D-GulpNAc":	{"name":"N-acetyl Gulosamine", 		    "to": [3,4,6],		"from":1},
				"Neup":			{"name":"Neuraminic Acid", 				"to": [4,7,8,9],	"from":2},
				"KDNp":			{"name":"Deaminated Neuraminic Acid", 	"to": [4,5,7,8,9],	"from":2},
				"Neup5Ac":		{"name":"Sialic Acid", 					"to": [4,7,8,9],	"from":2},
				"Neup5Gc":		{"name":"N-glycolyl Sialic Acid", 		"to": [4,7,8,9],	"from":2},
				"D-GlcpA":		{"name":"Glucuronic Acid", 				"to": [2,3,4],		"from":1},
				"D-ManpA":		{"name":"Mannuronic Acid", 				"to": [2,3,4],		"from":1},
				"D-GalpA":		{"name":"Galacturonic Acid", 			"to": [2,3,4],		"from":1},
				"D-GulpA":		{"name":"Guluronic Acid", 			    "to": [2,3,4],		"from":1},
				"D-GlcpN":		{"name":"Glucosamine", 					"to": [3,4,6],		"from":1},
				"D-ManpN":		{"name":"Mannosamine", 					"to": [3,4,6],		"from":1},
				"D-GalpN":		{"name":"Galactosamine", 				"to": [3,4,6],		"from":1},
				"D-GulpN":		{"name":"Gulosamine", 				    "to": [3,4,6],		"from":1},
				"L-Fucp":		{"name":"Fucose", 						"to": [2,3,4],		"from":1},
				"D-Quip":		{"name":"Quivonose", 					"to": [2,3,4],		"from":1},
				"L-Rhap":		{"name":"Rhamnose", 					"to": [2,3,4],		"from":1},
				"D-Xylp":		{"name":"Xylose", 						"to": [2,3,4],		"from":1},
				"L-Araf":		{"name":"Arabinose",					"to": [2,3,5],		"from":1},
				"D-Ribf":		{"name":"Ribose",						"to": [2,3,5],		"from":1},
				"L-Talp":		{"name":"Talose", 						"to": [2,3,4,6],	"from":1} 
			   };
	var sub = {"Ac":  {"name":"Acetyl",    "organic":true},
			   "NAc": {"name":"N-Acetyl",  "organic":true},
			   "Me":  {"name":"Methyl",    "organic":true},
			   "P":   {"name":"Phosphate", "organic":false},
			   "S":   {"name":"Sulfate",   "organic":false}
			   };
	var ring = ["p","f","o"];
	var abs =  ["D","L"];
	
	var subLbl   = options && options.submitLabel? options.submitLabel : "Done";
	var title    = options && options.title? options.title : "Structure Builder";
	var dialog   = options && options.modal? options.modal : modal($("<div/>"), "", {}, 900, true);
	var notifier = options && options.notifier? options.notifier : new Notifier($("body"));
	var callback = options && options.callback? options.callback : function(motif){ alert(JSON.stringify(motif)); };
	
	var renderer = new StructRenderer({"builder":builder});
	var substituentPosition = false;
	var linkagePosition = false;
	var selected = false;
	var root = false;
	
	var getRoot = function(){
		return root;
	};
	
	var findBase = function(residue){
		// remove substituents
		var type = residue.id.replace(/[DL]-/g, "")
							 .replace(/[fpo]/g, "")
							 .replace(/5Ac/g, "~")
							 .replace(/5Gc/g, "!")
							 .replace(/\d[a-zA-Z]+/g, "")
							 .replace("~", "5Ac")
							 .replace("!", "5Gc");
		var link = false;
		for(var id in base){
			var mod = id.replace(/[DL]-/g, "").replace(/[fpo]/g, "");
			if(type == mod){
				return base[id];
			}
		}
		return false;
	};
	
	var setSelected = function(node){
		selected = node;
		var baseResidue = findBase(selected);
		substituentPosition.empty();
		if(baseResidue){
			var link = baseResidue.to;
			for(var i=0; i<link.length; i++){
				var l = link[i];
				substituentPosition.append("<option>"+l+"</option>");
			}
		}else notifier.error("Residue unidentified - "+type);
	};
	
	var deleteResidue = function(node, comparison, parent){
		if(node == comparison && !parent) clearAll();
		else{
			if(node == comparison){
				var children = parent.children;
				for(var i=0; i<children.length; i++){
					if(children[i] == node){
						children.splice(i,1);
						break;
					}
				}
			}else if(comparison.children){
				var children = comparison.children;
				for(var i=0; i<children.length; i++){
					deleteResidue(node, children[i], comparison);
				}
			}
		}
	};
	
	var setAnomer = function(anomer){
		return function(){
			if(selected){
				selected.anomer = anomer;
			}else{
				notifier.error("Please select a linkage");
			}
			renderer.build();
			renderer.render();
		};
	};

	var setAbsoluteConfig = function(absConf){
		return function(){
			if(selected){
				var id = selected.id;
				var matches = id.match(/[DL]-/g);
				if(matches != null)
					selected.id = selected.id.replace(matches[0], absConf+"-");
				else selected.id = absConf+"-"+selected.id;
			}else{
				notifier.error("Please select a residue");
			}
			renderer.build();
			renderer.render();
		};
	};

	var setLinkage = function(linkage){
		return function(){
			if(selected){
				var children = selected.parent.children;
				for(var i=0; i<children.length; i++){
					var child = children[i];
					if(child.link == linkage){
						notifier.error("A residue already exists at this position");
						return;
					}
				}
				selected.link = linkage;
			}else{
				notifier.error("Please select a linkage");
			}
			renderer.build();
			renderer.render();
		};
	};

	var addResidue = function(residue){
		return function(){
			var spec = {"id":residue, "anomer":"."};
			if(!renderer.spec){
				renderer.spec = spec;
				root = spec;
			}else{
				if(selected){
					if(!selected.children) selected.children = [];
					spec["from"] = findBase(spec).from;
					spec["parent"] = selected;
					selected.children.push(spec);
				}else{
					notifier.error("Please select a parent residue");
				}
			}
			setSelected(spec);
			renderer.build();
			renderer.render();
		};
	};

	var addSubstituent = function(substituent){
		return function(){
			if(!selected){
				notifier.error("Please select a parent residue");
			}else{
				if(!selected.children) selected.children = [];
				var linkage = substituentPosition.val();
				var id = selected.id;
				var matches = id.replace(/5[AG]c/g, "").match(/\d[a-zA-Z]+/g);
				if(matches != null){
					for(var i=0; i<matches.length; i++){
						if(matches[i].charAt(0) == linkage){
							selected.id = selected.id.replace(matches[i], "");
							break;
						}
					}
				}
				for(var i=0; i<selected.children.length; i++){
					var child = selected.children[i];
					if(child.link == linkage){
						if(!child.anomer){					
							selected.children.splice(i,1);
						}else{
							notifier.error("A residue already exists at this position");
							return;
						}
					}
				}
				if(!sub[substituent].organic){
					selected.children.push({"id":substituent, "link":linkage, "from":substituent});
				}else{
					selected.id = selected.id+linkage+substituent;
				}
			}
			renderer.build();
			renderer.render();
		};
	};

	var addRing = function(ring){
		return function(){
			if(!selected){
				notifier.error("Please select a residue");
			}else{
				var id = selected.id;
				var index = id.search(/[fpo]/g);
				var length = selected.id.length;
				selected.id = selected.id.slice(0,3)+ring+(index > -1? selected.id.slice(index+1,length) : selected.id.slice(3,length));
			}
			renderer.build();
			renderer.render();
		};
	};
	
	var deselect = function(){
		selected = false;
	};
	
	var clearAll = function(){
		delete renderer.spec;
		selected = false;
		root = false;
		renderer.build();
		renderer.render();
	};

	var deleteSelected = function(){
		if(selected){
			var parent = selected.parent;
			deleteResidue(selected, root);
			if(parent) setSelected(parent);
			renderer.build();
			renderer.render();
		}else{
			notifier.error("Please select a residue");
		}
	};
	
	var switchMenu = function(){
		residueMenu.toggle();
		linkageMenu.toggle();
	};

	var initResidueTiles = function(){
		var tiles = $("<div/>").css({"height":"30px", "margin-top":"-20px"});
		for(var id in base){
			var renderer = new StructRenderer({
												"struct" : {
															"spec" : {"id":id, "anomer":" "} 
														   }
											  });
			renderer.container.css({"height":"30px", "width":"30px", "float":"left", "min-width":"20px"});
			tiles.append(renderer.container.click(addResidue(id)));
			renderer.build();
			renderer.render();
		}
		return tiles;
	};

	var initResidueMenu = function(){
		var nav = $("<nav role=\"navigation\"/>").addClass("navbar navbar-default");
		var container = $("<div/>").addClass("container-fluid");

		var body = $("<div/>").addClass("collapse navbar-collapse");
		var navbar = $("<ul/>").addClass("nav navbar-nav");
	
		var anomDD = $("<li/>").addClass("dropdown")
							   .append($("<a href=\"#\" data-toggle=\"dropdown\"/>")
										.addClass("dropdown-toggle")
										.html("Set Anomer <b class=\"caret\"></b>"));
		var anomMenu = $("<ul/>").addClass("dropdown-menu");
	
		var absDD = $("<li/>").addClass("dropdown")
							   .append($("<a href=\"#\" data-toggle=\"dropdown\"/>")
										.addClass("dropdown-toggle")
										.html("Set Absolute Configuration <b class=\"caret\"></b>"));
		var absMenu = $("<ul/>").addClass("dropdown-menu");
	
		var ringDD = $("<li/>").addClass("dropdown")
							   .append($("<a href=\"#\" data-toggle=\"dropdown\"/>")
										.addClass("dropdown-toggle")
										.html("Set Ring Form <b class=\"caret\"></b>"));
		var ringMenu = $("<ul/>").addClass("dropdown-menu");
	
		var subDD = $("<li/>").addClass("dropdown")
							   .append($("<a href=\"#\" data-toggle=\"dropdown\"/>")
										.addClass("dropdown-toggle")
										.html("Add Substituent <b class=\"caret\"></b>"));
		var subMenu = $("<ul/>").addClass("dropdown-menu");
	
		var navbarRt = $("<ul/>").addClass("nav navbar-nav navbar-right");
		var clear = $("<li/>").append($("<a href=\"#\">Clear All</a>")
											.click(function(){
												clearAll();
											})
									  );
		var del = $("<li/>").append($("<a href=\"#\">Delete</a>")
											.click(function(){
												deleteSelected();
											})
									  );
	
		body.append(navbar.append(anomDD.append(anomMenu))
						  .append(absDD.append(absMenu))
						  .append(ringDD.append(ringMenu))
						  .append(subDD.append(subMenu)))
			.append(navbarRt.append(del).append(clear));
		nav.append(container.append(body));

		anomMenu.append($("<li/>").append($("<a href=\"#\">\u03b1</a>")
											.click(setAnomer("a"))));
		anomMenu.append($("<li/>").append($("<a href=\"#\">\u03b2</a>")
											.click(setAnomer("b"))));

		for(var i=0; i<abs.length; i++){
			var ac = abs[i];
			absMenu.append($("<li/>").append($("<a href=\"#\">"+ac+"</a>")
												.click(setAbsoluteConfig(ac))
			));
		}
		for(var i=0; i<ring.length; i++){
			var r = ring[i];
			ringMenu.append($("<li/>").append($("<a href=\"#\">"+r+"</a>")
												.click(addRing(r))
			));
		}
		for(var id in sub){
			var s = id;
			subMenu.append($("<li/>").append($("<a href=\"#\">"+s+"</a>")
												.click(addSubstituent(s))
			));
		}
		
		substituentPosition = $("<select/>").addClass("form-control");
		
		subMenu.append("<li class='divider'/>").click(function(e){e.stopPropagation();})
			   .append("<li class='dropdown-header'>At Position</li>").click(function(e){e.stopPropagation();})
			   .append($("<li/>").css({"padding":"0px 30px"})
								 .append(substituentPosition)
								 .click(function(e){e.stopPropagation();})
						);
		return nav;
	};

	var initLinkageMenu = function(){
		var nav = $("<nav role=\"navigation\"/>").addClass("navbar navbar-default");
		var container = $("<div/>").addClass("container-fluid");

		var body = $("<div/>").addClass("collapse navbar-collapse");
		var navbar = $("<ul/>").addClass("nav navbar-nav");
	
	
	
		var linkDD = $("<li/>").addClass("dropdown")
							   .append($("<a href=\"#\" data-toggle=\"dropdown\"/>")
										.addClass("dropdown-toggle")
										.html("Set Linkage <b class=\"caret\"></b>"));
		linkagePosition = $("<ul/>").addClass("dropdown-menu");
	
		var navbarRt = $("<ul/>").addClass("nav navbar-nav navbar-right");
		var clear = $("<li/>").append($("<a href=\"#\">Clear All</a>")
											.click(function(){
												clearAll();
											})
									  );
		var del = $("<li/>").append($("<a href=\"#\">Delete</a>")
											.click(function(){
												deleteSelected();
											})
									  );
	
		body.append(navbar.append(linkDD.append(linkagePosition)))
			.append(navbarRt.append(del).append(clear));
		nav.append(container.append(body));

		return nav;
	};
	
	var populateLinkages = function(){
		linkagePosition.empty();
		if(selected){
			var baseResidue = findBase(selected.parent);
			link = baseResidue.to;
			if(link){
				for(var i=0; i<link.length; i++){
					var l = link[i];
					linkagePosition.append($("<li/>").append($("<a href=\"#\">"+l+"</a>")
														.click(setLinkage(l))
					));
				}
			}else notifier.error("Residue unidentified - "+type);
		}else{
			notifier.error("Please select a linkage");
		}
	};
	
	var showResidueMenu = function(){
		if(!residueMenu.is(":visible")) switchMenu();
	};
	
	var showLinkageMenu = function(){
		if(!linkageMenu.is(":visible")) switchMenu();
		populateLinkages();
	};
	
	var highlightIncomplete = function(spec){
		var complete = true;
		if( !spec.id || spec.id == "." ) complete = false;
		if( !spec.anomer || spec.anomer == "." ) complete = false;
		if( spec.link == "." ) complete = false;
		if(spec["children"]){
			for(var i=0; i<spec["children"].length; i++){
				highlightIncomplete(spec["children"][i]);
			}
		}
		if(!complete) spec.match = "HIGHLIGHT";
		else delete spec.match;
	};
	
	var addHighlights = function(){
		highlightIncomplete(root);
		renderer.build();
		renderer.render();
	};
	
	var serialize = function(spec){
		var json = {};
		if(spec.id){
			var id = spec.id;
			var abs = ".";
			var matches = id.match(/[DL]-/g);
			if(matches != null){
				abs = matches[0].replace("-","");
				id = id.replace(matches[0], "");
			}
			json["type"] = id;
			json["abconf"] = abs;
			json["anomer"] = spec.anomer;
			if(spec.link){
				json["link"] = spec.link;
				json["from"] = spec.from;
			}
			if(spec["children"]){
				var children = [];
				for(var i=0; i<spec["children"].length; i++){
					children.push(serialize(spec["children"][i]));
				}
				json["children"] = children;
			}
		}
		return json;
	};
	
	var residueMenu = initResidueMenu();
	var linkageMenu = initLinkageMenu().hide();
	var residueTiles = initResidueTiles();
	
	var init = function(){
		var header = $("<h3/>").html(title);
	
		var container = dialog["body"].empty();
		container.append(residueMenu)
				 .append(linkageMenu)
				 .append(residueTiles)
				 .append(renderer.container.css({"height":"300px"}));
	
		var funcs = {};
		//funcs["Debug"] = function(){ alert( JSON.stringify( serialize(root)) ); };
		funcs[subLbl] = function(e){ callback( e, serialize(root) ); };
		//funcs["Cancel"] = function(){};
		
		dialog.setTitle(header);
		dialog.setWidth(900);
		dialog["footer"].empty();
		dialog.generateButtons(funcs);
	};
	
	builder.showResidueMenu = showResidueMenu;
	builder.showLinkageMenu = showLinkageMenu;
	builder.deselect = deselect;
	builder.setSelected = setSelected;
	builder.init = init;
	builder.dialog = dialog;
	builder.addHighlights = addHighlights;
	builder.getSelected = function(){ return selected; };
	builder.getRoot = getRoot;
	builder.serialize = serialize;
	
	init();
	return builder;
};