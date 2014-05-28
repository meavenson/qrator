var _metaId = 0;

var MetaDisplay = function(app, options){
	var t = this;
	t.app = app;
	t.service = options.service;
	t.readOnly = options.readOnly;
	t.metaId = _metaId++;
	t.selected = false;
	
	t.aOffset = 0;
	t.rOffset = 0;
	t.pOffset = 0;
	
	var content = $("<div/>").addClass("tab-content");
	t.annotations = $("<div id='anno"+t.metaId+"'/>").addClass("tab-pane active q-meta-container");
	t.references = $("<div id='ref"+t.metaId+"'/>").addClass("tab-pane q-meta-container");
	t.provenance = $("<div id='prov"+t.metaId+"'/>").addClass("tab-pane q-prov-container");
	
	t.aList = $("<ul/>").addClass("q-list q-meta-list");
	t.rList = $("<ul/>").addClass("q-list q-meta-list");
	t.pList = $("<ul/>").addClass("q-list q-prov-list");
		 
	// metadata containing element
	var container = $("<div/>").addClass("q-container")
							   .css( {"position":"absolute",
							   		  "top":"0",
							   		  "left":"570px",
							   		  "right":"0",
							   		  "min-width": "320px" } );
	
	var switchFunc = function(e){ $(e.target).tab('show'); };
	
	var nav = $("<ul/>").addClass("nav nav-tabs")
						.append($("<li class='active'><a href='#anno"+t.metaId+"'>Comments</a></li>").click(switchFunc))
						.append($("<li><a href='#ref"+t.metaId+"'>References</a></li>").click(switchFunc))
						.append($("<li><a href='#prov"+t.metaId+"'>Provenance</a></li>").click(switchFunc));
	
	// initialize tab containers
	container.append(nav)
			 .append(content.append(t.annotations.append(t.aList))
			 				.append(t.references.append(t.rList))
			 				.append(t.provenance.append(t.pList))
			 		);
	
	t.container = container;
	t.annotations.append($("<div/>").html("Select a structure to view its comments")
						.addClass("q-hint"));
	t.references.append($("<div/>").html("Select a structure to view its references")
						.addClass("q-hint"));
	t.provenance.append($("<div/>").html("Select a structure to view its provenance")
						.addClass("q-hint"));
};

