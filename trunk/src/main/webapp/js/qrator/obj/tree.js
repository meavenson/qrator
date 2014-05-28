var Tree = function(id, attrbs){
	Entity.call(this, id, attrbs);
	this.createAvatar();
};

Tree.prototype = new Entity();

// TODO - implement this
Tree.prototype.createAvatar = function(){
};