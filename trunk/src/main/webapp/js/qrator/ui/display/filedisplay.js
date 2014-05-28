var FileDisplay = function(pane, options){
	var t = this;
	t.files = [];
	t.pane = pane;
	t.app = pane.app;
	t.service = t.app.fileService;
	t.readOnly = options.readOnly;
	t.meta = new MetaDisplay(t.app, {"service":t.service, "readOnly":t.readOnly});
	t.renderer = t.app.renderer;
	t.filter = new FileFilter(t.app);
	t.offset = 0;
	t.next = $("<li/>").addClass("q-next-results").html("<i class='icon-chevron-down'/> Next 10");
	
	// an "enhancement" function to run on the objects viewed by this display
	t.enhancer = options.enhancer;
	
	// structure containing element
	var buttons = t.initButtons();
	var container = $("<div/>").css("height","100%");
	var structs = $("<div/>").addClass("q-container");
	t.list = $("<ul/>").addClass("q-list");
	container.append(structs.append(buttons).append(t.list))
			 .append(t.meta.container);
	t.container = container;
};

FileDisplay.prototype = {

	initButtons: function(){
		var t = this;
		var add = $("<button type='button' class='btn btn-primary'><i class='icon-filter icon-white'/> Filter</button>")
			.click(function(e){

				// use popover function found in utils.js
				var elmt = $(this);
				
				var funcs = { "Search": function(){
									t.filter.refresh();
									t.search(10);
							  },
							  "Cancel": function(){}
							};
				popover(elmt, "Filter", "bottom", t.filter.container, funcs);
				t.filter.enable();
				e.stopPropagation();
		});
		
		var container = $("<div/>")
			.addClass("q-button-container")
			.append(add);
			
		return container;
	},

	search: function(limit){
		this.clear();
		this.meta.clear();
		this.getFiles(limit);
	},

	nextFiles: function(limit){
		this.getFiles(limit);
	},

	getFiles: function(limit){
		var t = this;
		t.app.loading();
		var callback = function(resp){
			var files = resp["objs"];
			if(files && files.length > 0){
				t.parseFiles(files);
				t.offset += files.length;
			}else{
				var response = resp["error"]? resp["error"] : resp["message"];
				if(resp["error"]) t.app.notifier.error(response);
				else if(response) t.app.notifier.message(response);
			}
			t.app.loaded();
		};
		var filter = t.filter.filter;
		t.service.list(filter["status"], 
					   filter["tree"], 
					   filter["type"],
					   filter["owned"],
					   t.offset, limit, callback);
	},
	
	parseFiles: function(files){
		var t = this;
		
		for(var i=0; i<files.length; i++){
			var file = new File(files[i].id, files[i]);
			var index = t.files.length;
			t.files[index] = file;
			file.createDiagram(t.renderer);
			
			file.avatar.click( function(e){
									$(this).parent().parent().find(".ui-selected").each(function(key, val){
																							var elmt = $(val);
																							elmt.removeClass("ui-selected");
																							elmt.data("obj").hideButtons();
																						});
									$(this).addClass("ui-selected");
									var obj = $(this).data("obj");
									obj.showButtons();
									if(t.meta){
										t.meta.getMeta(obj);
									}
								});
								
			if(t.enhancer) t.enhancer(file);
			
			var row = $("<li/>").addClass("q-struct").append(file.avatar);
			t.list.append(row);
			t.renderer.renderStructure(file);
			file.hideButtons();
		}
		if(files.length < 10) t.next.remove();
		else t.list.append( t.next.unbind("click").click(function(e){ t.nextFiles(10); }) );
	},
	
	clear: function(){
		var t = this;
		for(var i=0; i<t.files.length; i++){
			t.files[i].graph.disconnect();
			t.files[i].avatar.remove();
		}
		t.files = [];
		t.offset = 0;
		t.list.empty();
	},
	
	show: function(){
		this.container.show();
	},
	
	hide: function(){
		this.container.hide();
	}

};