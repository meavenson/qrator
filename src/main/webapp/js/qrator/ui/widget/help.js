var Help = function(){
	var t = this;
	t.modal = modal($("<div/>"), "", {}, 860, true);
	t.modal["body"].append($("#help").show());
	t.modal.setTitle("Qrator: A Curation System for Glycan Structures");
	t.show();
};

Help.prototype = {

	show: function(){
		var t = this;
		funcs = { "Ok": function(e){} };
		t.modal["footer"].empty();
		t.modal.generateButtons(funcs);
		t.modal.container.modal("show");
	}
};
