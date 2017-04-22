package servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import DAO.ObjectDAO;
import object.Settings;

public class SettingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String redirect = System.getenv("CALLBACK");
	private String softwareName = null, factuurType = null, user = null, token = null;
	private String importOffice = null, exportOffice = null, exportWerkbonType = null;
	private int roundedHours = 1;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// softwareName = (String)
		// req.getSession().getAttribute("softwareName");
		factuurType = req.getParameter("factuurType");
		softwareName = req.getParameter("softwareName");
		user = req.getParameter("users");
		String[] importTypes = req.getParameterValues("importType");
		token = req.getParameter("softwareToken");
		// For each connection another case;
		switch (softwareName) {
		case "Twinfield":
			importOffice = req.getParameter("offices");
			exportOffice = importOffice;
			break;
		case "WeFact":
			exportWerkbonType = req.getParameter("exportWerkbon");
			roundedHours = Integer.parseInt(req.getParameter("roundedHours"));
			break;
		}
		
		if (token != null) {
			ArrayList<String> impTypes = new ArrayList<String>();
			ArrayList<String> impTypesCheck = new ArrayList<String>();
			// Check if settings are changed for response message
			Settings oldSettings = ObjectDAO.getSettings(token);
			String message = "";
			
			if (oldSettings != null && importTypes != null) {
				for (String type : importTypes) {
					impTypesCheck.add(type);
				}
				if (importOffice != null && !importOffice.equals(oldSettings.getImportOffice())) {
					message = "Administratie is opgeslagen<br />";
				}
				if (importTypes != null && !impTypesCheck.equals(oldSettings.getImportObjects())) {
					message += "Import objecten zijn opgeslagen<br />";
				}
				if (importOffice != null && !user.equals(oldSettings.getUser())) {
					message += "Medewerker voor uurboeking is opgeslagen<br />";
				}
				if (exportWerkbonType != null && !exportWerkbonType.equals(oldSettings.getExportWerkbontype())) {
					message += "Werkbon type is opgeslagen<br />";
				}
				if (roundedHours != oldSettings.getRoundedHours()) {
					message += "Afronding uren is opgeslagen<br />";
				}
			} else {
				message = "Instellingen zijn opgeslagen<br />";
			}
			req.getSession().setAttribute("checkSaved", message);
			
			if (importTypes != null) {
				for (String type : importTypes) {
					impTypes.add(type);
				}
				Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes, user, exportWerkbonType, roundedHours);
				ObjectDAO.saveSettings(set, token);
			} else {
				// employees, projects, materials, relations and/or hourtypes
				Settings checkbox = ObjectDAO.getSettings(token);
				ArrayList<String> checkboxes = null;
				if (checkbox != null) {
					checkboxes = checkbox.getImportObjects();
					if (checkboxes != null) {
						Settings set = new Settings(importOffice, exportOffice, factuurType, checkboxes, user,
								exportWerkbonType, roundedHours);
						ObjectDAO.saveSettings(set, token);
					}
				}
			}
			if (redirect != null) {
				resp.sendRedirect(redirect + "OAuth.do?token=" + token + "&softwareName=" + softwareName);
			} else {
				resp.sendRedirect(
						"http://localhost:8080/connect/OAuth.do?token=" + token + "&softwareName=" + softwareName);
			}
		}
	}
}
