package servlet;

import java.io.*;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import DAO.ObjectDAO;
import object.Settings;

public class ImportDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String redirect = System.getenv("CALLBACK");

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String importOffice = req.getParameter("offices");
		String exportOffice = req.getParameter("exportOffices");
		String factuurType = req.getParameter("factuurType");
		String[] importTypes = req.getParameterValues("importType");
		// String[] exportTypes = req.getParameterValues("exportType");
		String token = (String) req.getSession().getAttribute("softwareToken");

		ArrayList<String> impTypes = new ArrayList<String>();
		// employees, projects, materials, relations and/or hourtypes
		Settings checkbox = ObjectDAO.getSettings(token);
		ArrayList<String> checkboxes = null;
		if (importTypes != null) {
			for (String type : importTypes) {
				impTypes.add(type);
			}
			Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes);
			ObjectDAO.saveSettings(set, token);
		}else{
			if (checkbox != null) {
				checkboxes = checkbox.getImportObjects();
				if (checkboxes != null) {
					Settings set = new Settings(importOffice, exportOffice, factuurType, checkboxes);
					ObjectDAO.saveSettings(set, token);
				}
			}
		}
		if (redirect != null) {
			resp.sendRedirect(redirect + "OAuth.do?token=" + token);
		} else {
			resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + token);
		}
	}
}
