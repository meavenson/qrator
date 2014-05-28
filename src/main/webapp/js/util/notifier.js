var Notifier = function(container){
	var t = this;
	var messages = [];
	var errors   = [];
	var active   = [];
	var elmt = $("<ul/>").addClass("note-holder");
	container.append(elmt);
	
	var remove = function(){
		active[0].remove();
		active.splice(0,1);
	};
	
	var message = function( msg, error ){
		var icon = $("<i/>").addClass("glyphicon note-icon");
		var note = $("<li/>").addClass("note").html(msg)
							.append(icon);
							
		if(error){
			icon.addClass("glyphicon-warning-sign");
			note.addClass("note-error");
		}else{
			icon.addClass("glyphicon-info-sign");
			note.addClass("note-success");
		}
		elmt.prepend(note);
	
		active.push(note);
		if(error) errors.push(msg);
		else messages.push(msg);
	
		var callback = function(){
			setTimeout(function(){
					var cb = function(){
						remove();
					};
					note.fadeOut(500, cb);
			}, 7000);
		};
		note.fadeTo(500, 0.9, callback);
	};
	
	var error = function( msg ){
		this.message( msg, true );
	};
	
	return {
		message: message,
		error: error
	};
};