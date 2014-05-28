$.extend( $.fn.dataTableExt.oStdClasses, {
	"sWrapper": "dataTables_wrapper form-inline"
    //"sSortAsc": "header headerSortDown",
    //"sSortDesc": "header headerSortUp",
    //"sSortable": "header"
} );

$.fn.dataTableExt.oApi.fnAddTr = function ( oSettings, nTr, bRedraw ) {
    if ( bRedraw === undefined ){
        bRedraw = false;
    }
      
    var nTds = nTr.getElementsByTagName('td');
    if ( nTds.length != oSettings.aoColumns.length ){
        alert( 'Warning: not adding new TR - columns and TD elements must match' );
        return;
    }
      
    var aData = [];
    for ( var i=0 ; i<nTds.length ; i++ ){
        aData.push( nTds[i].innerHTML );
    }
      
    /* Add the data and then replace DataTable's generated TR with ours */
    var iIndex = this.oApi._fnAddData( oSettings, aData );
    nTr._DT_RowIndex = iIndex;
    oSettings.aoData[ iIndex ].nTr = nTr;
      
    oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
      
    if ( bRedraw ){
        this.oApi._fnReDraw( oSettings );
    }
};

$.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
{
    return {
        "iStart":         oSettings._iDisplayStart,
        "iEnd":           oSettings.fnDisplayEnd(),
        "iLength":        oSettings._iDisplayLength,
        "iTotal":         oSettings.fnRecordsTotal(),
        "iFilteredTotal": oSettings.fnRecordsDisplay(),
        "iPage":          oSettings._iDisplayLength === -1 ?
            0 : Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
        "iTotalPages":    oSettings._iDisplayLength === -1 ?
            0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
    };
}
 
