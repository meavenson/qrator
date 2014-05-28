var AdminPane = function(app){
	var t = this;
	t.app = app;
	t.started = false;
	
	t.objDisplay = new UserDisplay(app);
	
	t.header = $("<div/>").append($("<h2/>").html("Administration"));
	t.container = $("<div/>").addClass("q-pane-container");
	
	t.container.append(t.objDisplay.container);
}

AdminPane.prototype = {
	
	init: function(){
		var t = this;
		$("#welcome").hide();
		t.app.hidePanes();
		t.container.show();
		t.header.show();
		//if(!t.started){
			t.objDisplay.search();
		//	t.started = true;
		//}
	}

};