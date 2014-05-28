var Type = function(id, attrbs){
	Entity.call(this, id, attrbs);
	this.createAvatar();
};

Type.prototype = new Entity();

// TODO - implement this
Type.prototype.createAvatar = function(){
};