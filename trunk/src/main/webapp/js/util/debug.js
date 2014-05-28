var Terminal = function(){
	var t = this;
	t.container = $("<div id='terminal'/>");
	
	t.container.dialog({ title:"Terminal",
					width:450,
					height:485,
					open:function() {
						$(this).parents(".ui-dialog:first").find(".ui-dialog-titlebar-close").remove();
					}
				});

};

Terminal.prototype = {

	println: function( str ){
		var line = $("<p>"+str+"</p>").css("padding","1").css("margin","1");
		$("#terminal").append(line);
	}
	
};