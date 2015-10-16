var currentSelImage;

function makeHorizontalScroll(hrf){
	//Get our elements for faster access and set overlay width
	var div = $('div.sc_menu'),
		ul = $('ul.sc_menu'),
		ulPadding = 15;
	
	//Get menu width
	var divWidth = div.width();

	//Remove scrollbars	
	div.css({overflow: 'hidden'});
	
	//Find last image container
	var lastLi = ul.find('li:last-child');
	
	//When user move mouse over menu
	div.mousemove(function(e){
		//As images are loaded ul width increases,
		//so we recalculate it each time
		var ulWidth = lastLi[0].offsetLeft + lastLi.outerWidth() + ulPadding;	
		var left = (e.pageX - div.offset().left) * (ulWidth-divWidth) / divWidth;
		div.scrollLeft(left);
	});
	
	$('.sc_menu a').click(function(e){
		e.preventDefault();
		var hrf = $(e.target).parent().attr('href');
		getImage(hrf);
		
	});
	if(hrf != null)
		getImage(hrf);
};

function getImage(hrf){
	$.getJSON(hrf, function(data){		
		$('.projectImageDesc2').html('<h2>'+data.data.name+'</h2>'+data.data.description);
		$('.projectImage').attr('src', data.data.fileName);
		currentSelImage = data.data;
	});
}



function makeScrollable(wrapper, scrollable){
	// Get jQuery elements
	var wrapper = $(wrapper), scrollable = $(scrollable);
	
	// Hide images until they are not loaded
	scrollable.hide();
	var loading = $('<div class="loading">Loading...</div>').appendTo(wrapper);
	
	// Set function that will check if all images are loaded
	var interval = setInterval(function(){
		var images = scrollable.find('img');
		var completed = 0;
		
		// Counts number of images that are succesfully loaded
		images.each(function(){
			if (this.complete) completed++;	
		});
		
		if (completed == images.length){
			clearInterval(interval);
			// Timeout added to fix problem with Chrome
			setTimeout(function(){
				
				loading.hide();
				// Remove scrollbars	
				wrapper.css({overflow: 'hidden'});						
				
				scrollable.slideDown('slow', function(){
					enable();	
				});					
			}, 1000);	
		}
	}, 100);
	
	function enable(){
		// height of area at the top at bottom, that don't respond to mousemove
		var inactiveMargin = 99;					
		// Cache for performance
		var wrapperWidth = wrapper.width();
		var wrapperHeight = wrapper.height();
		// Using outer height to include padding too
		var scrollableHeight = scrollable.outerHeight() + 2*inactiveMargin;
		// Do not cache wrapperOffset, because it can change when user resizes window
		// We could use onresize event, but it's just not worth doing that 
		// var wrapperOffset = wrapper.offset();
		
		// Create a invisible tooltip
		var tooltip = $('<div class="sc_menu_tooltip"></div>')
			.css('opacity', 0)
			.appendTo(wrapper);
	
		// Save menu titles
		scrollable.find('a').each(function(){				
			$(this).data('tooltipText', this.title);				
		});
		
		// Remove default tooltip
		scrollable.find('a').removeAttr('title');		
		// Remove default tooltip in IE
		scrollable.find('img').removeAttr('alt');	
		
		var lastTarget;
		//When user move mouse over menu			
		wrapper.mousemove(function(e){
			// Save target
			lastTarget = e.target;

			var wrapperOffset = wrapper.offset();
		
			var tooltipLeft = e.pageX - wrapperOffset.left;
			// Do not let tooltip to move out of menu.
			// Because overflow is set to hidden, we will not be able too see it 
			tooltipLeft = Math.min(tooltipLeft, wrapperWidth - 75); //tooltip.outerWidth());
			
			var tooltipTop = e.pageY - wrapperOffset.top + wrapper.scrollTop() - 40;
			// Move tooltip under the mouse when we are in the higher part of the menu
			if (e.pageY - wrapperOffset.top < wrapperHeight/2){
				tooltipTop += 80;
			}				
			tooltip.css({top: tooltipTop, left: tooltipLeft});				
			
			// Scroll menu
			var top = (e.pageY -  wrapperOffset.top) * (scrollableHeight - wrapperHeight) / wrapperHeight - inactiveMargin;
			if (top < 0){
				top = 0;
			}			
			wrapper.scrollTop(top);
		});
		
		// Setting interval helps solving perfomance problems in IE
		var interval = setInterval(function(){
			if (!lastTarget) return;	
										
			var currentText = tooltip.text();
			
			if (lastTarget.nodeName == 'IMG'){					
				// We've attached data to a link, not image
				var newText = $(lastTarget).parent().data('tooltipText');

				// Show tooltip with the new text
				if (currentText != newText) {
					tooltip
						.stop(true)
						.css('opacity', 0)	
						.text(newText)
						.animate({opacity: 1}, 1000);
				}					
			}
		}, 200);
		
		// Hide tooltip when leaving menu
		wrapper.mouseleave(function(){
			lastTarget = false;
			tooltip.stop(true).css('opacity', 0).text('');
		});
		
		$('.sc_menu_wrapper a').click(function(e){
			e.preventDefault();
			var hrf = $(e.target).parent().attr('href');
			$.getJSON(hrf, function(data){
				$('.projectImageDesc2').html('<h2>'+data.name+'</h2>'+data.description);
				$('.projectImage').attr('src', data.fileName);
			});
			
		});
		
		/*
		//Usage of hover event resulted in performance problems
		scrollable.find('a').hover(function(){
			tooltip
				.stop()
				.css('opacity', 0)
				.text($(this).data('tooltipText'))
				.animate({opacity: 1}, 1000);
	
		}, function(){
			tooltip
				.stop()
				.animate({opacity: 0}, 300);
		});
		*/			
	}
}
