var ShowTree = function(app, options){
	Wizard.call(this, app);
	var t = this;
	t.service = app.treeService;
	t.tree = options.tree;
	t.modal = modal($("<div/>"), "", {}, 800);
	t.step1();
};

ShowTree.prototype = new Wizard();

ShowTree.prototype.step1 = function(){
	var t = this;
	var container = $("<div/>").css({"height":"400px","position":"relative"});
	var treeContainer = $("<div/>").css({"position":"relative", "height":"100%"}).hide();
	var placeholder = $("<div/>").addClass("q-placeholder")
								 .html("<span class='q-small-loading'/>Please wait.  Loading Canonical Tree...");
	
	var after =  function(resp){
		placeholder.remove();
		treeContainer.show();
		
		if(resp["objs"]){
			var struct = resp["objs"];
			var renderer = new StructRenderer({ "isTree":true, "struct":{"id": t.tree, "spec":jQuery.parseJSON(struct)} });
			treeContainer.append(renderer.container);
			var funcs = { "Ok": function(){ renderer.container.remove(); } };
			t.modal.generateButtons(funcs);
			t.modal["footer"].prepend($("<a href=\"service/tree/diagram?name="+t.tree+"\" class=\"btn btn-default\" target=\"_blank\"><span class='glyphicon glyphicon-picture' /> Download SVG</a>")
            				.click(function(e){ e.stopPropagation(); }));
			
			t.modal["footer"].prepend(
						$("<div/>").html("Drag to view more,<br/> or scroll to zoom.")
								   .addClass("q-footer-hint"));
			
			t.modal.container.modal().on('shown.bs.modal', function(){
				renderer.build();
				renderer.render();
			});
			
		}else{
			t.app.message({"error":"Tree is not loaded."});
			var funcs = { "Ok": function(){} };
			t.modal.generateButtons(funcs);
		}
	};
	
	t.service.spec(t.tree, after);
	container.append(placeholder).append(treeContainer);

	t.modal["header"].empty().append($("<h3/>").html(t.tree.replace(/_/g, " ")+" Canonical Tree"));
	t.modal["body"].empty().append(container);
	t.modal["footer"].empty();	
	t.modal.setWidth(800);
};