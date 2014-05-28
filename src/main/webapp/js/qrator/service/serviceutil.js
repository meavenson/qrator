var ServiceUtil = function(app, url){
	this.app = app;
	this.url = url;
};

ServiceUtil.prototype = {

	request : function(getRequest, action, data, after){
		var t = this;
		var callback = function(resp){
			if(after) after(resp);
			//else t.app.message(resp);
		};
		if(getRequest) this._getRequest(action, data, callback);
		else this._postRequest(action, data, callback);
	},

	// performs get requests to a service
	_getRequest : function(action, data, callback){
		var url = this.url+"/"+action;
		$.get(url, data, callback);
	},
	
	// performs get requests to a service
	_postRequest : function(action, data, callback){
		var url = this.url+"/"+action;
		$.post(url, data, callback);
	},

}