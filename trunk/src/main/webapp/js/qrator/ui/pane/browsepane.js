var BrowsePane = function(app){
	var t = this;
	t.app = app;
	t.started = false;
	
	t.objDisplay = new StructureDisplay(t, {"readOnly":true});
	
	t.header = $("<div/>").append($("<h2/>").html("Browse Structures"));
	t.container = $("<div/>").addClass("q-pane-container");
	
	t.container.append(t.objDisplay.container);
}

BrowsePane.prototype = {
	
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
	}

};