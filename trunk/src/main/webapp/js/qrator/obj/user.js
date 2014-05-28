var User = function(id, attrbs){
	Entity.call(this, id, attrbs);
	this.createAvatar();
};

User.prototype = new Entity();

User.prototype.createAvatar = function(){
	var avatar = $("<tr/>");
	avatar.append($("<td><input type=\"checkbox\"" + (this.active=="true"? "checked":"") + "/></td>"));
    avatar.append($("<td>" + this.username + "</td>"));
    avatar.append($("<td>" + this.name + "</td>"));
    avatar.append($("<td>" + this.email + "</td>"));
    avatar.append($("<td>" + this.createdOn + "</td>"));
    avatar.append($("<td>" + this.lastLogin + "</td>"));
    avatar.append($("<td>" + this.roles + "</td>"));

    this.setAvatar(avatar);
};