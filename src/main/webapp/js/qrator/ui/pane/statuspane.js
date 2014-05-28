var StatusPane = function(app){
	var t = this;
	t.app = app;
	t.started = false;
	t.structService = app.structService;
	
	t.header = $("<div/>").append($("<h2/>").html("Status"));
	t.stats = $("<div/>").addClass("status-container");
	//t.statContainer = $("<div/>").addClass("status-body");
	//t.structStatContainer = $("<div/>").addClass("status-body");
	t.statuses = {"pending" : "warning", 
				  "reviewed": "info", 
				  "deferred": "active", 
				  "rejected": "danger", 
				  "approved": "success",
				  "committed": ""};
	t.container = $("<div/>").addClass("q-pane-container")
							 .append(t.stats);
};

StatusPane.prototype = {
	
	init: function(){
		var t = this;
		$("#welcome").hide();
		t.app.hidePanes();
		t.container.show();
		t.header.show();
		t.app.loading();
		t.stats.empty();
		t.structService.count(function(resp){
								t.buildStats(resp);
								t.app.loaded();
							});
	},
	
	openSearch: function(filter){
		var t = this;
		var browsePane = t.app.panes["browse"];
		browsePane.started = true;
		browsePane.objDisplay.page = 0;
		browsePane.objDisplay.filter.reset();
		browsePane.objDisplay.filter.setValues(filter,
											  function(){
												browsePane.objDisplay.getStructures();
												browsePane.menuOption.click();
											  });
	},
	
	searchLink: function(options){
		var t = this;
		return function(){ t.openSearch(options); };
	},
	
	treeLink: function(tree){
		var t = this;
		return function(e){ new ShowTree(t.app, {"tree":tree}); };
	},
	
	buildStats: function(resp){
		var t = this;
		var width = "9%";
		if(resp["count"]){
			
			var categoryTotals = {};
			var row = $("<tr/>");
			var header = $("<div/>").addClass("status-header")
						.append($("<table/>").addClass("table table-condensed")
							.append( $("<thead/>")
								.append( row )
							)
						);
			row.append( $("<th/>").html("Canonical Tree").css("width","28%") )
			row.append( $("<th/>").addClass("status-column").html("total") )
			
			// initialize rows and initialize totals
			for(var status in t.statuses){
				row.append( $("<th/>").addClass("status-column").html(status) );
				categoryTotals[status] = 0;
			}
						
			var container = $("<div/>").addClass("status-table");
			var table = $("<table/>").addClass("table table-condensed");
			container.append(table);
			var tbody = $("<tbody/>");
			table.append(tbody);
			
			//container.append(table);
			var count = resp["count"];
			for(var tree in count){
			
				var open = $("<span/>").css("margin-right","5px")
									.addClass("caret-e")
									.click(function(e){
										var icon = $(this);
										icon.toggleClass("caret-e");
										icon.toggleClass("caret-s");
										var row = icon.parent().parent();
										if(icon.hasClass("caret-s")){
											row.nextUntil(":not(.sub)").show();
											row.children().next().hide();
										}else{
											row.nextUntil(":not(.sub)").hide();
											row.children().next().show();
										}
										e.stopPropagation();
									});
			
				var treeRow = $("<tr/>").addClass("status-row")
										.append( $("<td/>").append( tree=="Unknown"? 
																		tree : 
																		$("<a href='#'>"+tree.replace(/_/g," ")+"</a>")
																			.click(t.treeLink(tree))
															).css("width","28%") );
				tbody.append(treeRow);
				
				var types = count[tree];
				var treeId = types["id"]+"";
				
				var typeNum = 0;
				for(var type in types) typeNum++;
				// if there is more than one subtype, make a collapsible list of the subtypes
				if(typeNum > 2){ treeRow.children().first().prepend(open); }
				treeRow.children().first().css("padding-left","24px");
				
				var typeTotals = {};
				for(var status in t.statuses){
					typeTotals[status] = 0;
				}
				
				for(var type in types){
					if(type == "id") continue;
					var counts = types[type];
					
					var typeId = counts["id"];
					for(var status in t.statuses){
						typeTotals[status] += counts[status];
						categoryTotals[status] += counts[status];
					}
					
					if(typeNum > 2){
						var total = 0;
						for(var status in t.statuses){
							total += counts[status];
						}
						var subrow = $("<tr/>").addClass("sub").append( $("<td/>").html(type).css("padding-left","35px") )
											   .append( $("<td/>").append($("<a href='#'>"+total+"</a>")
																			.click(t.searchLink({"tree": treeId, "type": typeId }))
																		 )
																.append($("<a href='service/structure/downloadAll?tree="+treeId+"&type="+typeId+"' title='download'/>")
																		  .append("<span class='glyphicon glyphicon-download-alt'/>"))
																.css("width",width)
													  );
						for(var status in t.statuses){
							subrow.append( $("<td/>").append($("<a href='#'>"+counts[status]+"</a>")
																.click(t.searchLink({"status":status, "tree": treeId, "type": typeId }))
															 )
												     .append($("<a href='service/structure/downloadAll?status="+status+"&tree="+treeId+"&type="+typeId+"' title='download'/>")
																.append("<span class='glyphicon glyphicon-download-alt'/>"))
													 .css("width",width).addClass(t.statuses[status]) );
						}
						tbody.append(subrow.hide());
					}
					
				}
				
				var total = 0;
				for(var status in t.statuses){
					total += typeTotals[status];
				}
				
				treeRow.append( $("<td/>").append( $("<a href='#'>"+total+"</a>")
													.click(t.searchLink({"tree": treeId})) 
												)
										  .append($("<a href='service/structure/downloadAll?tree="+treeId+"' title='download'/>")
																.append("<span class='glyphicon glyphicon-download-alt'/>"))
									  	  .css("width",width)
							  );
				
				for(var status in t.statuses){
					treeRow.append( $("<td/>").addClass(t.statuses[status])
											  .append( $("<a href='#'>"+typeTotals[status]+"</a>")
														.click(t.searchLink({"status":status, "tree": treeId }))
											  		)
											  .append($("<a href='service/structure/downloadAll?status="+status+"&tree="+treeId+"' title='download'/>")
																.append("<span class='glyphicon glyphicon-download-alt'/>"))
											 .css("width",width)
								  );
				}
			}
			
			var total = 0;
			for(var status in t.statuses){
				total += categoryTotals[status];
			}
			
			var totalRow = $("<tr/>").addClass("status-row");
			tbody.append(totalRow.append($("<td/>"))
								 .append( $("<td/>").append($("<a href='#'>"+total+"</a>")
															.click( t.searchLink({}) ) 
														 )
													.append($("<a href='service/structure/downloadAll' title='download'/>")
																.append("<span class='glyphicon glyphicon-download-alt'/>"))
													.css("width",width) 
										)
						);
			for(var status in t.statuses){							  
			   totalRow.append( $("<td/>").append( $("<a href='#'>"+categoryTotals[status]+"</a>")
													.click( t.searchLink( {"status":status} ) ) 
												)
										  .append($("<a href='service/structure/downloadAll?status="+status+"' title='download'/>")
														.append("<span class='glyphicon glyphicon-download-alt'/>"))
										  .css("width",width)
							  );
			}
			
			t.stats.append(header).append(container);
		}else t.app.message(resp);
	}
	
};