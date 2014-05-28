var UserDisplay = function(app) {
    var t = this;
    t.app = app;
    t.service = app.adminService;
    t.headings = ["", "Username", "Name", "Email", "Registration", "Last Login", "Privileges"];
    t.container = $("<div/>").addClass("q-table");
    var tableContainer = $("<div/>");
    var buttons = t.initButtons();
    t.table = buildTable(tableContainer, t.headings);
    t.container.append(buttons).append(tableContainer);
};

UserDisplay.prototype = {

	initButtons: function(){
		var t = this;
		var load = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon-tree-deciduous'/> Load Canonical Trees</button>")
			.click(function(e) {
				var callback = function(resp){
					t.app.message(resp);
					t.app.loaded();
				};
				t.app.treeService.loadTrees(callback);
				t.app.loading();
			});
		
		var commit = $("<button type='button' class='btn btn-primary btn-sm'><span class='glyphicon glyphicon glyphicon-hdd'/> Commit Approved Structures</button>")
			.click(function(e) {
				var callback = function(resp){
					t.app.message(resp);
					t.app.loaded();
				};
				t.app.structService.commit(callback);
				t.app.loading();
			});
		
		var container = $("<div/>")
			.addClass("q-button-container")
			.append(load.css("margin-right","5px"))
			.append(commit);
			
		return container;
	},

    search: function() {
        var t = this;
        t.app.loading();
        var callback = function(resp) {
            t.parseUsers(resp["objs"]);
            t.app.loaded();
        };
        t.service.list(callback);
    },
    roleFunction: function(button, hasRole, user, role){
    	var t = this;
    	return function(){
			var after = function(resp){
				if(!resp["error"]){
					button.toggleClass("add-role-button remove-role-button");
					button.children("i").toggleClass("glyphicon-minus-sign").toggleClass("glyphicon-plus-sign");
					button.unbind("click").click(t.roleFunction(button, !hasRole, user, role));
				}
				t.app.message(resp);
			};
			if(hasRole) t.service.removeRole(user, role, after);
			else t.service.addRole(user, role, after);
		};
    },
    activateFunction: function(user){
    	var t = this;
    	return function(){
			var after = function(resp){
				if(resp["error"]){
					$(this).change();
				}
				t.app.message(resp);
			};
			if($(this).is(":checked")) t.service.activate(user, after);
			else t.service.deactivate(user, after);
		};
    },
    parseUsers: function(users) {
        var t = this;
        t.table.fnClearTable();
        var callback = function(resp){
			var roleList = resp["objs"];
			for (var i = 0; i < users.length; i++) {
				var user = new User(users[i].id, users[i]);
			
				var activeCell = user.avatar.children("td:first");
				activeCell.children("input")
						  .change(t.activateFunction(user));
				
				var roleCell = user.avatar.children("td:last");
				var roles = roleCell.text().split(",");
				roleCell.empty();
				var btngrp = $("<div/>").addClass("btn-group");
				for(var j=0; j<roleList.length; j++){
					var role = roleList[j];
					var hasRole = roles.indexOf(role) != -1;
					var roleButton = $("<button/>").addClass("btn btn-primary btn-xs"+(!hasRole?" add-role-button":" remove-role-button"))
									  .append("<i class='glyphicon "+(hasRole?"glyphicon-minus-sign":"glyphicon-plus-sign")+"' style='font-size:10px'/>&nbsp;")
									  .append("<strong>"+role+"</strong>");
					roleButton.click(t.roleFunction(roleButton, hasRole, user, role));
					btngrp.append(roleButton);
				}
				roleCell.append(btngrp);
				t.table.fnAddTr(user.avatar[0]);
			}
			t.table.fnDraw();
		};
		t.service.listRoles(callback);
    }
};