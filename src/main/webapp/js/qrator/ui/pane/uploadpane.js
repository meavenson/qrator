var UploadPane = function(app){
	this.app = app;
};

UploadPane.prototype = {

	init: function(){
		var t = this;
		t.modal = t.modal? t.modal : modal($("<div/>"), "Upload Structures", {}, 700);
		t.upload();
	},
	
	checkCompletion: function(spec){
		var t = this;
		if( !spec.type || spec.type == "." ) return false;
		if( !spec.abconf || spec.abconf == "." ) return false;
		if( !spec.anomer || spec.anomer == "." ) return false;
		if( spec.link == "." ) return false;
		if(spec["children"]){
			for(var i=0; i<spec["children"].length; i++){
				if(!t.checkCompletion(spec["children"][i]))
					return false;
			}
		}
		return true;
	},
	
	upload: function(){
		var t = this;
		t.modal.container.modal('show');
		var form = $("<form method=\"POST\" enctype=\"multipart/form-data\""+
						"action=\""+t.app.structService.address+"/upload\" target=\"uTarget\"/>");
		
		var file = $("<input type=\"file\" name=\"file\"/>").css("margin-top","10px").addClass("form-control");
		var ssid = $("<input type=\"hidden\" name=\"ssid\" value=\""+t.app.uid+"\"/>");
		
		var frame = $("<iframe name=\"uTarget\" id=\"uTarget\"/>").css("display","none");
		frame.load( function(e){
						var resp = frame.contents().text();
						if(resp){
							response = $.parseJSON(resp);
							t.app.message(response);
							t.results(response["report"]);
							frame.remove();
						}
					});
		
		form.append(ssid).append(file);
		
		var formats = {
			"GLYDE-II" : ".xml",
			"GlycoWorkbench" : ".gws",
			"Archive" : ".zip, .tar.gz, .tgz"
		};
		var table = $("<table/>").addClass("table table-condensed")
						.append( $("<thead/>")
							.append( $("<tr/>")
								.append( $("<th/>").css("width","40%").html("Supported Type") )
								.append( $("<th/>").html("Upload Format") )
							)
						);
		var tbody = $("<tbody/>");
		table.append(tbody);
		for(var id in formats){
	        var row = $("<tr/>").addClass("upload-row");
	        row.append($("<td>" + id + "</td>").css( {"background-color": "#cfc", "width":"40%"} ));
			row.append($("<td>" + formats[id] + "</td>").css("background-color", "#eee"));
			tbody.append(row);
	    }
		
		/*var prompt = $("<div/>").html("<strong>Upload a single structure in GLYDE-II XML, GlycoWorkbench GWS format, or an archive (must not contain subdirectories).<br/>"+
											"Currently supported:</strong> .xml, .gws, .zip, or .tar.gz (.tgz)");*/
		var builder = $("<button>Structure Builder</button>")
							.addClass("btn btn-default").css("margin-top","10px")
							.click(function(e) {
								var b;
								var callback = function(c, motif){
									if(t.checkCompletion(motif)){
										var after = function(resp){
											if(resp["error"]) c.stopPropagation();
											t.app.message(resp);
										};
										t.app.structService.build(motif, after);
									}else{
										b.addHighlights();
										t.app.message({"error":"Missing information detected in highlighted residues."});
										c.stopPropagation();
									}
								};
								var options = {"modal":t.modal,
											   "notifier":t.app.notifier, 
										       "callback":callback, 
										       "submitLabel":"Submit"};
								b = new StructBuilder(options);
							  });
		var container = $("<div/>").append(table)
								   .append(form)
								   .append($("<div/>").append($("<div>Or build a structure: </div>").css("margin-top","20px"))
								   					  .append(builder)
								   		  )
								   .append(frame);

		var funcs = {
						"Upload": function(e) {
							form.submit();
							$(e.target).html("<img src='img/loading-16.gif'/>");
							$(e.target).unbind("click");
							e.stopPropagation();
						},
						"Cancel": function(){}
					};
	
		t.modal.setTitle("Upload Structures");
		t.modal["body"].empty().append(container);
		t.modal["footer"].empty();
		t.modal.generateButtons(funcs);
	},
	
	results: function(report){
		var t = this;		
		var statuses = $("<div/>").addClass("upload-container");
		var header = $("<div/>").addClass("upload-header")
					.append($("<table/>").addClass("table table-condensed")
						.append( $("<thead/>")
							.append( $("<tr/>")
								.append( $("<th/>").css("width","40%").html("Name") )
								.append( $("<th/>").html("Message") )
							)
						)
					);
		var container = $("<div/>").addClass("upload-table");
		var table = $("<table/>").addClass("table table-condensed");
		container.append(table);
		var tbody = $("<tbody/>");
		table.append(tbody);
		statuses.append(header).append(container);
		
        var status = report["status"];
        for(var i=0; i<status.length; i++) {
        	var stat = status[i];
        	var row = $("<tr/>").addClass("upload-row");
			row.append($("<td>" + stat.name + "</td>").css( {"background-color":(stat.message?"#cfc":"#fcc"), "width":"40%"} ));
			row.append($("<td>" + (stat.message? stat.message : stat.error? stat.error : "Unknown error occurred") + "</td>").css("background-color", "#eee"));
			tbody.append(row);
        }
        
		var funcs = {
						"Upload More": function(e) {
							t.upload();
							e.stopPropagation();
						},
						"Done": function(){}
					};
	
		t.modal.setTitle("Upload Results");
		t.modal["body"].empty().append(statuses);
		t.modal["footer"].empty();
		t.modal.generateButtons(funcs);
	}
	
};