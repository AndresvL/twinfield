package servlet;

import controller.WorkOrderHandler;
import controller.drivefx.OAuthDriveFx;
import controller.eaccouting.OAuthEAccounting;
import controller.moloni.OAuthMoloni;
import controller.sageone.OAuthSageOne;
import controller.snelstart.OAuthSnelStart;
import controller.twinfield.OAuthTwinfield;
import controller.wefact.OAuthWeFact;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		// Get checkSaved from session every time oauth is called
		String checkSaved = (String) req.getSession().getAttribute("checkSaved");
		if (checkSaved != null) {
			req.getSession().setAttribute("saved", checkSaved);
			req.getSession().setAttribute("checkSaved", null);
		} else {
			req.getSession().setAttribute("saved", "");
		}
		
		RequestDispatcher rd = null;
		// Set session with software name and token
		int code = WorkOrderHandler.checkWorkOrderToken(softwareToken, softwareName);
		if (code == 200) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
			req.getSession().setAttribute("checkboxes", null);
			req.getSession().setAttribute("exportCheckboxes", null);
			req.getSession().setAttribute("logs", null);
			req.getSession().setAttribute("error", null);
			
			switch (softwareName) {
			case "Twinfield":
				OAuthTwinfield oauth = new OAuthTwinfield();
				oauth.authenticate(softwareToken, req, resp);
				req.getSession().setAttribute("offices", null);
				req.getSession().setAttribute("users", null);
				break;
			case "WeFact":
				OAuthWeFact oauth2 = new OAuthWeFact();
				oauth2.authenticate(softwareToken, req, resp);
				break;
			case "eAccounting":
				// typeofwork
				req.getSession().setAttribute("types", null);
				// paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthEAccounting oauth3 = new OAuthEAccounting();
				oauth3.authenticate(softwareToken, req, resp);
				break;
			case "Moloni":
				req.getSession().setAttribute("offices", null);
				// typeofwork
				req.getSession().setAttribute("types", null);
				// paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthMoloni oauth4 = new OAuthMoloni();
				oauth4.authenticate(softwareToken, req, resp);
				break;
			case "DriveFx":
				// typeofwork
				req.getSession().setAttribute("types", null);
				// paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthDriveFx oauth5 = new OAuthDriveFx();
				oauth5.authenticate(softwareToken, req, resp);
				break;
			case "SageOne":
				// typeofwork
				req.getSession().setAttribute("types", null);
				// paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthSageOne oauth6 = new OAuthSageOne();
				oauth6.authenticate(softwareToken, req, resp);
				break;
			case "SnelStart_Online":
				// typeofwork
				req.getSession().setAttribute("types", null);
				// paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthSnelStart oauth7 = new OAuthSnelStart();
				oauth7.authenticate(softwareToken, req, resp);
				break;
			}
		} else {
			switch (softwareName) {
			case "Twinfield":
				rd = req.getRequestDispatcher("twinfield.jsp");
				req.getSession().setAttribute("session", null);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("error", "Token is invalid");
				break;
			case "WeFact":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("weFact.jsp");
				break;
			case "eAccounting":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("eAccounting.jsp");
				break;
			case "Moloni":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("moloni.jsp");
				break;
			case "DriveFx":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("driveFx.jsp");
				break;
			case "SageOne":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("sageOne.jsp");
				break;
			case "SnelStart_Online":
				req.getSession().setAttribute("softwareToken", softwareToken);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " + code + ": Token is invalid");
				rd = req.getRequestDispatcher("snelStart.jsp");
				break;
			default:
				break;
			}
			rd.forward(req, resp);
		}
		
	}
}
