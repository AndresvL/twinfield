$(document).ready(function() {
			<!-- reload page every 15 - 20 min -->
			setTimeout(function(){
				  location.reload();
			  },880000)
		
			var modal = document.getElementById('loginModal');
			if ($('#client').val() == "" || $('#client').val() == null) {
				modal.style.display = "block";
			}else {
				if ($('#saved').val() !== "") {
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
				alert($(this).data("href"));
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
					  title: 'Even geduld...',
					  text: 'Data Synchroniseren',
					  imageUrl: 'WBA.png',
					  imageWidth: 250,
					  imageHeight: 220,
					  showConfirmButton: false,
					  timer: 500
					  
					}).then(
					  function () {
						  $( "#sync" ).submit();
					  },
					  function (dismiss) {
					    if (dismiss === 'timer') {
					    	 $( "#sync" ).submit();
					    }
					  })
					
				});
		$("#officelist").change(function() {
			$("#officeExportList").val($("#officelist").val());
			$("#exportOfficeValue").val($("#officelist").val());
		});