MetaDisplay.prototype = {

	commentButtons: function(){
		var t = this;
		var add = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon-comment'/> New Comment</button>")
			.click(function(e){

				// use popover function found in utils.js
				var elmt = $(this);
				var comment = $("<textarea placeholder='Comment'></textarea>").addClass("form-control").css("margin-bottom","10px");
				var funcs = { "Create": function(){
								t.addAnnotation(t.selected, comment.val());
							  },
							  "Cancel": function(){}
							};
				popover(elmt, "New Comment", "bottom", $("<div/>").append(comment), funcs);
				e.stopPropagation();
		});
		
		var container = $("<div/>")
			.addClass("q-button-container");
		container.append(add);
			
		return container;
	},
	
	refButtons: function(){
		var t = this;
		var add = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon-book'/> New Reference</button>")
			.click(function(e){

				// use popover function found in utils.js
				var elmt = $(this);
				
				var rendering = $("<div/>").addClass("q-ref-render");
				var id = $("<input type='text' placeholder='Source ID'/>").addClass("form-control");
				var sources = $("<select/>").addClass("form-control");
				
				var renderFunc = function(){
					var opt = $("option:selected", sources);
					var uri = opt.data("uri");
					var val = id.val();
					rendering.html("<a href='"+uri.replace("<ID>", val)+"' target='_blank'>"+opt.text()+" - "+val+"</a>");
				};
				
				t.app.structService.sources(function(resp){
					if(resp["objs"]){
						var objs = resp["objs"];
						$.each(objs, function(index, val){
							var opt = $("<option>"+val.name+"</option>").val(val.id);
							opt.data("uri", val.uri);
							sources.append(opt);
						});
					}
				});
								
				var funcs = { "Create": function(){
								t.addReference(t.selected, sources.val(), id.val());
							  },
							  "Cancel": function(){}
							};
							
				var content = $("<div/>").append($("<div/>").append(sources).append("<br/>").append(id)).append(rendering);
				popover(elmt, "New Reference", "bottom", content, funcs);
				
				var timeout = false;
				sources.change(renderFunc);
				id.keyup(function(e){
						if(timeout) clearTimeout(timeout);
						if(id.val() != ""){								
							timeout = setTimeout(renderFunc, 1000);
						}else rendering.empty();
					});
				e.stopPropagation();
		});
		
		var container = $("<div/>")
			.addClass("q-button-container");
		container.append(add);
			
		return container;
	},
	
	addAnnotation: function(obj, comment){
		var t = this;
		var after = function(resp){
			if(resp["error"]) t.app.message(resp);
			t.getAnnotations(t.selected.id, 0, -1);
		};
		t.service.addAnnotation(obj.id, comment, after);
	},
	
	addReference: function(obj, source, refId){
		var t = this;
		var after = function(resp){
			if(resp["error"]) t.app.message(resp);
			t.getReferences(t.selected.id, 0, -1);
		};
		var id = obj.file? obj.file: obj.id;
		t.service.addReference(id, source, refId, after);
	},

	getMeta: function(obj){
		var t = this;
		t.selected = obj;
		t.clear();
		t.getAnnotations(obj.id, 0, -1);
		t.getReferences(obj.id, 0, -1);
		t.getProvenance(obj.id, 0, -1);
	},

	getAnnotations: function(obj, offset, limit){
		var t = this;
		t.app.loading();
		t.annotations.find(".q-hint").remove();
		var callback = function(resp){
			t.aList.empty();
			
			if(!t.readOnly){
				if(t.annotations.find(".q-button-container").length == 0) t.annotations.prepend(t.commentButtons());
			}else t.aList.css("top", "0px");
			
			var annotations = resp["objs"];
			if(annotations && annotations.length > 0){
				t.parseAnnotations(annotations);
				t.aOffset += t.annotations.length;
			}else if(resp["error"]){
				t.app.message(resp);
			}else{
				
				var hint = $("<div/>").html("No comments yet.")
						.addClass("q-hint");
				t.annotations.append(hint);
			}
			t.app.loaded();
		};
		t.service.annotations(obj, offset, limit, callback);
	},
	
	parseAnnotations: function(annotations){
		var t = this;
		for(var i=0; i<annotations.length; i++){
			var annotation = new Annotation(annotations[i].id, annotations[i]);
			t.aList.append(annotation.avatar);
		}	
	},
	
	getReferences: function(obj, offset, limit){
		var t = this;
		t.app.loading();
		t.references.find(".q-hint").remove();
		var callback = function(resp){
			t.rList.empty();
			
			if(!t.readOnly){
				if(t.references.find(".q-button-container").length == 0) t.references.prepend(t.refButtons());
			}else t.rList.css("top", "0px");
			
			var references = resp["objs"];
			if(references && references.length > 0){
				t.parseReferences(references);
				t.rOffset = t.references.length;
			}else if(resp["error"]){
				t.app.message(resp);
			}else{
				var hint = $("<div/>").html("No references yet.")
						.addClass("q-hint");
				t.references.append(hint);
			}
			t.app.loaded();
		};
		t.service.references(obj, offset, limit, callback);
	},
	
	parseReferences: function(references){
		var t = this;
		for(var i=0; i<references.length; i++){
			var reference = new Reference(references[i].id, references[i]);
			t.rList.append(reference.avatar);
		}	
	},
	
	getProvenance: function(obj, offset, limit){
		var t = this;
		t.app.loading();
		t.provenance.find(".q-hint").remove();
		var callback = function(resp){
			var provenance = resp["objs"];
			if(provenance && provenance.length > 0){
				t.pList.empty();
				t.parseProvenance(provenance);
				t.pOffset = t.provenance.length;
			}else if(resp["error"]) t.app.message(resp);
			t.app.loaded();
		};
		t.service.provenance(obj, offset, limit, callback);
	},
	
	parseProvenance: function(provenance){
		var t = this;
		for(var i=0; i<provenance.length; i++){
			var prov = new Provenance(provenance[i].id, provenance[i]);
			t.pList.append(prov.avatar);
		}	
	},
	
	clear: function(){
		this.aList.empty();
		this.rList.empty();
		this.pList.empty();
	}
	
};