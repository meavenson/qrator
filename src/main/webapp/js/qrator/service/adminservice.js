var AdminService = function(app){
	this.app = app;
	this.util = new ServiceUtil(app, "/qrator/service/admin");
};

AdminService.prototype = {

	login: function(username, password, after){
		var t = this;
		var action = "login";
		var data = { "username" : username, 
					 "password" : hex_sha1(password)};
		t.util.request(true, action, data, after);
	},
	
	register: function(username, password, name, email, after){
		var t = this;
		var action = "register";
		var data = { "username" : username, 
					 "password" : hex_sha1(password),
					 "name"		: name,
					 "email"	: email };
		t.util.request(true, action, data, after);
	},
	
	logout: function(after){
		var t = this;
		var action = "logout";
		var data = { "ssid" : t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	reset: function(email, after){
		var t = this;
		var action = "reset";
		var data = { "email" : email};
		t.util.request(true, action, data, after);
	},
	
	list: function(after){
		var t = this;
		var action = "list";
		var data = { "ssid" : t.app.uid,
		             "offset":-1,
		             "limit": -1 };
		t.util.request(true, action, data, after);
	},
	
	getUserDetails: function(after){
		var t = this;
		var action = "get/userdetails";
		var data = { "ssid" : t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	modify: function(user, after){
		var t = this;
		var action = "modify";
		var data = { "ssid" : t.app.uid,
					 "username": user.username,
					 "password": hex_sha1(user.password),
					 "name": user.name,
					 "email": user.email };
		if(user.newpass) data["newpass"] = hex_sha1(user.newpass);
		t.util.request(true, action, data, after);
	},
	
	activate: function(user, after){
		var t = this;
		var action = "activate/"+user.id;
		var data = { "ssid" : t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	deactivate: function(user, after){
		var t = this;
		var action = "deactivate/"+user.id;
		var data = { "ssid" : t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	addRole: function(user, role, after){
		var t = this;
		var action = "add/role/"+user.id;
		var data = { "ssid" : t.app.uid,
		             "role": role };
		t.util.request(true, action, data, after);
	},
	
	removeRole: function(user, role, after){
		var t = this;
		var action = "remove/role/"+user.id;
		var data = { "ssid" : t.app.uid,
		             "role": role };
		t.util.request(true, action, data, after);
	},
	
	listRoles: function(after){
		var t = this;
		var action = "list/roles";
		var data = { "ssid" : t.app.uid };
		t.util.request(true, action, data, after);
	}
};