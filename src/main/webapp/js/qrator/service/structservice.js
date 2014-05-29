var StructureService = function(app){
	this.app = app;
	this.address = "service/structure";
	this.util = new ServiceUtil(app, this.address);
};

StructureService.prototype = {

	/*search: function(motif, offset, limit, after){
		var t = this;
		var action = "search";
		var data = { "ssid": t.app.uid,
					 "motif": JSON.stringify(motif),
					 "offset": offset,
					 "limit": limit };
		t.util.request(false, action, data, after);
	},*/
	
	build: function(spec, after){
		var t = this;
		var action = "build";
		var data = { "ssid": t.app.uid,
					 "spec": JSON.stringify(spec)
				   };
		t.util.request(false, action, data, after);
	},
	
	compare: function(struct, after){
		var t = this;
		var action = "compare/"+struct.id;
		t.util.request(true, action, {}, after);
	},

	count: function(after){
		var t = this;
		var action = "count";
		t.util.request(true, action, {}, after);
	},
	
	addAnnotation: function(struct, comment, after){
		var t = this;
		var action = "add/annotation/"+struct;
		var data = { "ssid":    t.app.uid,
					 "comment": comment };
		t.util.request(false, action, data, after);
	},
	
	removeAnnotation: function(annotation, after){
		var t = this;
		var action = "remove/annotation/"+annotation;
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	addReference: function(file, source, refId, after){
		var t = this;
		var action = "add/reference/"+file;
		var data = { "ssid":   t.app.uid,
					 "source": source,
					 "refId":  refId };
		t.util.request(true, action, data, after);
	},

	removeReference: function(reference, after){
		var t = this;
		var action = "remove/reference/"+reference;
		var data = { "ssid":    t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	review: function(spec, structId, type, comment, after){
		var t = this;
		var action = "review/"+structId;
		var data = { "ssid": t.app.uid,
					 "spec": JSON.stringify(spec),
					 "type": type,
					 "comment": comment };
		t.util.request(false, action, data, after);
	},
	
	checkout: function(struct, after){
		var t = this;
		var action = "checkout/"+struct;
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	checkin: function(struct, after){
		var t = this;
		var action = "checkin/"+struct;
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	},
	
	defer: function(struct, comment, after){
		var t = this;
		var action = "defer/"+struct;
		var data = { "ssid": t.app.uid,
					 "comment": comment };
		t.util.request(false, action, data, after);
	},
	
	approve: function(struct, comment, after){
		var t = this;
		var action = "approve/"+struct;
		var data = { "ssid": t.app.uid,
					 "comment": comment };
		t.util.request(false, action, data, after);
	},
	
	reject: function(struct, comment, after){
		var t = this;
		var action = "reject/"+struct;
		var data = { "ssid": t.app.uid,
					 "comment": comment };
		t.util.request(false, action, data, after);
	},
	
	commit: function(after){
		var t = this;
		var action = "commit";
		var data = { "ssid": t.app.uid };
		t.util.request(true, action, data, after);
	},

	list: function(status, tree, type, owned, motif, offset, limit, after){
		var t = this;
		var action = "list";
		var data = { "offset": offset,
					 "limit": limit };
		
		if(t.app.uid) data["ssid"] = t.app.uid;
		if(tree) data["tree"] = tree;
		if(type) data["type"] = type;
		if(status) data["status"] = status;
		if(motif) data["motif"] = JSON.stringify(motif);
		if(owned) data["owned"] = owned;
		t.util.request(false, action, data, after);
	},
	
	getById: function(source, reference, after){
		var t = this;
		var action = "get/"+source+"/"+reference;
		t.util.request(true, action, {}, after);
	},
	
	annotations: function(struct, offset, limit, after){
		var t = this;
		var action = "annotations/"+struct;
		var data = { "offset": offset,
					 "limit": limit };
		t.util.request(true, action, data, after);
	},
	
	references: function(struct, offset, limit, after){
		var t = this;
		var action = "references/"+struct;
		var data = { "offset": offset,
					 "limit": limit };
		t.util.request(true, action, data, after);
	},
    
    provenance: function(struct, offset, limit, after){
		var t = this;
		var action = "provenance/"+struct;
		var data = { "offset": offset,
					 "limit": limit };
		t.util.request(true, action, data, after);
	},
	
	sources: function(after){
		var t = this;
		var action = "sources";
		t.util.request(true, action, {}, after);
	}
	
};