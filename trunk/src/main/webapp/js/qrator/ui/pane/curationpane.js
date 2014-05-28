var CurationPane = function(app){
	var t = this;
	t.app = app;
	t.started = false;
	
	t.objDisplay = new StructureDisplay(t, { "enhancer": function(struct){ t.initCurationInterface(struct); },
											 "curation": true });
		
	t.header = $("<div/>").append($("<h2/>").html("Curate Structures"));
	t.container = $("<div/>").addClass("q-pane-container");
		
	t.container.append(t.objDisplay.container);
};

CurationPane.prototype = {
	
	init: function(){
		var t = this;
		$("#welcome").hide();
		t.app.hidePanes();
		t.container.show();
		t.header.show();
		if(!t.started){
			t.objDisplay.filterStructures();
			t.started = true;
		}
	},
	
	initCurationInterface: function(struct){
		var t = this;
		
		if(struct.status != "approved" && struct.status != "committed"){
			var menu = struct.avatar.find(".q-status");
			menu.addClass("dropdown q-status-menu");
			var list = $("<ul role='menu' aria-labelledby='dLabel'/>").addClass("dropdown-menu");
			menu.find(".badge").append("<span class='caret'/>");
			
			if(struct.status == "pending"){
				t.initReviewButton(list, struct);
				t.initDeferButton(list, struct);
				t.initRejectButton(list, struct);
			}else if(struct.status == "reviewed"){
				t.initApproveButton(list, struct);
				t.initRejectButton(list, struct);
			}else if(struct.status == "deferred"){
				t.initReviewButton(list, struct);
				t.initRejectButton(list, struct);
			}else if(struct.status == "rejected"){
				t.initReviewButton(list, struct);
				t.initDeferButton(list, struct);
			}
		
			menu.append(list);
		}
		
		if(struct.status == "pending"){
			var changeType = $("<a href='#' class='glyphicon glyphicon-cog'/>");
			var action = function(e){
				var after = function(resp){
					if(!resp["error"]){
						var obj = resp["obj"];
						struct.tree = obj.tree;
						struct.type = obj.type;
						struct.treeId = obj.treeId;
						struct.typeId = obj.typeId;
					
						var structTree = struct.avatar.find(".struct-tree");
					
						var newTree = $("<span/>").addClass("struct-tree")
												.html(struct.type)
					
						structTree.replaceWith(newTree);
					
						if(struct.tree != "Unknown"){
							newTree.empty()
									 .append($("<a href='#'>"+struct.type+"</a>")
									 .click(t.objDisplay.treeLink(struct.tree)));
						}
						newTree.append(" ")
								.append(changeType.click(action))
					
						t.app.message(resp);
					}else t.app.message(resp);
				};
			
				var assignType = new AssignType(t.app);
				assignType.container.css("padding","0");
				var funcs = { 	"Change Type": function(){
									assignType.refresh();
									t.app.structService.type(struct.id, assignType.filter["type"], after);
								},
								"Cancel": function(){ } };
			
				var clone = struct.avatar.clone().css( {"height":"300px", "background-color":"#fff"} );
				clone.find(".q-status").remove();
				clone.find(".struct-link").remove();
			
				var content = $("<div/>").append(clone).append(assignType.container);
				var dialog = modal(content, "Assign Type", funcs, 500);
				dialog.container.modal().on('shown.bs.modal', function(){
					assignType.enable();
					assignType.setValues({"tree": struct.treeId, "type": struct.typeId });
				});
				e.stopPropagation();
			};
			changeType.click(action);
		
			struct.avatar.find(".q-struct-info").find(".struct-tree").append(" ").append(changeType);
		}
		
	},
	
	initReviewButton: function(list, struct){
		var t = this;
		var reviewed = $("<li/>").html("<a href='#'><span class='glyphicon glyphicon-play q-status-icon'/>Match</a>")
								.click(function(e){
									var after = function(resp){
										if(resp["error"]) t.app.message(resp);
										else new StructReview(t.app, {"struct":struct, "resp":resp});
									};
									t.app.matchService.list(struct.id, after);
								});
		list.append(reviewed);
	},
	
	initDeferButton: function(list, struct){
		var t = this;
		var action = function(e){
			var after = function(resp){
				if(!resp["error"]){
					t.app.message(resp);
					struct.avatar.parent().hide(function(){ struct.avatar.parent().remove() });	
					//t.app.fileService.checkin(file.id);
				}else t.app.message(resp);
			};
			
			var elmt = $(this);
			var comment = $("<textarea placeholder='Comment'></textarea>").addClass("form-control");
			var prompt = $("<div/>").html("This structure will be <span style=\"font-weight:bold;color:#999;\">deferred</span>."+
										  "<br/>Do you have comments about this structure?");
			var funcs = { 	"Done": function(){ 
								t.app.structService.defer(struct.id, comment.val(), after);
							},
							"Cancel": function(){ } };
			
			var clone = struct.avatar.clone().css( {"height":"300px", "background-color":"#fff"} );
			clone.find(".q-status").remove();
			clone.find(".struct-link").remove();
			
			var content = $("<div/>").append(clone).append(prompt).append(comment);
			modal(content, "Comment", funcs, 600);
			e.stopPropagation();
		};
		var deferred = $("<li/>").html("<a href='#'><span class='glyphicon glyphicon-pause q-status-icon'/>Defer</a>")
								.click(action);
		list.append(deferred);
	},
	
	initApproveButton: function(list, struct){
		var t = this;
		var action = function(e){
			var after = function(resp){
				if(!resp["error"]){
					t.app.message(resp);
					struct.avatar.parent().hide(function(){ struct.avatar.parent().remove() });	
					//t.app.fileService.checkin(file.id);
				}else t.app.message(resp);
			};
			
			var elmt = $(this);
			var comment = $("<textarea placeholder='Comment'></textarea>").addClass("form-control");
			var prompt = $("<div/>").html("This structure will be <span style=\"font-weight:bold;color:#090;\">approved</span>."+
										  "<br/>Do you have comments about this structure?");
			var funcs = { 	"Done": function(){ 
								t.app.structService.approve(struct.id, comment.val(), after);
							},
							"Cancel": function(){ } };
			
			var clone = struct.avatar.clone().css( {"height":"300px", "background-color":"#fff"} );
			clone.find(".q-status").remove();
			clone.find(".struct-link").remove();
			
			var content = $("<div/>").append(clone).append(prompt).append(comment);
			modal(content, "Comment", funcs, 600);
			e.stopPropagation();
		};
		var approved = $("<li/>").html("<a href='#'><span class='glyphicon glyphicon-ok q-status-icon'/>Approve</a>")
								.click(action);
		list.append(approved);
	},
	
	initRejectButton: function(list, struct){
		var t = this;
		var action = function(e){
			var after = function(resp){
				if(!resp["error"]){
					t.app.message(resp);
					struct.avatar.parent().hide(function(){ struct.avatar.parent().remove() });	
					//t.app.fileService.checkin(file.id);
				}else t.app.message(resp);
			};
			
			var elmt = $(this);
			var comment = $("<textarea placeholder='Comment'></textarea>").addClass("form-control");
			var prompt = $("<div/>").html("This structure will be <span style=\"font-weight:bold;color:#f00;\">rejected</span>."+
										  "<br/>Do you have comments about this structure?");
			var funcs = { 	"Done": function(){ 
								t.app.structService.reject(struct.id, comment.val(), after);
							},
							"Cancel": function(){ } };
			
			var clone = struct.avatar.clone().css( {"height":"300px", "background-color":"#fff"} );
			clone.find(".q-status").remove();
			clone.find(".struct-link").remove();
			
			var content = $("<div/>").append(clone).append(prompt).append(comment);
			modal(content, "Comment", funcs, 600);
			e.stopPropagation();
		};
		var rejected = $("<li/>").html("<a href='#'><span class='glyphicon glyphicon-stop q-status-icon'/>Reject</a>")
								.click(action);
		list.append(rejected);
	}
	
};