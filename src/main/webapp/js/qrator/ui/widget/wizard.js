var Wizard = function(app){
	var t = this;
	t.obj = {};
	t.app = app;
	t.inputs = {};
};

Wizard.prototype = {

	generateField: function(input){
		var t = this;
		var elmt = input["elmt"];
		var id = input["id"];
		var name = (input["required"] === true?"*":"")+input["name"];
		$("#"+id).remove();
		if(!elmt.is("div")){
			elmt.attr({"id":id, "placeholder":name, "name":id}).addClass("form-control");
		}else{
			elmt.children("input").attr({"id":id, "placeholder":name, "name":id}).addClass("form-control");
		}
		var group = $("<div class='form-group'/>");
		var label = $("<label class='col-lg-3 control-label' for='"+id+"'>"+name+"</label>");
		var controls = $("<div class='col-lg-9'/>");
		controls.append(elmt);
		group.append(label).append(controls);
		t.inputs[id] = elmt;
		return group;
	},
	
	generateFields: function(parent, inputs){
		var t = this;
		for(var i=0; i<inputs.length; i++){
			var input = inputs[i];
			parent.append(t.generateField(input));
		}
	},
	
	generateOptions: function(list, selectElmt){
		for(var i=0; i<list.length; i++){
			var option = $("<option>"+list[i]+"</option>");
			selectElmt.append(option);
		}
		return selectElmt;
	},
	
	validate: function(fields) {
        var t = this;
        var result = true;
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var elmt = field["elmt"];
            if (field["required"] === true) {
                var combo = false;
                if (elmt.is("div")) {
                    elmt = elmt.children("input");
                    combo = true;
                }
                var grp = elmt.parent().parent();
                if(combo) grp = grp.parent();
                if (elmt.val() === "") {
                    if(combo) grp.addClass("has-error");
                    else{
                    	grp.append($("<span/>").addClass("glyphicon glyphicon-remove form-control-feedback"))
                    	   .addClass("has-error has-feedback");
                    }
                    result = false;
                } else {
                    if(combo) grp.removeClass("has-error");
                    else{
                        grp.removeClass("has-error has-feedback");
                        grp.find(".glyphicon-remove").remove();
                    }
                }
            }
            if (field["maxchars"]) {
                var max = field["maxchars"];
                if (elmt.val().length > max) {
                    t.app.notifier.error(field["name"] + "'s length exceeds " + max + " characters");
                    result = false;
                }
            }
        }
        return result;
    },
	collectData: function(fields, obj) {
        var t = this;
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var elmt = field["elmt"];
            if (elmt.is("div"))
                elmt = elmt.children("input");
            var id = field["id"];
            if(!obj) t.obj[id] = elmt.val();
            else obj[id] = elmt.val();
        }
    },
    restore: function(obj) {
    	var t = this;
    	var obj = obj? obj : t.obj;
        for (var id in obj) {
        	var input = t.inputs[id];
        	if(input){
				if (!input.is("div")) {
					input.val(obj[id]);
				} else {
					input.children("input").val(obj[id]);
				}
			}
        }
    }

};