var Entity = function(id, attrbs){
	this.id = id;
	this.update(attrbs);
};

Entity.prototype = {

	update: function(attrbs){
		for(var attrb in attrbs)
			if(attrbs[attrb] == "") delete this[attrb];
			else this[attrb] = attrbs[attrb];
	},

	setAvatar: function(avatar){
		this.avatar = avatar;
		avatar.data("obj", this);
	},
	
	setName: function(name){
		this.name = name;
		$(this.label).html(name);
	},

	hide: function(){
		this.avatar.hide();
	},
	
	show: function(){
		this.avatar.show();
	},
	
	remove: function(){
		if(this.avatar){
			this.avatar.remove();
			delete this.avatar;
		}
	}
};