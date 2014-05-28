var GridNode = function(container){
	this.container = container;
	this.ranks = [];
	this.rootNode = null;
	this.radius = 30;
	this.iterations = Infinity; //2000;
};

GridNode.prototype = {

	sameRank: function(node, parent){
		
		var lbl = node.label.search("Fuc") != -1 || 
				   node.label.search("Xyl") != -1;
		if(parent){
			var plbl = parent.label.search("Fuc") != -1 || 
				       parent.label.search("Xyl") != -1;
			return lbl && !plbl;
		}else return lbl;
		
	},
	
	angled: function(node){
		return node.label.search("Fuc") != -1 || 
			   node.label.search("Xyl") != -1 || 
			   node.label.search("Rha") != -1 ||
			   node.label.search("Qui") != -1;
	},
	
	generateSortingNumber: function(node, link){
		var sortNum = 0;
		for(var i = 0; i<node.children.length; i++){
			var child = node.children[i];
			if (child.link == link) {
				sortNum++;
			}
		}
		return sortNum;        
	},

	addNode: function(node){
		var t = this;
		var parent = node.parent;
		var rank = 0;
		// necessary for angled residues (xylose, fucose, rhamnose, quinovose)
		if(t.angled(node)) node.angle = 0;
		
		if(parent){
			var sameRank = t.sameRank(node, parent);
			rank = sameRank? parent.rank:parent.rank+1;
			var sort = t.generateSortingNumber(parent, node.link);
			node.sort = sort;
		}else{
			t.rootNode = node;
			node.sort = 0;
		}
		node.rank = rank;
		if(!t.ranks[rank]) t.ranks[rank] = [];
		t.ranks[rank].push(node);
	},
	
	removeNode: function(node){
		var rank = this.ranks[node.rank];
		for(var i=0; i<rank.length; i++)
			if(rank[i] == node) rank.splice(i, 1);
	},

	flattenTree: function(){
		var t = this;
		var height = t.container.height();
		var width = t.container.width();
		for(var i=0; i<t.ranks.length; i++){
			var rank = t.ranks[i];
			for(var j=0; j<rank.length; j++){
				var node = rank[j];
				var parent = node.parent;
				var x = width/2+((t.ranks.length-1)*30)/2;//+20;
				var y = height/2;
				if(parent){
					var sameRank = t.sameRank(node, parent);
					x = sameRank? parent.x : parent.x-t.radius;
					y = parent.y;
				}				
				node.x = x;
				node.y = y;
				node.row = 0;
			}
		}
	},

	moveTree: function(node, rows){
		node.y += rows*this.radius/2;
		node.row += -rows;
		var parent = node.parent;
		if(parent && this.sameRank(node, parent)){
			if(parent.y > node.y) node.angle = 90;
			else node.angle = -90;
		}
		var children = node.children;
		for(var i=0; i<children.length; i++){
			this.moveTree(children[i], rows);
		}
	},
	
	render: function(){
		var t = this;
		t.flattenTree();
		var col = t.expRender(0);
 		var iteration = 0;
 		while(iteration < t.iterations && col){
 			col = t.expRender(0);
 			iteration++;
 		}
	},

	expRender: function(rank){
		var t = this;
		var nodes = t.ranks[rank];
		var collision = false;
		if(!nodes) return collision;
		for(var i=0; i<nodes.length; i++){
			var n1 = nodes[i];
			for(var j=i+1; j<nodes.length; j++){
				var n2 = nodes[j];
				if(n1 == n2) continue;
				if( Math.abs(n1.row-n2.row) < 2 ){
					t.adjustNodes(n1, n2);
					collision = true;
				}
			}
		}
		collision = collision==true?collision:t.expRender(rank+1);
		return collision;
	},

	commonAncestor: function(n1, n2){
		var anc = {c1:n1, c2:n2, a:n1};
		// handle special case:
		//     Fuc or Xyl attached to another Fuc or Xyl
		if(n1.parent == n2){
			anc.a = n2;
			return anc;
		}else if(n2.parent == n1){
			anc.a = n1;
			return anc;
		}
		
		var n1parent = n1.parent;
		while(n1parent && n1parent.rank == n1.rank){
			anc.c1 = n1;
			n1 = n1parent;
			n1parent = n1.parent;
			anc.a = n1;
		}
		
		var n2parent = n2.parent;
		while(n2parent && n2parent.rank == n2.rank){
			anc.c2 = n2;
			n2 = n2parent;
			n2parent = n2.parent;
			anc.a = n2;
		}
		
		while(n1 != n2){
			anc.c1 = n1;
			anc.c2 = n2;
			n1parent = n1.parent;
			n2parent = n2.parent;
			if(n1parent == n2){
				n1 = n1parent;
				anc.a = n2;
			}else if(n2parent == n1){
				n2 = n2parent;
				anc.a = n1;
			}else{
				n1 = n1parent;
				n2 = n2parent;
				anc.a = n1;
			}
		}
		return anc;
	},
	
	adjustNodes: function(node1, node2){
		var t = this;
		var rows = 1;
		
		// find common ancestor
		var anc = t.commonAncestor(node1, node2);
		
		var a1 = anc.c1;
		var a2 = anc.c2;
		var a = anc.a;
		
		//console.debug("adjust: "+a1.uri+" -- "+a2.uri);
		
		// handle special case:
		//     Fuc or Xyl attached to another Fuc or Xyl
		if( t.sameRank(a1) && a == a2 ){
			//console.debug("a1 "+a1.uri);
			if( t.sameRank(a) ){
				if( a.link > 3 ){
					t.moveTree(a1, -rows);
					//console.debug("   up "+a1p.uri);
				}else{
					t.moveTree(a1, rows);
					//console.debug("   down "+a1p.uri);
				}
			}else{
				if( a1.link > 3 ){
					//console.debug("   up: "+a1.uri);
					t.moveTree(a1, -rows);
				}else{
					//console.debug("   down: "+a1.uri);
					t.moveTree(a1, rows);
				}
			}
		}else if( t.sameRank(a2) && a == a1 ){  // also special case
			//console.debug("a2 "+a2.uri);
			if( t.sameRank(a) ){
				if( a.link > 3 ){
					t.moveTree(a2, -rows);
					//console.debug("   up "+a2p.uri);
				}else{
					t.moveTree(a2, rows);
					//console.debug("   down "+a2p.uri);
				}
			}else{
				if( a2.link > 3 ){
					//console.debug("   up: "+a2.uri);
					t.moveTree(a2, -rows);
				}else{
					//console.debug("   down: "+a2.uri);
					t.moveTree(a2, rows);
				}
			}
		}else{  // end special cases
			// if the first node is a Fuc or Xyl
			if( t.sameRank(a1) ){
				// move up if link is > 3
				if( a1.link > 3 ){
					//console.debug("   up: "+a1.uri);
					t.moveTree(a1, -rows);
				}else{ // move down
					//console.debug("   down: "+a1.uri);
					t.moveTree(a1, rows);
				}
			// if the second node is a Fuc or Xyl
			}else if( t.sameRank(a2) ){
				// move up if link is > 3
				if( a2.link > 3 ){
					//console.debug("   up: "+a2.uri);
					t.moveTree(a2, -rows);
				}else{ // move down
					//console.debug("   down: "+a2.uri);
					t.moveTree(a2, rows);
				}
			}else{
				// No Fuc or Xyl - move one up and one down
				if( a1.link > a2.link ){
					//console.debug("   up "+a1.uri);
					t.moveTree(a1, -rows);
					//console.debug("   down "+a2.uri);
					t.moveTree(a2, rows);
				}else{
					//console.debug("   down "+a1.uri);
					t.moveTree(a1, rows);
					//console.debug("   up "+a2.uri);
					t.moveTree(a2, -rows);
				}
			}
		}
			
	}

};