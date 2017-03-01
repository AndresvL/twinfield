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

<title>WeFact Connection</title>

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
<script>
	function checkSession() {
		//Get the modal
		var modal = document.getElementById('myModal');

		if ($('#client').val() == "" || $('#client').val() == null) {
			modal.style.display = "block";
		}
	}
</script>
<style>

.settings {
	width: 50%;
	height: 100%;
	float: left;
}

.log {
	width: 50%;
	height: 100%;
	float: right;
}
</style>
</head>

<body onload="return checkSession();">
	<!-- Settings Section -->
	<div id="float">
		<div class="settings">
			<div class="panel-group">
				<!-- The Modal -->
				<div id="myModal" class="modal">
					<!-- Modal content -->
					<div class="modal-content">
						<form action="OAuth.do">
							<input type="hidden" value="${softwareToken}" name="token"
								id="softwareToken" /> <input type="hidden" value="${softwareName}" name="softwareName"
								id="softwareToken" /> <input type="hidden"
								value="${clientToken}" id="client" />
							<table>
								<tr>
									<td>
										<h2>Authentication</h2>
									</td>
								</tr>
								<tr>
									<td><label>${errorMessage}</label></td>
								</tr>
								<tr>
									<td><label>Securitycode WeFact</label> <img
										src="./img/vraagteken.jpg"
										title="Login to WeFact and navigate to Instellingen > API > beveiligingscode"
										height="13" width="13" /></td>
								</tr>
								<tr>
									<td><input type="text" class="form-control"
										id="clientToken"
										placeholder="example: 10b566e8fe030c6d083ebee7d043757f"
										value="10b566e8fe030c6d083ebee7d043757f" name="clientToken"
										required /></td>
								</tr>
							</table>
							<br /> <input type="submit" value="Submit"
								class="btn btn-success btn-lg" />
						</form>
					</div>

				</div>

				<form action="settings.do">
					<div class="panel panel-success">
						<div class="panel-heading">Import settings</div>
						<div class="panel-body">
							<div class="row control-group">
								<div class="form-group col-xs-12 floating-label controls">
									<label>Select objects for import</label> <img
										src="./img/vraagteken.jpg"
										title="Select the objects you want to import from WeFact into WorkOrderApp"
										height="13" width="13" /><br /> Saved imports: <b> <c:forEach
											items="${checkboxes}" var="checkboxs">${checkboxs}, 
										</c:forEach></b>
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

								</div>
							</div>
						</div>
					</div>
					<div class="panel panel-success">
						<div class="panel-heading">Export settings</div>
						<div class="panel-body">
							<div class="row control-group">
								<div class="form-group col-xs-12 floating-label controls">
									<label>Werkbontype</label> <img src="./img/vraagteken.jpg"
										title="Choose the status of the workorder you want to export"
										height="13" width="13" /> <select name="factuurType"
										class="form-control" id="factuurlist" required>
										<option disabled selected value>-- Select a
											Werkbontype --</option>
										<option value="Compleet"
											${factuur == 'Compleet' ? 'selected="selected"' : ''}>Compleet</option>
										<option value="Afgehandeld"
											${factuur == 'Afgehandeld' ? 'selected="selected"' : ''}>Afgehandeld</option>
									</select>
								</div>

							</div>
							<br>
							<div id="success"></div>
							<div class="row">
								<div class="form-group col-xs-12">
									<input type="submit" class="btn btn-success btn-lg"
										value="Save" name="category" />
								</div>
							</div>
						</div>
					</div>
				</form>

				<div class="panel panel-success">
					<div class="panel-heading">Manually synchronize</div>
					<div class="panel-body">
						<div class="row control-group">
							<div class="form-group col-xs-12 floating-label controls">
								<div id="success"></div>
								<div class="row">
									<div class="form-group col-xs-12">
										<form action="sync.do">
											<input type="hidden" value="${softwareToken}" name="token" />
											<input type="submit" class="btn btn-success btn-lg"
												value="Synchronize" onclick="return synchMessage();" />
										</form>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>

			</div>

		</div>
		<div class="log">
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

</body>

</html>

