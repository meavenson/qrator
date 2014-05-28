var Provenance = function(id, attrbs){
	Entity.call(this, id, attrbs);
	this.createAvatar();
};

Provenance.prototype = new Entity();

Provenance.prototype.createAvatar = function(){
	var t = this;
	var avatar = $("<li/>").addClass("q-meta q-provenance");
	var color = null;
	switch(t.action){
		case "toPending"  : color = "bg-warning"; break;
		case "toReviewed" : color = "bg-info"; break;
		case "toDeferred" : color = "bg-active"; break;
		case "toApproved" : color = "bg-success"; break;
		case "toRejected" : color = "bg-danger"; break;
		default           : color = "bg-primary";
	}
	var action = t.action.replace("to","")
					 .replace("Pending","added this structure")
					 .replace("Deferred","deferred this structure")
					 .replace("Reviewed","reviewed this structure")
					 .replace("Approved","approved this structure")
					 .replace("Rejected", "rejected this structure")
					 .replace("Ontology", "committed this structure");
	
	var container = $("<div/>").addClass("q-provenance-field "+color)
							   .html(t.createdBy + " " + action + " on "+t.createdOn);
	avatar.append(container);
	this.setAvatar(avatar);
};