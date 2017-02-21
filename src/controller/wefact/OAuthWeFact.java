package controller.wefact;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;

import controller.Authenticate;

public class OAuthWeFact extends Authenticate {

	@Override
	public void authenticate(String token, String secret, String softwareToken, HttpServletRequest req,
			HttpServletResponse resp) throws ClientProtocolException, IOException, ServletException {
		if (!req.getParameterMap().containsKey("clientToken")) {
			//Login page
			RequestDispatcher rd = req.getRequestDispatcher("weFact.jsp");
			rd.forward(req, resp);
		} else {
			//Settings page
			String clientToken = req.getParameter("clientToken");
			WeFactHandler we = new WeFactHandler();
			if(we.checkClientToken(clientToken)){
				we.getRelations(clientToken);
			}else{
				//Login page
				RequestDispatcher rd = req.getRequestDispatcher("weFact.jsp");
				rd.forward(req, resp);
			}
			
		}
	}

}
