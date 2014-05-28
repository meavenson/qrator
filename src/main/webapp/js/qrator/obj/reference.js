var Reference = function(id, attrbs){
	Entity.call(this,id,attrbs);
	this.createAvatar();
};

Reference.prototype = new Entity();

Reference.prototype.createAvatar = function(){

	var avatar = $("<li/>").addClass("q-meta q-reference")
						   .html("<a href='"+this.sourceURI.replace("<ID>", this.refId)+"' target='_blank'>"+this.source+" - "+this.refId+"</a>");
	var label = $("<div/>").addClass("q-meta-label");
	var field = $("<div/>").addClass("q-meta-field");
	var createdBy = $("<div/>").html(this.createdBy);
	var createdOn = $("<div/>").html(this.createdOn);
	avatar.append(label.append(field.append(createdBy).append(createdOn)));
	this.setAvatar(avatar);
};