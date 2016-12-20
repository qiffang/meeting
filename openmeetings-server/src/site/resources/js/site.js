/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
    	  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  
*/
function getMenu(a) {
	return a.parent().children('ul:first');
}

$(document).ready(function() {

	console.log($(".example-image-link"));

	$(".example-image-link").click(
		function(e) {
			e.preventDefault();
			var addressValue = $(this).attr("href");
			console.log("addressValue="+addressValue);

			var overLay = '<div id="overlay"><div class="bg"></div><img src="'+addressValue+'"></div>';

			var overlay = jQuery(overLay);
			overlay.appendTo(document.body);

			$("#overlay").click(
				function(e) {
					$("#overlay").remove();
				}
			);

		}
	);

	$('ul.nav.nav-list > li a').each(function() {
		var a = $(this);
		var m = getMenu(a);
		if (m.length) {
			a.addClass('nav-header')
			a.attr('data-toggle', 'collapse');
			a.click(function(){
				var a = $(this);
				var v = getMenu(a).height() != 0;
				a.children('i').removeClass(v ? 'icon-chevron-down' : 'icon-chevron-right').addClass(v ? 'icon-chevron-right' : 'icon-chevron-down');
			})
			m.attr('id', a.attr('href').substr(1));
			if (!m.find('li.active').length) {
				m.collapse('hide');
				a.children('i').removeClass('icon-chevron-down').addClass('icon-chevron-right');
			}
		}
	});
	var c = $('#slider');
	if (c.length) {
		/* FIXME uncomment later
		var items = $('.carousel-inner .item', c);
		var ol = $('<ol>').addClass('carousel-indicators');
		for (var i = 0; i < items.length; ++i) {
			var li = $('<li>').attr('data-target', '#slider').attr('data-slide-to', i);
			if ($(items[i]).hasClass('active')) {
				li.addClass('active');
			}
			ol.append(li);
		}
		c.prepend(ol);
		*/
		$('.carousel-control.left', c).attr('data-slide', 'prev');
		$('.carousel-control.right', c).attr('data-slide', 'next');
		c.attr('data-ride', 'carousel').addClass('carousel slide');
	}
	// "New" markers
	var topics = ["Call For Logo"];
	for (var i = 0; i < topics.length; ++i) {
		$('ul.nav.nav-list ul.nav.nav-list li:contains("' + topics[i] + '") a').append('&nbsp;&nbsp;<span class="label label-success">New</span>');
	}
})
