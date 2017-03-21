package servlet;

import java.io.*;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import DAO.ObjectDAO;
import object.Settings;

public class SettingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String redirect = System.getenv("CALLBACK");
	private String softwareName = null, factuurType = null, user = null;
	private String importOffice = null, exportOffice = null;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		softwareName = (String) req.getSession().getAttribute("softwareName");
		factuurType = req.getParameter("factuurType");
		softwareName = req.getParameter("softwareName");
		user = req.getParameter("users");
		String[] importTypes = req.getParameterValues("importType");
		String token = (String) req.getSession().getAttribute("softwareToken");
		//For each connection another case;
		switch(softwareName){
		case "Twinfield" :
			importOffice = req.getParameter("offices");
			exportOffice = importOffice;
			break;
		case "WeFact" :
			break;
		}
		//check if settings are changed for response message
		Settings oldSettings = ObjectDAO.getSettings(token);
		String message = "";
		if(oldSettings != null){
			if(importOffice != null && !importOffice.equals(oldSettings.getImportOffice())){
				message = "Administratie is opgeslagen<br />";
			}else if(importTypes != null &&!importTypes.equals(oldSettings.getImportObjects())){
				message += "Import objecten zijn opgeslagen<br />";
			}else if(importOffice != null  && !user.equals(oldSettings.getUser())){
				message += "Medewerker voor uurboeking is opgeslagen<br />";
			}else{
				message = null;
			}
		}else{
			message = "Import objecten zijn opgeslagen<br />";
		}	
		req.getSession().setAttribute("checkSaved", message);
		
		ArrayList<String> impTypes = new ArrayList<String>();
		if (importTypes != null) {
			for (String type : importTypes) {
				impTypes.add(type);
			}
			Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes, user);
			ObjectDAO.saveSettings(set, token);
		}else{
			// employees, projects, materials, relations and/or hourtypes
			Settings checkbox = ObjectDAO.getSettings(token);
			ArrayList<String> checkboxes = null;
			if (checkbox != null) {
				checkboxes = checkbox.getImportObjects();
				if (checkboxes != null) {
					Settings set = new Settings(importOffice, exportOffice, factuurType, checkboxes, user);
					ObjectDAO.saveSettings(set, token);
				}
			}
		}
		if (redirect != null) {
//			 + "&checkbox=" + oldImport
			resp.sendRedirect(redirect + "OAuth.do?token=" + token + "&softwareName=" + softwareName);
		} else {
			resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + token + "&softwareName=" + softwareName);
		}
	}
}
