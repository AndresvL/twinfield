<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Twinfield Connection</title>

<!-- Bootstrap Core CSS -->
<link href="vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

<!-- Theme CSS -->
<link href="css/freelancer.min.css" rel="stylesheet">

<!-- Custom Fonts -->
<link href="vendor/font-awesome/css/font-awesome.min.css"
	rel="stylesheet" type="text/css">
<link href="https://fonts.googleapis.com/css?family=Montserrat:400,700"
	rel="stylesheet" type="text/css">
<link
	href="https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic"
	rel="stylesheet" type="text/css">

<!-- Sweet Alert -->
<script src="sweetalert2/sweetalert2.min.js"></script>
<link rel="stylesheet" href="sweetalert2/sweetalert2.min.css">


<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
		        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
		        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
		    <![endif]-->

<style>
.settings {
	width: 50%;
	float: left;
}

#help {
	float: right;
}

.image img {
	-webkit-transition: all 1s ease; /* Safari and Chrome */
	-moz-transition: all 1s ease; /* Firefox */
	-ms-transition: all 1s ease; /* IE 9 */
	-o-transition: all 1s ease; /* Opera */
	transition: all 1s ease;
}

.image:hover img {
	-webkit-transform: scale(1.90); /* Safari and Chrome */
	-moz-transform: scale(1.90); /* Firefox */
	-ms-transform: scale(1.90); /* IE 9 */
	-o-transform: scale(1.90); /* Opera */
	transform: scale(1.90);
}
</style>
</head>

