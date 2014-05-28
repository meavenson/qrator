// CSS classes
// q-input
// q-input-field
// q-filter-button
// q-filter-container

var Filter = function(app){
	this.app = app;
	this.filter = {};
	this.fields = {};
	this.chains = {};
	this.container = $("<div/>").addClass("q-filter-container");
};

Filter.prototype = {

	generateField: function(field){
		var t = this;
		var elmt = field["elmt"];
		var id = field["id"];
		var name = field["name"];
	
		//$("#"+id).remove();
		if(!elmt.is("div")){
			elmt.attr({"placeholder":name, "name":id});
		}else{
			elmt.children("input").attr({"placeholder":name, "name":id});
		}
		if(elmt.attr("type") != "hidden"){
			var group = $("<div class='form-group'/>");
			var label = $("<label class='control-label' for='"+id+"'>"+name+"</label>");
			var controls = $("<div class='controls'>");
			controls.append(elmt);
			if(field["required"] == true){
				controls.append($("<span>*required</span>").addClass("help-inline required"));
			}
			group.append(label).append(controls);
			field.input = group;
			t.container.append(field.input);
		}else{
			t.container.append(elmt);
		}
		t.fields[field.id] = field;
	},
	
	reset: function(){
		var t = this;
		// remove existing chains
		for(var id in t.chains){
			var chain = t.chains[id].id;
			if(t.fields[chain]){
				t.fields[chain].input.remove();
				delete t.fields[chain];
				delete t.filter[chain];
			}
		}
		for(var id in t.fields){
			var elmt = t.fields[id].elmt;
			if(!elmt.is("div")){
				elmt.val("");
			}else{
				elmt.children("input").val("");
			}
		}
		t.filter = {};
	},
	
	setValues: function(options, after){
		var t = this;
		
		for(var field in options){
			// init callback from invoking chain
			var callback = function(){
				t.fields[field].elmt.val(options[field]);
				delete options[field];
				t.setValues(options, after);
			};
			// treat as a regular field if invoking as a chain doesn't work
			if(!t.invokeChain(field, callback)){
				if(t.fields[field]){
					t.fields[field].elmt.val(options[field]);
					delete options[field];
				}
			}else return;
		}
		t.refresh();
		if(after) after();
	},
	
	invokeChain: function(chain, after){
		var t = this;
		for(var id in t.chains){
			if(t.chains[id].id == chain){
				t.chains[id].func(t.fields[id].elmt.val(), after);
				return true;
			}
		}
		return false;
	},
	
	generateSelect: function(options){
		var t = this;
		var select = $("<select/>").addClass("form-control");
		for(var id in options){
			select.append($("<option>"+id+"</option>").val(options[id]));
		}
		return select;
	},
	
	chainSelect: function(field, chain, func){
		this.chains[field] = {"id":chain, "func":func};
	},
	
	refresh: function(){
		var t = this;
		for(var key in t.fields){
			var field = t.fields[key];
			var elmt = field["elmt"];
			if(elmt.is("div")) elmt = elmt.children("input");
			var id = field["id"];
			t.filter[key] = elmt.val();
		}
	},
	
	enable: function(){
		var t = this;
		for(var chain in t.chains){
			var field = t.fields[chain];
			var func = t.chains[chain].func;
			field.input.change( function(){
									t.filter[chain] = field.input.find("option:selected").val();
									func(t.filter[chain]);
								} );
		}
	}

};