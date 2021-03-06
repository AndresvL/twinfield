$(document).ready(function() {
	  		$('[data-toggle="tooltip"]').tooltip(); 
	  		
			<!-- reload page every 1,5 - 2 min -->
			setTimeout(function(){
				  location.reload();
			  },300000)
			// Date
		    $('#datetimepicker1').datetimepicker({ format:'DD-MM-YYYY HH:mm:ss'
		    });
			
			var modal = document.getElementById('loginModal');
			if ($('#loggedIn').val() === "false" || $('#loggedIn').val() === null || $('#loggedIn').val() === "") {
				modal.style.display = "block";
			}else {
				if($('#loggedIn').val() === "true"){
					swal({
						title : 'Success',
						text : "Je bent ingelogd",
						type : 'success'
					})
					$('#loggedIn').val("pending");
				}
				if ($('#saved').val() !== "" && $('#saved').val() !== false) {
					swal({
						title : 'Success',
						text : $('#saved').val(),
						type : 'success'
					})
				}
				$('#saved').val("false");
			}
			if(!$('#verkooporders').attr('checked')){
				 $('#verkooporders_extra').hide();
			}
		    $('#verkooporders').on('change', function() {
			    $('#verkooporders_extra').toggle(this.checked);
		    })
			
			
			$(".showDetails").click(function() {
				var errorDetails = $(this).data("href");
				if(errorDetails !== null && errorDetails !== true && errorDetails !== false && errorDetails !== ""){
					alert(errorDetails);
				} 
			});
		});
		$("#show").click(function() {
			$("#mappingTable").toggle();
		});
		$("#help").click(function() {
			var modal = document.getElementById('myModal');
			modal.style.display = "block";
			// Get the <span> element that closes the modal
			var span = document.getElementsByClassName("close")[0];
			// When the user clicks on <span> (x), close the modal
			span.onclick = function() {
				modal.style.display = "none";
			}

			// When the user clicks anywhere outside of the modal, close it
			window.onclick = function(event) {
				if (event.target == modal) {
					modal.style.display = "none";
				}
			}
		});
		$("#syncbutton").click(
				function(event) {
					
				event.preventDefault();
				swal({
					  title: 'Synchronizing...',
					  text: 'Synchronisation takes place in the background. Please come back in a moment',
					  imageUrl: 'WBA.png',
					  imageWidth: 250,
					  imageHeight: 220,
					  showConfirmButton: true					  
					}).then(
					  function () {
						  $( "#sync" ).submit();
					  })
					
				});