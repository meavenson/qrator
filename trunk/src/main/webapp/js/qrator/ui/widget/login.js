var Login = function(app, register){
	Wizard.call(this, app);
	var t = this;
	t.modal = modal($("<div/>"), "", {}, 400);
	if(register) t.register();
	else t.login();
};

Login.prototype = new Wizard();

Login.prototype.after = function(resp){
	var t = this;
	if(resp["error"]) t.app.message(resp);
	else t.app.login(resp);
};

Login.prototype.login = function(){
	var t = this;
	var form = $("<form/>").addClass("form-horizontal");
	
	var funcs = {};
	var pwd = $("<input type='password'/>").keyup(function(e){
													if(e.keyCode == 13){
														funcs["Login"](e);
													}
												});
	var fields = [{ "id":"username", "name":"Username", "elmt":$("<input type='text'/>"), 	  "required":true },
				  { "id":"password", "name":"Password", "elmt":$("<input type='password'/>"), "required":true }];
	
	t.generateFields(form, fields);

	funcs = {"Login": function(e){ 
						if(t.validate(fields)){
							t.app.adminService.login(t.inputs["username"].val(),
													 t.inputs["password"].val(),
													 function(resp){ t.after(resp); });
						}else e.stopPropagation();
					},
			 "Cancel": function(e){}
			};
	
	t.modal.setTitle("Login");
	t.modal["body"].empty()
				   .append(form)
				   .append($("<a href='#'>Forgot your password?</a>")
								.css("margin-left","100px")
								.click(function(e){
									t.reset();
					}));
	t.modal["footer"].empty();
	t.modal.generateButtons(funcs);
	t.modal.setWidth(400);
};

Login.prototype.register = function(){
	var t = this;
	var form = $("<form/>").addClass("form-horizontal");
	
	var fields = [{ "id":"username", "name":"Username", "elmt":$("<input type='text'/>"), 	  "required":true },
				  { "id":"password", "name":"Password", "elmt":$("<input type='password'/>"), "required":true },
				  { "id":"confirm",  "name":"Confirm",  "elmt":$("<input type='password'/>"), "required":true },
				  { "id":"name",     "name":"Name",     "elmt":$("<input type='text'/>"),     "required":true },
				  { "id":"email",    "name":"Email",    "elmt":$("<input type='text'/>"),     "required":true }];
	
	t.generateFields(form, fields);

	var funcs = {"Register": function(e){ 
							if(t.validate(fields)){
								if(t.inputs["password"].val() == t.inputs["confirm"].val()){
									t.app.adminService.register(t.inputs["username"].val(), 
															t.inputs["password"].val(), 
															t.inputs["name"].val(), 
															t.inputs["email"].val(),
															function(resp){ t.after(resp); });
								}
							}else e.stopPropagation();
						},
				  "Cancel": function(e){ t.login(); }
				};
	
	t.modal.setTitle("Register");
	t.modal["body"].empty().append(form);
	t.modal["footer"].empty();
	t.modal.generateButtons(funcs);
	t.modal.setWidth(500);
};

Login.prototype.reset = function(){
	var t = this;
	var form = $("<form/>").addClass("form-horizontal");
	var funcs = {};
	var fields = [{ "id":"email", "name":"Email", "elmt":$("<input type='text'/>"), "required":true }];
	
	t.generateFields(form, fields);

	funcs = {"Reset": function(e){
						t.app.loading();
						if(t.validate(fields)){
							t.app.adminService.reset(t.inputs["email"].val(),
													 function(resp){
													 	t.app.message(resp); 
													 	t.app.loaded();
													 });
						}else e.stopPropagation();
					},
			 "Cancel": function(e){}
			};
	
	t.modal.setTitle("Reset Password");
	t.modal["body"].empty().append(form);
	t.modal["footer"].empty();
	t.modal.generateButtons(funcs);
	t.modal.setWidth(400);
};