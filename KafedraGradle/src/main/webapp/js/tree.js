var globalSelectedLoadID = 0;
var globalIsMulti = false;
var globalStreamID = 0;
var globalDiscID = 0;

$.ajaxSetup ({
	// Disable caching of AJAX responses
	cache: false
});

$(function() {
	$('.tree a').on('click', function(event) {
		event.preventDefault();
		$ul = $(this).siblings('ul');
		if ($ul.css('display') == 'none')
			ulOpen($ul);
		else
			ulClose($ul);
	});
	
	
	$( "#showChecked" ).click(function( event ) {			
		$(".cb:checked").parent().find('ul').each(function(){
			ulOpen($(this));
		});
	});
	
	$( "#hideChecked" ).click(function( event ) {			
		$(".cb:checked").parent().find('ul').each(function(){
			ulClose($(this));			
		});
	});
	
	function ulOpen($ul){
		$ul.show();
		$ul.siblings(".isExpandable").children(".plus").attr("src","../treeImg/minus.jpg");
	}
	
	function ulClose($ul){
		$ul.hide();
		$ul.siblings(".isExpandable").children(".plus").attr("src","../treeImg/plus.jpg");
	}
});

function toggle(X){
}

//Set teacher's load info by ajax-GET query
(function ($) {
	 
    $.postJSON = function (url, data) {
 
        var o = {
            url: url,
            type: "POST",
            dataType: "json",
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            data : data,
        };
 
        return $.ajax(o);
    };
 
} (jQuery));

function refreshBarCharts() {
	//setTimeout(1000, function() {
		$('#percent').find('img').attr('src', '../PercentBarChart?'+Math.random());
		$('#time').find('img').attr('src', '../HoursBarChart?'+Math.random());
	//});	
}

function ajaxAppoint(teacher_id, jsonGroups) {
		if (jsonGroups == null) {
			alert("Группа не выбрана!");
			return;
		}
				
		var jsonString = $.toJSON(jsonGroups);
		
		$.postJSON("../AppointLoadTo", {"data": jsonString, "teacher_id": teacher_id, "random" : Math.random()*99999}).done(function( response ) {
			if (response.success) {
				$('#progressbar').trigger('refresh');
				
				// Repaint appointed items
				response.appointed.forEach(function(entry) {
					if (response.teacher != "") 
						$('#' + entry).children('a').removeClass('notappointed').addClass('appointed');
					else
						$('#' + entry).children('a').removeClass('appointed').addClass('notappointed');
					
					$('#' + entry).children('a').attr('teacher', response.teacher);
				});
				
				paintStart();
				
				// Refresh group card
				$(".cb:last").trigger('change');	
			} else {
				alert(response.reason + " - error!");
			}
			
			refreshBarCharts();
		}).fail(function( jqxhr, textStatus, error ) {
			var err = textStatus + ", " + error;
			alert("Request Failed: " + err);
		});
	}

