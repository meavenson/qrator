var Annotation = function(id, attrbs){
	Entity.call(this, id, attrbs);
	this.createAvatar();
};

Annotation.prototype = new Entity();

Annotation.prototype.createAvatar = function(){
	var avatar = $("<li/>").addClass("q-meta q-annotation").html(this.comment);
	//.html("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec id bibendum sapien. Integer nibh mi, tempus a malesuada eu, rhoncus et nisi. Nullam tempor cursus eleifend. Donec tempor sem nisl, sed malesuada diam. Proin sollicitudin faucibus elit sed hendrerit. Nam mauris velit, feugiat et vehicula vel, tempus eget felis. Suspendisse sollicitudin nunc id risus feugiat sagittis ac ac augue. Proin ultrices vestibulum nisl, nec molestie arcu dignissim in. Curabitur sapien dolor, aliquet non fermentum pellentesque, porta ut sapien. Aliquam vel lobortis sem. Donec euismod dignissim ante ultricies auctor.");
	var label = $("<div/>").addClass("q-meta-label");
	var field = $("<div/>").addClass("q-meta-field");
	var createdBy = $("<div/>").html(this.createdBy);
	var createdOn = $("<div/>").html(this.createdOn);
	avatar.append(label.append(field.append(createdBy).append(createdOn)));
	this.setAvatar(avatar);
};