package servlet;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import DAO.ObjectDAO;
import object.Settings;

public class SettingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String redirect = System.getenv("REDIRECT");
	private String softwareName = null, factuurType = null, user = null, token = null;
	private String importOffice = null, exportOffice = null, exportWerkbonType = null, syncDate = null,
			materialCode = null;
	private int roundedHours = 1;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		syncDate = req.getParameter("syncDate");
		factuurType = req.getParameter("factuurType");
		softwareName = req.getParameter("softwareName");
		user = req.getParameter("users");
		String[] importTypes = req.getParameterValues("importType");
		token = req.getParameter("softwareToken");
		
		// For each integration another case;
		switch (softwareName) {
		case "Twinfield":
			importOffice = req.getParameter("offices");
			exportOffice = importOffice;
			break;
		case "WeFact":
			exportWerkbonType = req.getParameter("exportWerkbon");
			roundedHours = Integer.parseInt(req.getParameter("roundedHours"));
			break;
		case "eAccounting":
			exportWerkbonType = req.getParameter("exportWerkbon");
			roundedHours = Integer.parseInt(req.getParameter("roundedHours"));
			materialCode = req.getParameter("materialCode");
			importOffice = req.getParameter("typeofwork");
			exportOffice = req.getParameter("paymentmethod");
			req.getSession().setAttribute("errorMessage", "");
			break;
		case "Moloni":
			importOffice = req.getParameter("offices");
			exportWerkbonType = req.getParameter("exportWerkbon");
			roundedHours = Integer.parseInt(req.getParameter("roundedHours"));
			exportOffice = req.getParameter("typeofwork");
//			exportOffice = req.getParameter("paymentmethod");
			req.getSession().setAttribute("errorMessage", "");
			break;
		}
		
		if (token != null) {
			ArrayList<String> impTypes = new ArrayList<String>();
			ArrayList<String> impTypesCheck = new ArrayList<String>();
			Settings oldSettings = ObjectDAO.getSettings(token);
			String message = "";
			// Check if settings are changed for response message
			if (oldSettings != null && importTypes != null) {
				for (String type : importTypes) {
					impTypesCheck.add(type);
				}
				if (importOffice != null && !importOffice.equals(oldSettings.getImportOffice())
						&& softwareName.equals("Twinfield")) {
					message = "Administratie is opgeslagen<br />";
				}
				if (importOffice != null && !importOffice.equals(oldSettings.getImportOffice())
						&& softwareName.equals("EAccounting")) {
					message = "Worktype saved<br />";
				}
				if (exportOffice != null && !exportOffice.equals(oldSettings.getExportOffice())
						&& softwareName.equals("EAccounting")) {
					message += "Paymentmethod saved<br />";
				}
				if (importTypes != null && !impTypesCheck.equals(oldSettings.getImportObjects())) {
					message += "Import objects saved<br />";
				}
				if (importOffice != null && user != null && !user.equals(oldSettings.getUser())
						&& softwareName.equals("Twinfield")) {
					message += "Medewerker voor uurboeking is opgeslagen<br />";
				}
				if (exportWerkbonType != null && !exportWerkbonType.equals(oldSettings.getExportWerkbontype())) {
					message += "Workorder type saved<br />";
				}
				if (roundedHours != oldSettings.getRoundedHours()) {
					message += "Rounded hours saved<br />";
				}
				if (syncDate != null && !syncDate.equals(oldSettings.getSyncDate())) {
					message += "Synchronisation date saved<br />";
				}
				if (materialCode != null && !materialCode.equals(oldSettings.getMaterialCode())) {
					message += "Article number saved<br />";
				}
			} else {
				message = "Settings saved<br />";
			}
			req.getSession().setAttribute("checkSaved", message);
			
			if (importTypes != null) {
				for (String type : importTypes) {
					impTypes.add(type);
				}
				Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes, user, exportWerkbonType,
						roundedHours, syncDate, materialCode);
				ObjectDAO.saveSettings(set, token);
			} else {
				// employees, projects, materials, relations and/or hourtypes
				Settings checkbox = ObjectDAO.getSettings(token);
				ArrayList<String> checkboxes = null;
				if (checkbox != null) {
					checkboxes = checkbox.getImportObjects();
					if (checkboxes != null) {
						Settings set = new Settings(importOffice, exportOffice, factuurType, checkboxes, user,
								exportWerkbonType, roundedHours, syncDate, materialCode);
						ObjectDAO.saveSettings(set, token);
					}
				}
			}
			if (redirect != null) {
				resp.sendRedirect(redirect + "OAuth.do?token=" + token + "&softwareName=" + softwareName);
			} else {
				resp.sendRedirect(
						"https://www.localhost:8080/connect/OAuth.do?token=" + token + "&softwareName=" + softwareName);
			}
		}
	}
}
