package servlet;

import java.io.*;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import DAO.ObjectDAO;
import object.Settings;

public class ImportDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String importOffice = req.getParameter("offices");
		String exportOffice = req.getParameter("exportOffices");
		String factuurType = req.getParameter("factuurType");
		String[] importTypes = req.getParameterValues("importType");
		// String[] exportTypes = req.getParameterValues("exportType");
		String token = (String) req.getSession().getAttribute("softwareToken");

		ArrayList<String> impTypes = new ArrayList<String>();
		// employees, projects, materials, relations and/or hourtypes

		if (importTypes != null) {
			for (String type : importTypes) {
				impTypes.add(type);
			}

		} else {
			impTypes.add("alles");
		}

		Settings set = new Settings(importOffice, exportOffice, factuurType, impTypes);
		ObjectDAO.saveSettings(set, token);
		// Chance this later
		resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + token);
	}
}
