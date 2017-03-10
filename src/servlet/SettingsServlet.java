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
	private String softwareName = null, factuurType = null;
	private String importOffice = null, exportOffice = null;
	private String saved = "false";

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		softwareName = (String) req.getSession().getAttribute("softwareName");
		factuurType = req.getParameter("factuurType");
		softwareName = req.getParameter("softwareName");
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
		ArrayList<String> impTypes = new ArrayList<String>();
		if (importTypes != null) {
			saved = "true";
			for (String type : importTypes) {
				impTypes.add(type);
			}
			req.getSession().setAttribute("checkSaved", saved);
			Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes);
			ObjectDAO.saveSettings(set, token);
		}else{
			req.getSession().setAttribute("saved", false);
			// employees, projects, materials, relations and/or hourtypes
			Settings checkbox = ObjectDAO.getSettings(token);
			ArrayList<String> checkboxes = null;
			if (checkbox != null) {
				checkboxes = checkbox.getImportObjects();
				if (checkboxes != null) {
					Settings set = new Settings(importOffice, exportOffice, factuurType, checkboxes);
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
