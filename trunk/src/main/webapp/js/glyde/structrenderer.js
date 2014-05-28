var StructRenderer = function(options){
	var t = this;
	t.colors = {
		"relation" : "#000",
		"outline"  : "#000",
		"text"     : "#000"
	};
	t.residueSize = 15; // size of the residues
	t.isTree = options? options.isTree : false;
	t.builder = options? options.builder : false;
	t.id = options && options.struct? options.struct.id : "builder";
	t.spec = options && options.struct? options.struct.spec : false;
	t.container = options && options.struct && options.struct.struct? options.struct.struct: $("<div/>").addClass("struct");
};

StructRenderer.prototype = {
	
	build: function(){
		var t = this;
		t.container.find("svg").remove();
		
		t.svg = d3.select(t.container[0]).append("svg:svg").attr("width", "100%")
			.attr("height", "100%");
		
		t.viewport = t.svg.append("svg:g");
		if(t.id) t.viewport.attr("id", "struct"+t.id);
		
		t.nodes = [];
		t.links = [];
	
		t.gridnode = new GridNode(t.container);
	
		if(t.spec){
			// parse the structure
			t.parseStructure(undefined, t.spec);
	
			for(var i=0; i<t.nodes.length; i++){
				t.gridnode.addNode(t.nodes[i]);
			}
	
			// populate relations
			t.relations = t.viewport.selectAll(".link").data(t.links).enter()
				.append("svg:g");
		
			// populate entities
			t.entities = t.viewport.selectAll(".node").data(t.nodes).enter()
				.append("svg:g");
				//.on("mousedown", function(d){ d3.event.stopPropagation(); });
			
			if(t.isTree){
				var zoom = d3.behavior.zoom().on("zoom",function() {
								d3.select("#struct"+t.id)
									.attr("transform", "translate(" + d3.event.translate + ") "
										 +"scale(" + d3.event.scale + ")");
									}).scaleExtent([0.5, 3]);
				t.svg.call(zoom);
			}/*else{
				t.zf = 0.5;
				t.scale = 1;
	
				var zoomin = $("<button/>").addClass("zoom zoom-in").append($("<span/>").addClass("glyphicon glyphicon-zoom-in"));
				var zoomout = $("<button/>").addClass("zoom zoom-out").append($("<span/>").addClass("glyphicon glyphicon-zoom-out"));
				struct.struct.append(zoomin).append(zoomout);
	
				zoomin.click(function(e){
					t.scale = t.scale>2.5?t.scale : t.scale+t.zf;
					var x = 50;
					var y = t.container.height()/2;
					t.viewport.attr("transform", "translate("+x+","+y+")scale("+t.scale+")translate("+-x+","+-y+")");
					e.stopPropagation();
				});
	
				zoomout.click(function(e){
					t.scale = t.scale<1?t.scale : t.scale-t.zf;
					var x = 50;
					var y = t.container.height()/2;
					t.viewport.attr("transform", "translate("+x+","+y+")scale("+t.scale+")translate("+-x+","+-y+")");
					e.stopPropagation();
				});
			}*/
		}
	},
	
	render: function(){
		var t = this;
		// render entities and relations
		t.gridnode.render();
		t.createNodes();
		t.createLinks();
		t.createLinkLabels();
		t.createNodeLabels();
	},

	// parses a structure spec and recursively creates nodes and links
	parseStructure : function(parent, spec){
		var t = this;
		var id = spec["id"].replace(".?", "");
		if(spec["anomer"]){
			var node = {
				label:  id,
				parent: parent,
				anomer: spec["anomer"].replace("."," "),
				link:   spec["link"]? spec["link"] : "",
				match:  spec["match"],
				diff:   spec["diff"],
				spec:   spec,
				children: []
			};
			
			if(parent) parent.children.push(node);
			
			// remove extra information and split
			var subStr = id.replace(/[A-Z]-glycan_/g, "")
					   .replace(/core_/g, "")
					   .replace(/_([A-Z]|[0-9]+)$/g, "");
					   
			var subs = subStr.replace(/5[AG]c/g, "").match(/\d[a-zA-Z]+/g);
			if(subs != null){
				// divide substituent info into an upper and lower label
				node.upper = node.upper? node.upper : "";
				node.lower = node.lower? node.lower : "";
				for(var i=0; i<subs.length; i++){
					subStr = subStr.replace(subs[i], "");
					// divide substituents based on the linkage number
					if(subs[i].match(/\d/)[0] >= 4)
						node.upper += subs[i];
					else node.lower += subs[i];
				}
			}
			
			// match colors
			for(var color in t.stereo)
				if( t.stereo[color].test(subStr) ) node.color = color;
	
			// match shapes
			for(var shape in t.types)
				if( t.types[shape].test(subStr) ) node.shape = shape;
			
			if(node.shape == "bisecteddiamond"){
				// match rotations
				for(var rotation in t.rotations)
					if( t.rotations[rotation].test(subStr) ) node.rotation = rotation;
			}
			
			t.nodes.push(node);
			
			if(parent){
				var anomer = node.anomer;
				var link = node.link;
				if(anomer == "a") anomer = "\u03b1";
				if(anomer == "b") anomer = "\u03b2";
				var linkLabel = anomer + link;
				var link = {
					source : node,
					target : parent,
					label : linkLabel
				};
				t.links.push(link);
			}
						
			var children = spec["children"]? spec["children"]: [];
			for(var i=0; i<children.length; i++){
				t.parseStructure(node, children[i]);
			}
		}else{
			id = id.replace(/_([A-Z]|[0-9]+)$/g, "")
					 .replace(/.*_/g, "");
			var link = spec["link"]? spec["link"] : "";
			var label = link+id;
			if(link > 4){
				if(parent.upper) parent.upper += " "+label;
				else parent.upper = label;
			}else{
				if(parent.lower) parent.lower += " "+label;
				else parent.lower = label;
			}
			if(spec["match"]){
				var match = spec["match"];
				if(match != "EXACT"){
					if(match == "INEXACT"){
						if(link > 4) parent.upperColor = "#f00";
						else parent.lowerColor = "#f00";
					}else{
						if(link > 4) parent.upperColor = "#0bb";
						else parent.lowerColor = "#0bb";
					}
				}
			}
		}
	},
	
	transformLabel: function(d){
		var t = this;
		
		var x1 = d.source.x;
		var y1 = d.source.y;
		var x2 = d.target.x;
		var y2 = d.target.y;
		
		var lx = x2-x1;
		var ly = y2-y1;
		
		var tx = Math.abs(lx)/2+Math.min(x1,x2);
		var ty = Math.abs(ly)/2+Math.min(y1,y2);
		return "translate("+tx+","+ty+")";
	},
	
	createLinks: function(){
		var t = this;
		var colors = t.colors;
		var h = t.residueSize;
		
		// build links
		t.relations.append("svg:line")
			.attr("stroke", colors.relation)
			.attr("stroke-width", "2")
			.attr("x1", function(d){ return d.source.x; })
			.attr("y1", function(d){ return d.source.y; })
			.attr("x2", function(d){ return d.target.x; })
			.attr("y2", function(d){ return d.target.y; });
		
		if(t.builder){
			t.relations.on("click", function(link){
				t.builder.setSelected(link.source.spec);
				t.builder.showLinkageMenu();
				t.clearHighlights();
				t.entities.filter(function(node){ return node == link.source; })
						  .selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
						  .style("stroke-width", 3)
						  .style("stroke", colors.outline);
				d3.select(this).select("line")
							   .style("stroke-width", 4)
							   .style("stroke", colors.relation);
				d3.event.stopPropagation();
			});
		}
		
	},
	
	createLinkLabels: function(){
		var t = this;
		var colors = t.colors;
		var h = t.residueSize;
		var relLabels = t.viewport.selectAll(".link").data(t.links).enter().append("svg:g");
		relLabels.append("svg:rect")
			.attr("width", 12)
			.attr("height", 10)
			.attr("x", -6)
			.attr("y", -5)
			.attr("opacity", 0.7)
			.style("fill", "#fff");
		
		relLabels.append("svg:text")
			.attr("text-anchor", "middle")
			.attr("y", 3)
			.attr("fill", colors.text)
			.style("font", "9px Verdana")
			.text(function(d){ return d.label; });
		
		relLabels.attr("transform", function(d){return t.transformLabel(d);});
		
		if(t.builder){
			relLabels.on("click", function(link){
				t.builder.setSelected(link.source.spec);
				t.builder.showLinkageMenu();
				t.clearHighlights();
				t.entities.filter(function(node){ return node == link.source; })
						  .selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
						  .style("stroke-width", 3)
						  .style("stroke", colors.outline);
				t.relations.filter(function(lblLink){ return link == lblLink; }).select("line")
							   .style("stroke-width", 4)
							   .style("stroke", colors.relation);
				d3.event.stopPropagation();
			});
		}
	},
	
	createNodeLabels: function(){
		var t = this;
		var colors = t.colors;
		var h = t.residueSize;
		if(t.isTree){
			// create residue labels
			var entLabels = t.viewport.selectAll(".node").data(t.nodes).enter()
				.append("svg:g")
				.style("visibility", "hidden")
				.attr("transform", function(node){ 
									return "translate("+(node.x-(node.label.length*7/2)+h)+","+(node.y+7)+")"; 
								});
			
			entLabels.append("svg:rect")
				.attr("width", function(node){ return node.label.length*6; })
				.attr("height", 15)
				.attr("x", function(node){ return -11; })
				.attr("y", 3)
				.attr("rx", 4)
				.attr("ry", 4)
				.attr("opacity", 0.7)
				.style("fill", colors.text);
		
			entLabels.append("svg:text")
				//.attr("text-anchor", "middle")
				.attr("x", -7)
				.attr("y", 13.5)
				.attr("fill", "#fff")
				.style("font", "10px Trebuchet MS")
				.text(function(node){ return node.label; });
				
			t.entities.on("mouseover", function(node){
							entLabels.filter(function(lblNode){ return node == lblNode; }).style("visibility", "");
						})
					  .on("mouseout", function(node){
							entLabels.filter(function(lblNode){ return node == lblNode; }).style("visibility", "hidden");
						});
		}else{
		
			// trigger residue labels to show/hide on mouseover/mouseout
			t.entities.on("mouseover", function(node){
							node.lbl = $("<div/>").addClass("nodeLabel").html(node.label)
										.css({"top":node.y+10});
							if(node.diff){
								var diff = node.diff;
								var lbls = ["Residue Type",
											"Absolute Configuration",
											"Anomeric Configuration",
											"Ring Form",
											"Link Number"];
								node.lbl.append($("<br/>"));
								var table = $("<table/>").addClass("q-comparison");
								var tbody = $("<tbody/>");
								var header = $("<tr><td/><td>Candidate</td><td>Match</td></tr>");
								header.children("td").css("text-align","center");
								table.append(tbody.append(header));
								node.lbl.append(table);
								for(var i=0; i<diff.length; i++){
									if(diff[i] != null){
										var dspl = diff[i].split("=");
										tbody.append($("<tr/>")
												.append($("<td/>").addClass("matchCell").html(lbls[i]))
												.append($("<td/>").addClass("matchCell")
																  .addClass("matchGreen").html(dspl[1]))
												.append($("<td/>").addClass("matchCell")
																  .addClass("matchRed").html(dspl[0]))
										);
									}
								}
							}
						
							t.container.append(node.lbl);
							var width = node.lbl.width();
							var x = node.x-width/2;
							var pWidth = t.container.width();
							node.lbl.css("left", x);
						})
					  .on("mouseout", function(node){
							node.lbl.remove();
						});
		}
	},
	
	clearHighlights: function(){
		var t = this;
		var colors = t.colors;
		var h = t.residueSize;
		t.relations.select("line")
				   .style("stroke-width", 1)
				   .style("stroke", colors.relation);
		t.entities.selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
				  .style("stroke-width", 1)
				  .style("stroke", colors.outline);
	},
	
	createNodes: function(){
		var t = this;
		var colors = t.colors;
		var h = t.residueSize;
		
		// circles
		t.entities.filter(function(node){ return node.shape == "circle"; })
					.append("svg:circle")
					.attr("r", h/2+0.5)
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){ return "translate("+node.x+","+node.y+")"; });
		
		// bisected squares and bisected diamonds
		var grp = t.entities.filter(function(node){ return node.shape == "bisectedsquare" || node.shape == "bisecteddiamond"; }).append("svg:g")
		grp.append("svg:polygon")
			.attr("points", function(node){ 
								var w = node.shape == "bisectedsquare"? h : 11;
								return (-w/2+0.5)+","+(-w/2)+" "+w/2+","+-w/2+" "+(w/2)+","+(w/2-0.5);
							})
			.style("fill", function(node){ return node.color; })
			.style("stroke", colors.outline)
			.style("stroke-width", 1);
		grp.append("svg:polygon")
			.attr("points", function(node){ 
								var w = node.shape == "bisectedsquare"? h : 11;
								return (-w/2)+","+(-w/2+0.5)+" "+-w/2+","+w/2+" "+(w/2-0.5)+","+(w/2);
							})
			.style("fill", "#fff")
			.style("stroke", colors.outline)
			.style("stroke-width", 1);
		grp.attr("transform", function(node){
									var angle = node.shape == "bisectedsquare"? 0 : node.rotation;
									return "rotate("+angle+" "+node.x+","+node.y+"), translate("+node.x+","+node.y+")"; 
							});
		
		// stars
		t.entities.filter(function(node){ return node.shape == "star"; })
					.append("svg:polygon")
					.attr("points", h/2+","+0+" "+h/10+","+h/7+" "+
									h/10+","+h/2+" "+-h/7+","+h/5+" "+
									-h/2+","+h/3+" "+-h/3.5+","+0+" "+
									-h/2+","+-h/3+" "+-h/7+","+-h/5+" "+
									h/10+","+-h/2+" "+h/10+","+-h/7)
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){ return "rotate(-90 "+node.x+" "+node.y+"), translate("+node.x+","+node.y+")"; });
		
		// pentagons
		t.entities.filter(function(node){ return node.shape == "pentagon"; })
					.append("svg:polygon")
					.attr("points", h/2+","+0+" "+h/10+","+h/2+" "+-h/2+","+h/3+" "+-h/2+","+-h/3+" "+h/10+","+-h/2)
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){ return "rotate(-90 "+node.x+" "+node.y+"), translate("+node.x+","+node.y+")"; });
		
		// hexagons
		t.entities.filter(function(node){ return node.shape == "hexagon"; })
					.append("svg:polygon")
					.attr("points", h/2+","+-h/3.5+" "+h/2+","+h/3.5+" "+0+","+h/1.8+" "+-h/2+","+h/3.5+" "+-h/2+","+-h/3.5+" "+0+","+-h/1.8)
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){ return "rotate(-90 "+node.x+" "+node.y+"), translate("+node.x+","+node.y+")"; });
		
		// triangles
		t.entities.filter(function(node){ return node.shape == "triangle"; })
					.append("svg:polygon")
					.attr("points", h/2+","+0+" "+-h/2+","+h/2+" "+-h/2+","+-h/2)
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){ return "rotate("+node.angle+" "+node.x+" "+node.y+"), translate("+node.x+","+node.y+")"; });
		
		// squares and diamonds
		t.entities.filter(function(node){ return node.shape == "square" || node.shape == "diamond"; })
					.append("svg:rect")
					.attr("width", function(node){ return node.shape == "square"? h : 11; })
					.attr("height", function(node){ return node.shape == "square"? h : 11; })
					.attr("x", function(node){ return node.shape == "square"? -h/2 : -(h-4)/2; })
					.attr("y", function(node){ return node.shape == "square"? -h/2 : -(h-4)/2; })
					.style("fill", function(node){ return node.color; })
					.style("stroke", colors.outline)
					.style("stroke-width", 1)
					.attr("transform", function(node){
											var angle = node.shape == "square"? 0 : -45;
											return "rotate("+angle+" "+node.x+" "+node.y+"), translate("+node.x+","+node.y+")";
										});
										
		// create upper and lower labels
		t.entities.filter(function(node){ return node.upper != undefined; }).append("svg:text")
			.attr("text-anchor", "middle")
			.attr("y", -10)
			.attr("fill", colors.text)
			.style("font", "9px Verdana")
			.text(function(d){ return d.upper; })
			.attr("transform", function(node){ return "translate("+node.x+","+(node.y)+")"; });
		
		t.entities.filter(function(node){ return node.lower != undefined; }).append("svg:text")
			.attr("text-anchor", "middle")
			.attr("y", 10)
			.attr("fill", colors.text)
			.style("font", "9px Verdana")
			.text(function(d){ return d.lower; })
			.attr("transform", function(node){ return "translate("+node.x+","+(node.y+7)+")"; });
			
		// create root node anomer label
		t.entities.filter(function(node){ return node.parent == undefined; }).append("svg:text")
			.attr("text-anchor", "middle")
			.attr("x", 10)
			.attr("fill", colors.text)
			.style("font", "9px Verdana")
			.text(function(d){
					var anomer = d.anomer;
					if(anomer == "a") anomer = "\u03b1";
					if(anomer == "b") anomer = "\u03b2";
					return anomer; 
				  })
			.attr("transform", function(node){ return "translate("+(node.x+4)+","+(node.y+2)+")"; });
			
		// create node highlights
		t.entities.filter(function(node){ return node.match != undefined && node.match != "EXACT"; }).insert("svg:circle", ":first-child")
			.attr("r", h/2+5)
			.style("fill", "#fff") //function(node){ return node.match == "INEXACT"? "#f66" :"#6dd"; })
			.style("stroke", function(node){
									switch(node.match){
										case "HIGHLIGHT":
										case "INEXACT": return "#f00";
										default: return "#0bb";
									}
				})
			.style("stroke-width", 2)
			.style("stroke-dasharray", function(node){
									switch(node.match){
										case "HIGHLIGHT": return "2,2";
										default: return "";
									}
				})
			.attr("transform", function(node){ return "translate("+node.x+","+node.y+")"; });
			  	
		if(t.builder){
			
			t.svg.on("click", function(){
						t.clearHighlights();
						t.builder.showResidueMenu();
						t.builder.deselect();
					});
			t.svg.on("contextmenu", function(){
						t.clearHighlights();
						t.builder.showResidueMenu();
						t.builder.deselect();
						d3.event.preventDefault();
						d3.event.stopPropagation();
					});
			
			t.entities.on("click", function(node){
						t.clearHighlights();
						t.builder.showResidueMenu();
						d3.select(this).selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
									   .style("stroke-width", 3)
									   .style("stroke", colors.outline);
						t.builder.setSelected(node.spec);
						d3.event.stopPropagation();
					});
			t.entities.on("contextmenu", function(node){
						t.builder.showResidueMenu();
						t.clearHighlights();
						d3.select(this).selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
									   .style("stroke-width", 3)
									   .style("stroke", colors.outline);
						d3.event.preventDefault();
						d3.event.stopPropagation();
					});
			
			t.entities.filter(function(node){ return node.spec == t.builder.getSelected(); })
					.selectAll("circle[r=\""+(h/2+0.5)+"\"], rect, polygon")
					.style('stroke-width', 3)
					.style("stroke", colors.outline);
		}
		
	},
	
	// stereo codes for determining color
	stereo: {
		"#0000fa" : /(Glc[pf]?)|(Qui[pf]?)/,              		  	// glucose                   : blue
		"#00c832" : /(Man[pf]?)|(KDN[pf]?)|(Rha[pf]?)/,   		  	// mannose | KDN             : green
		"#ffff00" : /Gal[pf]?/,                  				  	// galactose                 : yellow
		"#ff69b4" : /Gul[pf]?/,                  				  	// gulose                    : pink
		"#faead5" : /Xyl[pf]?/,                 				  	// xylose                    : orange
		"#fa0000" : /Fuc[pf]?/,                 				  	// fucose                    : red
		"#ffffff" : /(Neu[pf]?)|(Ara[pf]?)|(Rib[pf]?)|(Tal[pf]?)/, // neuraminic acid           : white
		"#c800c8" : /Neu[pf]?.*5Ac/,            				  	// N-Acetylneuraminic acid   : purple
		"#e9ffff" : /Neu[pf]?.*5Gc/             				  	// N-Glycolylneuraminic acid : light blue
	},
	
	// type codes for determining shape
    types: {
		"circle"          : /.*/,
		"bisectedsquare"  : /N/,
		"bisecteddiamond" : /A/,
		"star"            : /Xyl[pf]?/,
		"triangle"        : /(Fuc[pf]?)|(Rha[pf]?)|(Qui[pf]?)/,
		"pentagon"        : /(Ara[pf]?)|(Rib[pf]?)/,
		"hexagon"         : /Tal[pf]?/,
		"square"          : /NAc$/,
		"diamond"         : /(Neu[pf]?)|(KDN[pf]?)/
	},
	
	rotations: {
		"-45"  : /Glc/,      // glucose
		"45"   : /Man|Gul/,  // mannose or gulose
		"-135" : /Gal/,      // galactose
	}
};