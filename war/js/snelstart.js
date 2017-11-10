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
			if ($('#client').val() === "" || $('#client').val() === null) {
				modal.style.display = "block";
			}else {
				if($('#client').val() === "true"){
					swal({
						title : 'Success',
						text : "Je bent ingelogd",
						type : 'success'
					})
					$('#client').val("pending");
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
					if($("#status").val() === "error"){
						swal({
							  title: 'Let op',
							  text: 'Werkbonstatus staat op Error. Hierdoor zullen geen werkbonnen gesynchroniseerd worden',
							  imageUrl: 'WBA.png',
							  imageWidth: 250,
							  imageHeight: 220,
							  showConfirmButton: true,
							  showCancelButton: false,
							  cancelButtonText: 'Verander status'
							}).then(
							  function () {
								  $( "#sync" ).submit();
							  }, function (dismiss) {
						  // dismiss can be 'cancel', 'overlay',
						  // 'close', and 'timer'
						  if (dismiss === 'cancel') {
						    $('#status').val("compleet");
						    $( "#saveSnelStart" ).submit();
						  }else {
							  
						  }						  
						})
					}
					
					else{
				swal({
					  title: 'Synchroniseren',
					  text: 'Op de achtergrond zal de synchronisatie plaatsvinden. Kom over een paar minuten terug',
					  imageUrl: 'WBA.png',
					  imageWidth: 250,
					  imageHeight: 220,
					  showConfirmButton: true
					}).then(
					  function () {
						  $( "#sync" ).submit();
					  }, function (dismiss) {
						  // dismiss can be 'cancel', 'overlay',
						  // 'close', and 'timer'
						  if (dismiss === 'overlay') {
						  }
					})
					}
				});
				
		$(".exportRelation").on('change', function() {
			if ($("input[name='exportRelations']:checked").val() == "yes") {
				$('#relations').attr('checked', true);
			}
		});