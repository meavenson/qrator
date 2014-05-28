var StructReview = function(app, options){
	Wizard.call(this, app);
	var t = this;
	t.service = app.structService;
	t.obj = options.struct;
	t.display = options.display;
	t.modal = modal($("<div/>"), "", {}, 1000);
	t.step1();
};

StructReview.prototype = new Wizard();

StructReview.prototype.addStructure = function(struct, comment){
	var t = this;
	var after = function(resp){
		if(!resp["error"]){
			t.obj.avatar.parent().hide(function(){ t.obj.avatar.parent().remove() });
		}
		t.app.message(resp);
	};
	t.service.review(struct.spec, t.obj.id, struct.typeId, comment, after);
};

StructReview.prototype.step1 = function(){
	var t = this;

	var container = $("<div/>").css({"height":"400px","position":"relative"});
	var comparison = $("<div/>").css({"position":"relative", "margin-top":"20px", "height":"300px"}).hide();

	//var avatar = t.obj.avatar;
	var matches = $("<ul/>").addClass("q-list").css({"position":"absolute","top":"0px","bottom":"0px","left":"50%", "width":"50%"});
	var prompt = $("<div/>").html("<strong>Below are the best possible matches to the canonical tree to which this glycan belongs.</br>"+
								  "Please select the best match by clicking it.  Striped matches already exist and are not selectable.<br/></strong>"+
								  "<span style='color:#0bb'>Blue</span> circles indicate that a residue will be added to the canonical tree.<br/>"+
								  "<span style='color:#f00'>Red</span> circles indicate corrections to fit the existing canonical tree.").hide();
	var placeholder = $("<div/>").addClass("q-placeholder").html("<div class='q-small-loading'/>Please wait.  Finding best matches...");
	
	comparison.append(matches);
	container.append(placeholder).append(prompt).append(comparison);
	
	var after =  function(resp){
		placeholder.remove();
		prompt.show();
		comparison.show();
		
		var avatar = $("<div/>").css({"border-right":"2px solid #ccc", "width": "480px"}).addClass("q-container");
		var clone = t.obj.avatar.clone().css("background-color","#fff");
		var status = clone.find(".q-status");
		status.parent().find("ul").remove();
		status.find(".caret").remove();
		clone.children(".struct-download").remove();
		
		avatar.append(clone);
		comparison.prepend(avatar);
		
		var structArr = [];
		if(resp["objs"]){
			var structs = resp["objs"];
			var len = structs.length > 20? 20: structs.length;
			for(var i=0; i<len; i++){
				var struct = new Structure(structs[i].id, structs[i]);
				struct.avatar.children(".q-struct-info").remove();
				struct.avatar.children(".struct-download").remove();
				if(struct.exists){
					struct.avatar.addClass("struct-exists");
				}else{
					struct.avatar.click( function(e){
										$(this).parent().parent().find(".ui-selected").removeClass("ui-selected");
										$(this).addClass("ui-selected");
									});
				}
				
				var row = $("<li/>").addClass("q-struct").append(struct.avatar);				
				struct.avatar.append($("<span/>").addClass("badge score").html(struct.score));
				matches.append(row);
				structArr.push(struct);
			}
		}else{
			t.app.message(resp);
		}
		if(!t.modal.container.is(":visible")){
			t.modal.container.modal().on('shown.bs.modal', function(){
				for(var i=0; i<structArr.length; i++){
					structArr[i].render();
				}
			});
		}else{
			for(var i=0; i<structArr.length; i++){
				structArr[i].render();
			}
		}
	};
	
	t.app.matchService.list(t.obj.id, after);
	
	var funcs = { 	"Ok": function(e){
							var struct = $(".ui-selected", matches).data("obj");
							if(!struct){
								t.app.notifier.error("No match selected.");
								e.stopPropagation();
							}else{
								t.step2(struct);
							}
						},
					"Cancel": function(){} };
	
	t.modal["header"].empty().append($("<h3/>").html("Matching Configurations"));
	t.modal["body"].empty().append(container);
	t.modal["footer"].empty();
	t.modal.generateButtons(funcs);
};

StructReview.prototype.step2 = function(struct){
	var t = this;

	var container = $("<div/>");
	var comment = $("<textarea placeholder='Comment'></textarea>").addClass("form-control");
	var prompt = $("<div/>").html("Do you have comments about this structure?");
	
	container.append(prompt).append(comment);
	
	var funcs = { 	"Ok": function(){
							t.addStructure(struct, comment.val());
						},
					"Cancel": function(){} };
	
	t.modal["header"].empty().append($("<h3/>").html("Comment"));
	t.modal["body"].empty().append(container);
	t.modal["footer"].empty();
	t.modal.generateButtons(funcs);
	t.modal.setWidth(400);
};