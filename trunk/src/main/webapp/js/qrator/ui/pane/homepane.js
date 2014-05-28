var HomePane = function(app){
	var t = this;
	t.app = app;
	
	t.header = $("<div/>").append($("<h2/>").html("Welcome"));
	t.container = $("<div/>").addClass("q-pane-container");
}

HomePane.prototype = {
	
	init: function(){
		var t = this;
		$("#welcome").show();
		t.app.hidePanes();
		t.container.show();
		t.header.show();
	}

};