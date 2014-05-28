var DashPane = function(app){
	var t = this;
	t.app = app;
	t.started = false;
	
	var select = $("<select/>").addClass("styled-select")
								.append($("<option selected>Uploaded Structures</option>"))
								.append($("<option>Reviewed Structures</option>"));
	
	select.change(function(){
					var option = select.find("option:selected").val();
					t.switchSearch(option); 
				})
	
	t.structFilter = new StructureFilter(app);
	t.structFilter.filter["owned"] = true;
		
	t.objDisplay = new StructureDisplay(t, t.structFilter);
		
	t.header = $("<div/>").append($("<h2/>").html("My ").append(select));
	t.container = $("<div/>").addClass("q-pane-container");
	
	t.container.append(t.structDisplay.container);
}

DashPane.prototype = {

	init: function(){
		if(!this.started){
			this.objDisplay.search(10);
			this.started = true;
		}
	},
	
	switchSearch: function(option){
		var t = this;
		t.objDisplay.container.hide();
		if(option.indexOf("Matched") != -1){
			t.objDisplay = t.structDisplay;
			if(!t.structDisplay.structs.length > 0) t.objDisplay.search(10);
		}else if(option.indexOf("Submitted") != -1){
			t.objDisplay = t.fileDisplay;
			if(!t.fileDisplay.files.length > 0) t.objDisplay.search(10);
		}
		t.objDisplay.container.show();
	}
};