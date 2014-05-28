var Structure = function(id, attrbs, tree){
	Entity.call(this, id, attrbs);
	if(this.spec && !this.spec["id"]){ // if the spec is a string, we have to parse it first
		this.spec = this.spec.replace(/"null"/g, "null");
		this.spec = jQuery.parseJSON(this.spec);
	}
	this.createAvatar(tree);
};

Structure.prototype = new Entity();

Structure.prototype.createAvatar = function(tree){
	var t = this;
	var avatar = $("<div/>").addClass("struct");
	t.struct = avatar;
	//var avatar = $("<div/>").addClass("struct-container").append(t.struct);
	
	var infobox = $("<div/>").addClass("q-struct-info");
	var attrbs = $("<dl/>").addClass("dl-horizontal")
						.append($("<dt>Added By:</dt>")).append($("<dd/>").html(t.createdBy))
						.append($("<dt>Added On:</dt>")).append($("<dd/>").html(t.createdOn))
						.append($("<dt>Type:</dt>")).append($("<dd/>").append($("<span/>")
																				.addClass("struct-tree")
																				.html(t.type)));
	
	infobox.append(attrbs);
	if(t.uri && t.uri != "null"){
		infobox.append($("<div/>").addClass("struct-uri")
								  .html(t.uri.replace("http://glycomics.ccrc.uga.edu/ontologies/GlycO#","")));
	}
	
	avatar.append(infobox);
	
	if(t.status && t.status != "null"){
		var menu = $("<div/>").addClass("q-status")
							  .append($("<a href='#' data-toggle='dropdown'>"+t.status+"</a>")
										.addClass("badge q-"+t.status)
									  );	
		avatar.append(menu);
	}
	
	avatar.append($("<a href='service/structure/download?id="+t.id+"' title='download' class='struct-link struct-download'/>")
					.append("<span class='glyphicon glyphicon-download-alt'/>")
					.click(function(e){ e.stopPropagation(); }));
	
	t.setAvatar(avatar);
	t.renderer = new StructRenderer({"struct":t, "isTree":tree});
};

Structure.prototype.render = function(){
	this.renderer.spec = this.spec;
	this.renderer.build();
	this.renderer.render();
};