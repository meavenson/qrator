var Qrator = function(){
	var t = this;
	//t.contextMenu = $("<ul/>").addClass("v-context-menu ui-corner-all");
	t.container = $("#qrator");
	
	// available panes
	t.panes = {};
		
	t.indicator = $("<img src='img/loading-32.gif'/>").addClass("q-loading").hide();
	$("#menu").prepend(t.indicator);
	t.indicatorCount = 0;
	
	// initialize a notifier
	t.notifier = new Notifier($("#footer"));
	
	// initialize services
	t.adminService = new AdminService(this);
	t.structService = new StructureService(this);
	t.matchService = new MatchService(this);
	t.treeService = new TreeService(this);
	
	//t.container.bind("contextmenu", function(){ return false; });
	$("body").click( function(){ /*t.contextMenu.remove();*/ $(".popover").remove(); });
	
	t.init();
};

Qrator.prototype = {

	init: function(){
		var t = this;
		
		var homepane = new HomePane(t);
		t.addPane(homepane, "home", "Home <span class='glyphicon glyphicon-home'/>");
		
		var statpane = new StatusPane(t);
		t.addPane(statpane, "status", "Status <span class='glyphicon glyphicon-stats'/>");
		
		var browsepane = new BrowsePane(t);
		t.addPane(browsepane, "browse", "Browse <span class='glyphicon glyphicon-eye-open'/>");
		
		$("#menu-list").children().first().click();
		t.banner();
	},

	loading: function(){
		this.indicator.show();
		this.indicatorCount++;
	},
	
	loaded: function(){
		this.indicatorCount--;
		if(this.indicatorCount == 0)
			this.indicator.hide();
	},
	
	message: function(resp){
		var t = this;
		if(resp["error"]) t.notifier.error(resp["error"]);
		else t.notifier.message(resp["message"]);
	},
	
	hidePanes: function(){
		for( var pane in this.panes ){
			if(this.panes[pane].container){
				this.panes[pane].container.hide();
				this.panes[pane].header.hide();
			}
		}
	},
	
	addPane: function(pane, label, title){
		var t = this;
		t.panes[label] = pane;
		if(pane.container){
			$("#content").append(pane.container.hide());
			$("#header").append(pane.header.hide());
		}

		var activate = function(){
			var pane = t.panes[label];
			$(".menu-opt").css({"background-color":"", "color":""}); 
			$(this).css({"background-color":"#ffc", "color":"#333"});
			pane.init();
		};
		
		var opt = $("<li/>").addClass("menu-opt").html("<span class='menu-label'>"+title+"</span>").click(activate);
		$("#menu-list").append(opt);
		pane.menuOption = opt;
	},
	
	login: function(resp){
		var t = this;
		var uid = resp["ssid"];
		if(uid){
			t.uid = uid;
			var username = resp["username"];
			
			// populate the banner
			var login = $("<span/>").html("<strong>User - </strong>").addClass("login");
			//<span class=\"badge bg-primary\"></span>
			login.append($("<a href='#' title='Settings'><span class='logged-in'>"+username+" <span class='glyphicon glyphicon-dashboard'/></a>")
						.click(function(){
							new UserProfile(t);
							return false;
						}))
				 .append($("<a href='#' alt='Logout'/>").html("<strong>Logout</strong>")
							  .addClass("logout")
							  .click(function(){
							  			var after = function(resp){
											$("#menu-list").empty();
											t.container.append($("#welcome"));
											$("#content").empty().append($("#welcome"));
											$("#header").empty();
											t.panes = {};
											$(".banner").remove();
											t.init();
											delete t.uid;
											t.message(resp);
							  			};
							  			t.adminService.logout(after);
							  			return false;
							  		})
						);
			$(".login").remove();
			$(".banner").append(login);
			
			// initialize the panes
			//var dashpane = new DashPane(t);
			//t.addPane(dashpane, "mine", "My Work");
			
			var roles = resp["roles"];
			if(roles){
				if(roles.indexOf("submit") != -1){
					var uploadpane = new UploadPane(t);
					t.addPane(uploadpane, "upload", "Upload <span class='glyphicon glyphicon-open'/>");
				}
			
				if(roles.indexOf("curate") != -1){
					var curationpane = new CurationPane(t);
					t.addPane(curationpane, "review", "Curate <span class='glyphicon glyphicon-wrench'/>");
				}

				if(roles.indexOf("admin") != -1){
					var adminpane = new AdminPane(t);
					t.addPane(adminpane, "admin", "Administration <span class='glyphicon glyphicon-user'/>");
				}
			}
		}else{
			var message = resp["error"]? resp["error"] : "Unknown error encountered.";
			t.notifier.error(message);
		}
	},
	
	banner: function(){
		var t = this;
		var loginBanner = $("<div/>").append("<span class='title'>Qrator <sup style='font-size:8px'><i>beta</i></sup></span>")
									 .append(
									 	$("<span/>").addClass("browsers")
									 				.append("<span>Tested with: </span>")
									 				.append("<a href='http://www.google.com/chrome/'><img src='img/chrome-32.png' title='Chrome'/></a>")
									 				.append("<a href='http://www.mozilla.org/en-US/firefox/new/'><img src='img/firefox-32.png' title='Firefox'/></a>")
									 				.append("<a href='https://www.apple.com/safari/'><img src='img/safari-32.png' title='Safari'/></a>")
									 ).append($("<span/>").addClass("help")
									 					  .append($("<a href='#' alt='Help'><strong>Help</strong></a>")
																	.click(function(){
																		if(t.help) t.help.show();
																		else t.help = new Help();
																		return false;
																	})
																)
											 )
								     .addClass("banner");
		
		var login = $("<span/>")
						.append($("<a href='#' alt='Login'><strong>Login</strong></a>")
									.click(function(){
											new Login(t);
											return false;
										}))
						.addClass("login")
						.css("margin-top","8px")
						.append($("<span> or </span>"))
						.append($("<a href='#' alt='Register'><strong>Register</strong></a>")
							.click(function(){
								new Login(t, true);
								return false;
							}));
		
		loginBanner.append(login);
		t.container.prepend(loginBanner);
	},
	
	menu: function(e, title, options){
		var t = this;
		t.contextMenu.empty();
		
		var header = $("<li/>").addClass("v-menu-header").html(title);
		t.contextMenu.append(header);
		
		$.each(options, function(key, val){
			var opt = $("<li/>").append($("<span/>").html(key))
								.append($("<span/>").addClass("v-icon v-opt-icon "+val.icon))
								.addClass("v-menu-option "+(val.css?val.css:""))
								.click( function(){ t.contextMenu.remove(); val.click(); return false; });
			t.contextMenu.append(opt);
		});
		$("body").append(t.contextMenu);
		t.contextMenu.css({ "top": e.originalEvent.pageY+"px", "left": e.originalEvent.pageX+"px" });
	}
	
};