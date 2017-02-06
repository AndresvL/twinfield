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

<title>Twinfield Connect</title>

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
		if ($('#session').val() == "" || $('#session').val() == null) {
			swal({
				title : 'Warning',
				text : 'You are not connected',
				type : 'error'
			}).then(function() {
				$('#settings').hide();
			});
		}
	}
	function message() {
		if ($('#officelist').val() != null)
			swal({
				title : 'Please Wait!',
				text : 'Saving settings',
				imageUrl : 'loadingbar.gif',
				imageWidth : 150,
				imageHeight : 150,
				animation : true
			})
		return true;
	}
</script>
<style>
#float {
	float: left;
}

.settings {
	width: 50%;
	float: left;
}

.log {
	width: 50%;
	float: right;
}
</style>
</head>

<body onload="return checkSession();">
	<!-- Settings Section -->
	<div id="float">
		<div class="settings">
			<div class="panel-group">
				<input type="hidden" value="${session}" id="session" /> <input
					type="hidden" value="${error}" id="error" />
				<form action="import.do">
					<div class="panel panel-success">
						<div class="panel-heading">Import settings</div>
						<div class="panel-body">
							<div class="row control-group">
								<div class="form-group col-xs-12 floating-label controls">
									<label>Office</label> <select name="offices"
										class="form-control" id="officelist" required>
										<option disabled selected value>-- Select an office
											--</option>
										<c:forEach items="${offices}" var="office">
											<option value="${office.code}"
												${office.code == importOffice ? 'selected="selected"' : ''}>${office.name}</option>
										</c:forEach>
									</select> <br> <label>Select objects for import</label><br />Saved
									imports:
									<c:forEach items="${checkbox}" var="checkboxs">${checkboxs} </c:forEach>
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
									<label>Office</label> <select name="exportOffices"
										class="form-control" id="officeExportList" required>
										<option disabled selected value>-- Select an office
											--</option>
										<c:forEach items="${offices}" var="office">
											<option value="${office.code}"
												${office.code == exportOffice ? 'selected="selected"' : ''}>${office.name}</option>
										</c:forEach>
									</select>
								</div>
								<div class="form-group col-xs-12 floating-label controls">
									<label>Werkbontype</label> <select name="factuurType"
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
										value="Save" name="category" onclick="return message();" />
								</div>
							</div>
						</div>
					</div>
				</form>
				<form action="sync.do">
					<div class="panel panel-success">
						<div class="panel-heading">Manually synchronize</div>
						<div class="panel-body">
							<div class="row control-group">
								<div class="form-group col-xs-12 floating-label controls">
									<div id="success"></div>
									<div class="row">
										<div class="form-group col-xs-12">
											<input type="hidden" 
												value="${softwareToken}" name="token"/>
											<input type="submit" class="btn btn-success btn-lg"
												value="Synchronize"/>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</form>
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