/* Bootstrap style pagination control */
$.extend( $.fn.dataTableExt.oPagination, {
    "bootstrap": {
        "fnInit": function( oSettings, nPaging, fnDraw ) {
            var oLang = oSettings.oLanguage.oPaginate;
            var fnClickHandler = function ( e ) {
                e.preventDefault();
                if ( oSettings.oApi._fnPageChange(oSettings, e.data.action) ) {
                    fnDraw( oSettings );
                }
            };
 
            $(nPaging).addClass('pagination').append(
                '<ul>'+
                    '<li class="prev disabled"><a href="#">&laquo;</a></li>'+
                    '<li class="next disabled"><a href="#">&raquo;</a></li>'+
                '</ul>'
            );
            var els = $('a', nPaging);
            $(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
            $(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
        },
 
        "fnUpdate": function ( oSettings, fnDraw ) {
            var iListLength = 5;
            var oPaging = oSettings.oInstance.fnPagingInfo();
            var an = oSettings.aanFeatures.p;
            var i, j, sClass, iStart, iEnd, iHalf=Math.floor(iListLength/2);
 
            if ( oPaging.iTotalPages < iListLength) {
                iStart = 1;
                iEnd = oPaging.iTotalPages;
            }
            else if ( oPaging.iPage <= iHalf ) {
                iStart = 1;
                iEnd = iListLength;
            } else if ( oPaging.iPage >= (oPaging.iTotalPages-iHalf) ) {
                iStart = oPaging.iTotalPages - iListLength + 1;
                iEnd = oPaging.iTotalPages;
            } else {
                iStart = oPaging.iPage - iHalf + 1;
                iEnd = iStart + iListLength - 1;
            }
 
            for ( i=0, iLen=an.length ; i<iLen ; i++ ) {
                // Remove the middle elements
                $('li:gt(0)', an[i]).filter(':not(:last)').remove();
 
                // Add the new list items and their event handlers
                for ( j=iStart ; j<=iEnd ; j++ ) {
                    sClass = (j==oPaging.iPage+1) ? 'class="active"' : '';
                    $('<li '+sClass+'><a href="#">'+j+'</a></li>')
                        .insertBefore( $('li:last', an[i])[0] )
                        .bind('click', function (e) {
                            e.preventDefault();
                            oSettings._iDisplayStart = (parseInt($('a', this).text(),10)-1) * oPaging.iLength;
                            fnDraw( oSettings );
                        } );
                }
 
                // Add / remove disabled classes from the static elements
                if ( oPaging.iPage === 0 ) {
                    $('li:first', an[i]).addClass('disabled');
                } else {
                    $('li:first', an[i]).removeClass('disabled');
                }
 
                if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
                    $('li:last', an[i]).addClass('disabled');
                } else {
                    $('li:last', an[i]).removeClass('disabled');
                }
            }
        }
    }
} );


function dialog(obj, attrbs, icon, buttons){
	var icon = $("<span/>").addClass("ui-icon").css("float","left");
	icon.addClass("ui-icon-"+icon);
	
	$(obj).dialog({
				 title: $("<span/>").append(icon).append("&nbsp;&nbsp;&nbsp;"+title),
				 modal: attrbs.modal? attrbs.modal: true,
				 width: attrbs.width? attrbs.width: 200,
				 height: attrbs.height? attrbs.height: 100,
				 resizable: attrbs.resizable? attrbs.resizable: false,
				 buttons: buttons
			   });
}

function selectbox(input, icon, action){
	var container = $("<div/>").addClass("input-append");
	var button = $("<button/>")
					.addClass("btn")
					.append($("<i class='"+icon+"'/>"))
					.click(action);
	
	container.append(input).append(button);
	
	return container;
}

function combobox(title, options){
	var container = $("<div/>").addClass("input-prepend");
	var input = $("<input type='text'/>");
	var group = $("<div/>").addClass("btn-group");
	var button = $("<button data-toggle='dropdown' data-target='#'/>")
					.addClass("btn dropdown-toggle")
					.html(title).append($("<span class='caret'>"));
	var optList = $("<ul class='dropdown-menu'/>").css( {"max-height": "130px", "overflow":"auto"} );
	
	$.each(options, function(key, value){
		var link = $("<a href='#'>"+value+"</a>")
						.click(function(e){
							input.val(value);
							button.dropdown("toggle");
						});
		var option = $("<li/>").append(link);
		optList.append(option);
	});
	
	container.append(group.append(button)
			 			  .append(optList))
			 .append(input);
	button.dropdown();
	
	return container;
}

function popover(elmt, title, placement, content, funcs){
	
	elmt.popover("destroy");
	//elmt.removeData("popover");
	
	// create the popover container
	var createContainer = function(){
		var container = $("<div/>").append(content);
		$.each( funcs, function(key, value){
			var button = $("<button>"+key+"</button>")
							.addClass("btn btn-default")
							.click(function(e) {
								funcs[key](e);
								elmt.popover("destroy");
							  });
			container.append(button);
			container.append("&nbsp;");
		});
		return container;
	};
		
	// activate the popover
	elmt.popover({ placement: placement,
				   html: true,
				   content: createContainer,
				   title: "<h5>"+title+"</h5>",
				   trigger: "manual" })
			.popover("show");
	
	var ppvr = $(".popover");
			
	ppvr.click(function(e){ e.stopPropagation(); });
}

function modal(elmt, title, funcs, width, keep){
	
	var container = $("<div/>").addClass("modal fade");
	var dialog = $("<div/>").addClass("modal-dialog");
	var content = $("<div/>").addClass("modal-content");
	var header = $("<div/>").addClass("modal-header");
	var body = $("<div/>").addClass("modal-body");
	var footer = $("<div/>").addClass("modal-footer");
	
	var modal = {
		container: container,
		dialog: dialog,
		content: content,
		header: header,
		body: body,
		footer: footer,
		
		generateButtons: function(buttons){
			$.each( buttons, function(key, value){
				var button = $("<button data-dismiss='modal'>"+key+"</button>")
								.addClass("btn btn-default")
								.click(function(e) {
									value(e);
								});
				footer.append(button);
			});
		},
		
		setWidth: function(width){
			dialog.css("width", width);
			/*dialog.css( {"top": "50%", 
							"left": "50%", 
							"margin-left": -container.width()/2+"px", 
							"margin-top": -container.height()/2+"px"} );*/
		},
		setTitle: function(title){
			this.header.find("h3").html(title);
		}
	};
	var title = $("<h3>"+title+"</h3>").addClass("modal-title");
	header.append($("<button type=\"button\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>").addClass("close"))
		  .append(title);
	
	body.append(elmt);
	modal.generateButtons(funcs);
	
	container.append(dialog.append(content.append(header).append(body).append(footer)));
	if(width) modal.setWidth(width);
	// activate the modal
	container.modal({backdrop:"static", keyboard:false});
	if(!keep){
		container.on('hidden.bs.modal', function (e) {
			container.remove();
		});
	}
		
	return modal;
}

function menu(options){
	var menuContainer = $("<ul/>").addClass("contextMenu ui-corner-all")
								  .fadeTo(0, 0.9);
	
	$.each(options, function(key, val){
		var optContainer = $("<li/>").html(key)
									 .addClass("menuOption")
									 .click( function(){ menuContainer.remove(); val(); });
		menuContainer.append(optContainer);
	});
	return menuContainer;
}

function buildTable(content, headings) {
    var table = $("<table/>");
    var thead = $("<thead/>");
    var tbody = $("<tbody/>");
    var tr = $("<tr/>");
    table.append(thead.append(tr)).append(tbody);
    for (var i = 0; i < headings.length; i++) {
        tr.append($("<th>" + headings[i] + "</th>"));
    }
    content.empty().append(table);
	var dTable = table.dataTable({
    		"bLengthChange": false,
    		"sDom": "<l<'spc_rgt'f>r>t<<'tbl_foot'<'spc_lft'i><'spc_rgt'p>>>",
    		"sPaginationType": "bootstrap",
            "bPaginate": false,
            "sScrollY": "100%"
        });
    return dTable;
}