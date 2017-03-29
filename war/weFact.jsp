<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
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
<link href="vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="https://fonts.googleapis.com/css?family=Montserrat:400,700" rel="stylesheet"
	type="text/css">
<link href="https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic"
	rel="stylesheet" type="text/css">
<!-- Sweet Alert -->
<script src="sweetalert2/sweetalert2.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="sweetalert2/sweetalert2.min.css">
<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
		        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
		        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
		    <![endif]-->
<style type="text/css">
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
#help {
	float: right;
}

#show {
	float: right;
}

#savebutton {
	margin-right: 10px;
	float: right;
}

#syncbutton {
	float: right;
	margin-right: 20px;
}
</style>
</head>
<body>
	<!-- Settings Section -->
	<div class="settings">
		<div class="panel-group">
			<!-- The login Modal -->
			<div id="loginModal" class="modal">
				<!-- Modal content -->
				<div class="modal-content">
					<form action="OAuth.do">
						<input type="hidden" value="${softwareToken}" name="token" id="softwareToken" />
						<input type="hidden" value="WeFact" name="softwareName" id="softwareName" />
						<input type="hidden" value="${clientToken}" id="client" />
						<table>
							<tr>
								<td>
									<h2>Authentication</h2>
								</td>
							</tr>
							<tr>
								<td>
									<label>${errorMessage}</label>
								</td>
							</tr>
							<tr>
								<td>
									<label>Securitycode WeFact</label>
									<img src="./img/vraagteken.jpg" data-toggle="tooltip"
										title="Login bij WeFact en navigeer naar Instellingen - API - beveiligingscode"
										height="13" width="13" />
								</td>
							</tr>
							<tr>
								<td>
									<input type="text" class="form-control" id="clientToken"
										placeholder="example: 5a5a5fbbcecdd585aa62812119d0721e"
										value="10b566e8fe030c6d083ebee7d043757f" name="clientToken" required />
								</td>
							</tr>
						</table>
						<br>
						<input type="submit" value="Submit" class="btn btn-success btn-lg" />
					</form>
				</div>
			</div>
			<!-- The Help Modal -->
			<div id="myModal" class="modal">
				<!-- Modal content -->
				<div class="modal-content">
					<div class="modal-header">
						<span class="close">&times;</span>
						<h2>
							Welkom <small>bij de WeFact koppeling</small>
						</h2>
					</div>
					<div class="modal-body">
						<h4>Informatie</h4>
						- Op deze pagina is het mogelijk om de <mark>import</mark> en <mark>export</mark>
						gegevens tussen WerkbonApp en WeFact in te stellen<br> - Elke 15 minuten zal er
						een <mark>automatische synchronisatie</mark> plaatsvinden aan de hand van deze
						instellingen.<br> - Het is mogelijk om <mark>handmatig een synchronisatie
							uit te voeren</mark> door onderaan op de knop Start Synchronisation te klikken.<br> <br>
						<h4>Belangrijk</h4>
						<b>
							Let op! WeFact is leidend. <br>- De <abbr>import settings</abbr> mogen alleen in
							WeFact worden gewijzigd. <br> - Bij een <abbr>foutmelding</abbr> kan je op het
							bericht(log) klikken om meer details te zien. <br> <br>
						</b>
						<button type="button" id="show" class="btn btn-info">Show</button>
						<h4>Data Mapping</h4>
						<div id="mappingTable" style="display: none;">
							<table class="table table-hover">
								<thead>
									<tr>
										<th>WerkbonApp</th>
										<th>WeFact</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<th colspan="2">Import</th>
									</tr>
									<tr>
										<td>Materialen</td>
										<td>Producten</td>
									</tr>
									<tr>
										<td>Relaties</td>
										<td>Debiteuren</td>
									</tr>
									<tr>
										<td>Uursoorten</td>
										<td>Producten(uren)</td>
									</tr>
									<tr>
										<td>Offertes</td>
										<td>Werkbonnen</td>
									</tr>
									<tr>
										<th colspan="2">Export</th>
									</tr>
									<tr>
										<td>Werkbon</td>
										<td>Offerte</td>
									</tr>
								</tbody>
							</table>
						</div>
						<br>
					</div>
					<div class="modal-footer">
						<p>WorkOrderApp B.V.</p>
					</div>
				</div>
			</div>
			<input type="hidden" value="${error}" id="error" />
			<input type="hidden" value="${saved}" id="saved" name="saved" />
			<form action="settings.do" id="saveWeFact">
				<div class="panel panel-success">
					<div class="panel-heading">Import instellingen</div>
					<div class="panel-body">
						<div class="row control-group">
							<div class="form-group col-xs-12 floating-label controls">
								<input type="hidden" value="${softwareName}" name="softwareName" />
								<input type="hidden" value="${softwareToken}" name="softwareToken" />
								<label>Selecteer objecten om te importeren</label>
								<img src="./img/vraagteken.jpg" data-toggle="tooltip"
									title="Selecteer de objecten die je wilt importeren van WeFact naar WerkbonApp"
									height="13" width="13" />
								<div class="checkbox">
									<label>
										<input type="checkbox" value="materials"
											${"selected" == checkboxes.materials  ? 'checked' : ''} name="importType">
										Materialen
									</label>
								</div>
								<div class="checkbox">
									<label>
										<input type="checkbox" value="relations"
											${"selected" == checkboxes.relations  ? 'checked' : ''} name="importType">
										Relaties
									</label>
								</div>
								<div class="checkbox">
									<label>
										<input type="checkbox" value="hourtypes"
											${"selected" == checkboxes.hourtypes  ? 'checked' : ''} name="importType">
										Uursoorten
									</label>
								</div>
								<div class="checkbox">
									<label>
										<input type="checkbox" value="offertes"
											${"selected" == checkboxes.offertes  ? 'checked' : ''} name="importType"
											id="offertes">
										Offertes
									</label>
								</div>
								<button type="button" id="help" class="btn btn-info btn-lg">Help</button>
							</div>
						</div>
					</div>
				</div>
				<div class="panel panel-success">
					<div class="panel-heading">Export instellingen</div>
					<div class="panel-body">
						<div class="row control-group">
							<div class="form-group col-xs-12 floating-label controls">
								<label>Werkbonstatus</label>
								<img src="./img/vraagteken.jpg" height="13" width="13" data-toggle="tooltip"
									title="Werkbonnen met status compleet worden opgehaald" />
								<input class="form-control" type="text" disabled value="Compleet" />
								<input class="form-control" type="hidden" name="factuurType" value="Compleet" />
								<br>
								<label>Werkbon type</label>
								<img src="./img/vraagteken.jpg" data-toggle="tooltip"
									title="De werkbon wordt als factuur of offerte verstuurd naar WeFact"
									height="13" width="13" />
								<br>
								<input type="radio" name="exportWerkbon" value="factuur"
									${"selected" == exportWerkbonType.factuur  ? 'checked' : ''} checked>
								Factuur<br>
								<input type="radio" name="exportWerkbon" value="offerte"
									${"selected" == exportWerkbonType.offerte  ? 'checked' : ''}>
								Offerte<br>
							</div>
						</div>
					</div>
					<br>
					<div class="row">
						<div class="form-group col-xs-12">
							<input type="submit" class="btn btn-success btn-lg" value="Sync" id="syncbutton" />
							<input type="submit" class="btn btn-success btn-lg" value="Save" name="category"
								id="savebutton" />
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>
	<!-- this form will be validated after syncbutton is pressed  -->
	<form action="sync.do" id="sync">
		<input type="hidden" value="${softwareToken}" name="token" />
		<input type="hidden" value="${softwareName}" name="softwareName" />
	</form>
	<div class="settings">
		<div class="panel panel-success">
			<div class="panel-heading">Log</div>
			<div class="panel-body">
				<div class="row control-group">
					<div class="form-group col-xs-12 floating-label controls">
						<table class="table table-hover">
							<thead>
								<tr>
									<th>Tijd</th>
									<th>Bericht</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${logs}" var="log">
									<tr class="showDetails" data-href='${log.details}'>
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
	<script src="vendor/jquery/jquery.min.js" type="text/javascript"></script>
	<!-- Bootstrap Core JavaScript -->
	<script src="vendor/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
	<!-- Plugin JavaScript -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js"
		type="text/javascript"></script>
	<!-- Contact Form JavaScript -->
	<script src="js/jqBootstrapValidation.js" type="text/javascript"></script>
	<script src="js/contact_me.js" type="text/javascript"></script>
	<!-- Theme JavaScript -->
	<script src="js/freelancer.min.js" type="text/javascript"></script>
	<script type="text/javascript" src="js/vkbeautify.js"></script>
	<script type="text/javascript" src="js/wefact.js"></script>
</body>
</html>
