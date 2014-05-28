var UserProfile = function(app) {
	Wizard.call(this, app);
	var t = this;
	t.service = app.adminService;
	t.modal = modal($("<div/>").css("height", "100px"), "User Settings", {}, 600);
	var callback = function(resp){
		t.obj = resp["objs"];
		t.step1();
	};
	t.service.getUserDetails(callback);
};

UserProfile.prototype = new Wizard();

UserProfile.prototype.modify = function() {
	var t = this;
	var after = function(resp){
		t.app.message(resp);
	};
	t.service.modify(t.obj, after);
};


UserProfile.prototype.step1 = function() {
	var t = this;
	var form = $("<form/>").addClass("form-horizontal");
	var fields = [ 
	{"id" : "name",	    	"name" : "Name",     	"elmt" : $("<input type='text'/>"), 	"required" : true,	"maxchars" : 256},
	{"id" : "username",		"name" : "Username",    "elmt" : $("<input type='text'/>"), 	"required" : true,	"maxchars" : 256},
	{"id" : "email",		"name" : "Email", 		"elmt" : $("<input type='text'/>"), 	"required" : true,	"maxchars" : 256}];
	t.generateFields(form, fields);
	
	form.append("<h5>Please enter your password to make changes.</h5>");
	var pwd = [ {"id" : "password",		"name" : "Password", 	"elmt" : $("<input type='password'/>"), "required" : true,	"maxchars" : 256} ];
	t.generateFields(form, pwd);
	
	var funcs = {
		"Change Password" : function(e) {
								t.step2();
							},
		"Update" : 			function(e) {
								if (t.validate(fields) && t.validate(pwd)) {
									t.collectData(fields);
									t.collectData(pwd);
									t.modify();
								} else{
									e.stopPropagation();
								}
							},
		"Cancel" : 			function() {}
	};
	
	t.modal["body"].empty().append(form);
	t.modal["footer"].empty();
	t.modal["header"].find("h3").html("User Settings");
	t.modal.generateButtons(funcs);
	t.modal.setWidth(500);
	t.restore();
};

UserProfile.prototype.step2 = function() {
	var t = this;
	var form = $("<form/>").addClass("form-horizontal");
	var fields = [ 
	{"id" : "password",		"name" : "Password",		"elmt" : $("<input type='password'/>"), "required" : true,	"maxchars" : 256},
	{"id" : "newpass",		"name" : "New Password",    "elmt" : $("<input type='password'/>"), "required" : true,	"maxchars" : 256},
	{"id" : "confirm",		"name" : "Confirm",			"elmt" : $("<input type='password'/>"), "required" : true,	"maxchars" : 256}];
	t.generateFields(form, fields);
	
	var funcs = {
		 "Update" : 		function(e) {
								if (t.validate(fields)) {
									t.collectData(fields);
									if(t.obj.newpass == t.obj.confirm){
										t.modify();
									}else{
										t.app.message({"error":"Confirmation does not match your new password."});
										e.stopPropagation();
									}
								} else{
									e.stopPropagation();
								}
							},
		 "Cancel" : 		function(){ 
		 						t.step1(); 
		 					}
	};
	
	t.modal["body"].empty().append(form);
	t.modal["footer"].empty();
	t.modal["header"].find("h3").html("Change Password");
	t.modal.generateButtons(funcs);
	t.modal.setWidth(500);
	t.restore();
};
	
	