<body>
	<!-- Settings Section -->
	<div class="settings" style="display: none;">
		<div class="panel-group">
			<!-- The Modal -->
			<div id="myModal" class="modal">
				<!-- Modal content -->
				<div class="modal-content">
					<div class="modal-header">
						<span class="close">&times;</span>
						<h2>Instructies</h2>
					</div>
					<div class="modal-body">
						<h3>Welkom bij de Twinfield koppeling</h3>
						<br />
						<p>Informatie</p>
						Op deze pagina is het mogelijk om de import en export gegevens tussen WerkbonApp en Twinfield in te stellen.<br />
						Elke 15 minuten zal er een automatische synchronisatie plaatsvinden aan de hand van deze instellingen.<br />
						Het is ook mogelijk om handmatig een synchronisatie uit te voeren door onderaan op de knop synchronize te klikken.<br />
						<br />
						<p>Data Mapping</p>
						<table class="table table-hover">
							<thead>
								<tr>
									<th>WerkbonApp</th>
									<th>Twinfield</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<th colspan="2">Import</th>
								</tr>
								<tr>
									<td>Medewerkers</td>
									<td>Gebruikers</td>
								</tr>
								<tr>
									<td>Projecten</td>
									<td>Projecten</td>
								</tr>
								<tr>
									<td>Materialen</td>
									<td>Artikelen</td>
								</tr>
								<tr>
									<td>Relaties</td>
									<td>Debiteuren</td>
								</tr>
								<tr>
									<td>Urensoorten</td>
									<td>Aktiviteiten</td>
								</tr>
								<tr>
									<th colspan="2">Export</th>
								</tr>

								<tr>
									<td>Werkbon (zonder projectNr)</td>
									<td>Factuur</td>
								</tr>
								<tr>
									<td>Werkbon (met projectNr)</td>
									<td>Urenboeking</td>
								</tr>
							</tbody>
						</table>
						<p>In Twinfield moeten de volgende gebruikersrollen worden
							toegevoegd</p>
						<div class="image">
							<img src="./img/rol.png" width="100%" height="100%"
								title="Ga naar Projecten > Uren & onkosten > Tarieven & toegang" />
						</div>
						<br />
						<p>
							<b>Let op! Twinfield is leidend. <br> De import settings mogen alleen in Twinfield worden gewijzigd</b>
						</p>

					</div>
					<div class="modal-footer">
						<h3>WorkOrderApp B.V.</h3>
					</div>
				</div>

			</div>
			<input type="hidden" value="${session}" id="session" /> <input
				type="hidden" value="${error}" id="error" />
			<form action="settings.do" id="save">
				<div class="panel panel-success">
					<div class="panel-heading" title="test">Import settings</div>
					<div class="panel-body">
						<div class="row control-group">
							<div class="form-group col-xs-12 floating-label controls">
							<input type="hidden" value="${softwareName}" name="softwareName" /> 
								<label>Office</label> <img src="./img/vraagteken.jpg"
									title="Choose an administration" height="13" width="13" /> <select
									name="offices" class="form-control" id="officelist" required>
									<option>-- Select an office --</option>
									<c:forEach items="${offices}" var="office">
										<option value="${office.code}"
											${office.code == importOffice ? 'selected="selected"' : ''}>
											${office.name}</option>
									</c:forEach>
								</select><br> <label>Select objects for import</label> <img
									src="./img/vraagteken.jpg"
									title="Select the objects you want to import from Twinfield into WorkOrderApp"
									height="13" width="13" /><br /> Saved imports: <b><c:forEach
										items="${checkboxes}" var="checkboxs">${checkboxs}, </c:forEach></b>
								<%-- <input type="text" id="checkbox" name="checkbox" value="${checkboxes}" /> <input
									type="text" id="oldCheckbox" value="${oldCheckboxes}" /> --%>
								<div class="checkbox">
									<label><input type="checkbox" value="employees"
										name="importType" id="employees">Employees</label>
								</div>
								<div class="checkbox">
									<label><input type="checkbox" value="projects"
										name="importType" id="projects">Projects</label>
								</div>
								<div class="checkbox">
									<label><input type="checkbox" value="materials"
										name="importType">Materials</label>
								</div>
								<div class="checkbox">
									<label><input type="checkbox" value="relations"
										name="importType">Relations</label>
								</div>
								<div class="checkbox">
									<label><input type="checkbox" value="hourtypes"
										name="importType">Hourtypes</label>
								</div>
								<input type="button" value="Help" id="help"
									class="btn btn-success btn-lg" />
							</div>

						</div>
					</div>
				</div>
				<div class="panel panel-success">
					<div class="panel-heading">Export settings</div>
					<div class="panel-body">
						<div class="row control-group">
							<input type="hidden" value="${errorExport}" id="errorExport" />
							<div class="form-group col-xs-12 floating-label controls">
								<label>Office</label> <img src="./img/vraagteken.jpg"
									title="Choose an administration" height="13" width="13" /> <select
									name="exportOffices" class="form-control" id="officeExportList"
									required>
									<option>-- Select an office --</option>
									<c:forEach items="${offices}" var="office">
										<option value="${office.code}"
											${office.code == exportOffice ? 'selected="selected"' : ''}>${office.name}</option>
									</c:forEach>
								</select>
							</div>
							<div class="form-group col-xs-12 floating-label controls">
								<label>Werkbontype</label> <img src="./img/vraagteken.jpg"
									title="Choose the status of the workorder you want to export"
									height="13" width="13" /><input class="form-control"
									type="text" disabled value="Compleet" /> <input
									class="form-control" type="hidden" name="factuurType"
									value="Compleet" />

							</div>
						</div>
						<br>
						<div id="success"></div>
						<div class="row">
							<div class="form-group col-xs-12">
								<input type="submit" class="btn btn-success btn-lg" value="Save"
									name="category" />
							</div>
						</div>
					</div>
				</div>
			</form>
			<form action="sync.do" id="sync">
				<div class="panel panel-success">
					<div class="panel-heading">Manually synchronize</div>
					<div class="panel-body">
						<div class="row control-group">
							<div class="form-group col-xs-12 floating-label controls">
								<div id="success"></div>
								<div class="row">
									<div class="form-group col-xs-12">
										<input type="hidden" value="${softwareToken}" name="token" />
										<input type="submit" class="btn btn-success btn-lg"
											value="Synchronize" />
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>

	</div>
	<div class="settings" style="display: none;">
		<div class="panel panel-success">
			<div class="panel-heading">Logs</div>
			<div class="panel-body">
				<div class="row control-group">
					<div class="form-group col-xs-12 floating-label controls">

						<table class="table table-hover">
							<thead>
								<tr>
									<th>Timestamp</th>
									<th>Log</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${logs}" var="log">
									<tr>
										<td>${log.timestamp}</td>
										<td>${log.message}</td>
									</tr>
								</c:forEach>

							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- jQuery -->
	<script src="vendor/jquery/jquery.min.js"></script>

	<!-- Bootstrap Core JavaScript -->
	<script src="vendor/bootstrap/js/bootstrap.min.js"></script>

	<!-- Plugin JavaScript -->
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js"></script>

	<!-- Contact Form JavaScript -->
	<script src="js/jqBootstrapValidation.js"></script>
	<script src="js/contact_me.js"></script>

	<!-- Theme JavaScript -->
	<script src="js/freelancer.min.js"></script>
	<script>
		$(document).ready(function() {
			var modal = $('#checkbox').val();
			if ($('#session').val() == "" || $('#session').val() == null) {
				if ($('#error').val() != null) {
					swal({
						title : 'Warning',
						text : $('#error').val(),
						type : 'error'
					})
				} else {
					swal({
						title : 'Warning',
						text : 'You are not connected',
						type : 'error'
					})
				}

			} else {
				$('.settings').show();
				/* if ($('#checkbox').val() != $('#oldCheckbox').val()) {
					swal({
						title :'Success',
						text : 'Settings are save!',
						type : 'success'
					})
				} */
			}
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
		$("#save").submit(
				function(event) {
					/* var text = $('#checkbox').val();
					$('#oldCheckbox').val(text);				 */
					if ($('#officeExportList').val() == null
							|| $('#officelist').val() == null) {
						event.preventDefault();
						swal({
							title : 'Warning',
							text : 'No administration selected',
							type : 'error'
						})
					}
				});
		$("#sync").submit(
				function(event) {
					if ($('#officeExportList').val() == null
							|| $('#officelist').val() == null) {
						event.preventDefault();
						swal({
							title : 'Warning',
							text : 'No administration selected',
							type : 'error'
						})
					}
				});
	</script>
</body>

</html>

