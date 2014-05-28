var StructureDisplay = function(pane, options){
	var t = this;
	t.structs = [];
	t.pane = pane;
	t.app = pane.app;
	t.readOnly = options.readOnly;
	t.service = t.app.structService;
	t.meta = new MetaDisplay(t.app, {"service":t.service, "readOnly":t.readOnly});
	t.searchFunc = function(){ t.filterStructures(); };
	t.filter = new StructureFilter(t.app);
	t.page = 0;
	t.limit = 10;
	t.enhancer = options.enhancer;
	t.curation = options.curation;
	
	// structure containing element
	var buttons = t.initButtons();
	var container = $("<div/>").css("height","100%");
	var structs = $("<div/>").addClass("q-container").css({"width":"570px"});
	t.list = $("<ul/>").addClass("q-list q-struct-list");
	container.append(structs.append(buttons).append(t.list))
			 .append(t.meta.container);
	t.container = container;
};

StructureDisplay.prototype = {

	initButtons: function(){
		var t = this;
		var filter = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon-filter'/> Filter</button>")
			.click(function(e){

				// use popover function found in utils.js
				var elmt = $(this);
				
				var funcs = { "Filter": function(){
									t.filter.refresh();
									t.filterStructures(t.builder? t.builder.serialize(t.builder.getRoot()) : false);
							  },
							  "Cancel": function(){}
							};
				popover(elmt, "Filter Structures", "bottom", t.filter.container, funcs);
				t.filter.enable();
				e.stopPropagation();
		});
		var search = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon-search'/> Search</button>")
			.click(function(e) {
				var callback = function(e, motif){
					t.filterStructures(motif);
				};
				var options = {"notifier":t.app.notifier, 
							   "callback":callback, 
							   "title":"Structure Search", 
							   "submitLabel":"Search"};
				t.builder = t.builder? t.builder : new StructBuilder(options);
				t.builder.dialog.container.modal("show");
			});
		
		t.pages = $("<ul/>").addClass("pagination pagination-sm q-pagination");
		
		var container = $("<div/>")
			.addClass("q-button-container")
			.append(filter.css("margin-right","5px"))
			.append(search)
			.append(t.pages);
			
		return container;
	},

	filterStructures: function(motif){
		this.clear();
		this.page = 0;
		this.meta.clear();
		this.getStructures({"motif":motif});
	},

	getStructures: function(options){
		var t = this;
		t.app.loading();
		t.clear();
		var callback = function(resp){
			var structs = resp["objs"];
			var count = resp["count"];
			t.pages.pagination({
				items: count,
				itemsOnPage: 10,
				displayedPages: 3,
				onPageClick: function(page, e){
					t.page = page-1;
					t.getStructures(options);
				}
			});
			t.pages.pagination("drawPage", t.page+1);
			if(structs && structs.length > 0){
				t.parseStructures(structs);
			}else{
				t.app.message(resp);
			}
			t.app.loaded();
		};
		var motif;
		if(options && options.motif && options.motif.type){
			motif = options.motif;
			//t.service.search(options.motif, t.page*10, t.limit, callback);
		}
		var filter = t.filter.filter;
		if(filter["reference"]){
			t.service.getById(filter["source"], filter["reference"], callback);
		}else{
			t.service.list(filter["status"], 
						   filter["tree"], 
						   filter["type"],
						   t.curation?true:false,
						   motif,
						   t.page*10, t.limit, callback);
		}		
	},
	
	parseStructures: function(structs){
		var t = this;
				
		for(var i=0; i<structs.length; i++){
			var struct = new Structure(structs[i].id, structs[i]);
			t.structs.push(struct);
			
			struct.avatar.click( function(e){
									$(this).parent().parent().find(".ui-selected").each(function(key, val){
																							var elmt = $(val);
																							elmt.removeClass("ui-selected");
																						});
									$(this).addClass("ui-selected");
									var obj = $(this).data("obj");
									if(t.meta){
										t.meta.getMeta(obj);
									}
								});
			
			if(struct.tree != "Unknown"){
				struct.avatar.find(".struct-tree")
							 .empty()
							 .append($("<a href='#'>"+struct.type+"</a>").click(t.treeLink(struct.tree)));
			}
			
			if(t.enhancer) t.enhancer(struct);
			if(struct.type != "Unknown" && struct.status != "committed"){
				struct.avatar.append($("<a href='#' title='comparison with tree' class='struct-link struct-tree-comparison'/>")
						.append("<span class='glyphicon glyphicon-tree-deciduous'/>")
						.click(t.compareFunction(struct)));
			}
			
			var row = $("<li/>").addClass("q-struct").append(struct.avatar);
			t.list.append(row);
			struct.render();
		}
	},
	
	treeLink: function(tree){
		var t = this;
		return function(e){ new ShowTree(t.app, {"tree":tree}); };
	},
	
	compareFunction: function(struct){
		var t = this;
		return function(e){
			var callback = function(resp){
				if(!resp["obj"]){
					t.app.message({"error":"Tree is not loaded."});
				}else{
					var str = JSON.stringify(resp["obj"]);
					if(str.indexOf("NOTATTEMPTED") == -1){
						t.app.message({"message":"No difference from tree."});
					}
					struct.spec = resp["obj"];
					struct.render();
				}
			};
			t.service.compare(struct, callback);
			e.stopPropagation(); 
	   };
	},
	
	clear: function(){
		var t = this;
		for(var i=0; i<t.structs.length; i++){
			t.structs[i].avatar.remove();
		}
		t.structs = [];
		t.list.empty();
		t.pages.empty();
		t.meta.clear();
	},
	
	show: function(){
		this.container.show();
	},
	
	hide: function(){
		this.container.hide();
	}

};