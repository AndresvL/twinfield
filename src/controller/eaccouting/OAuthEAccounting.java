package controller.eaccouting;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import controller.Authenticate;
import object.Settings;
import object.Token;

public class OAuthEAccounting extends Authenticate {
	@Override
	public void authenticate(String softwareToken, HttpServletRequest req, HttpServletResponse resp)
			throws ClientProtocolException, IOException, ServletException {
		RequestDispatcher rd = null;
		String token = System.getenv("EACCOUNTING_TOKEN");
		if (token == null) {
			// Client ID
			token = "werkbonapp";
		}
		String softwareName = req.getParameter("softwareName");
		Token dbToken = null;
		// Get token from database
		try {
			dbToken = TokenDAO.getToken(softwareToken, softwareName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// First time login
		if (dbToken == null) {
			String callback = "https://localhost:8080/connect/verify.do";
			String host = "https://auth-sandbox.test.vismaonline.com";
			String uri = host + "/eaccountingapi/oauth/authorize?client_id=" + token + "&redirect_uri=" + callback
					+ "&state=success&scope=sales+accounting+purchase&response_type=code";
			resp.sendRedirect(uri);
		} else if (dbToken.getAccessSecret().equals("invalid")) {
			rd = req.getRequestDispatcher("eAccounting.jsp");
			req.getSession().setAttribute("softwareToken", null);
			req.getSession().setAttribute("errorMessage",
					"softwareToken is al in gebruik door " + dbToken.getSoftwareName());
		} else {
			req.getSession().setAttribute("softwareToken", dbToken.getSoftwareToken());
			req.getSession().setAttribute("errorMessage", "");
			if (req.getSession().getAttribute("softwareToken") == null) {

			}
			ArrayList<Map<String, String>> allLogs = ObjectDAO.getLogs(softwareToken);
			if (!allLogs.isEmpty() || allLogs != null) {
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("logs", allLogs);
			}
			Settings set = ObjectDAO.getSettings(softwareToken);
			if (set != null) {
				Map<String, String> allImports = new HashMap<String, String>();
				for (String s : set.getImportObjects()) {
					allImports.put(s, "selected");
				}
				Map<String, String> exportWerkbonType = new HashMap<String, String>();
				exportWerkbonType.put(set.getExportWerkbontype(), "selected");

				req.getSession().setAttribute("checkboxes", allImports);
				req.getSession().setAttribute("exportWerkbonType", exportWerkbonType);
				req.getSession().setAttribute("roundedHours", set.getRoundedHours());
				req.getSession().setAttribute("factuur", set.getFactuurType());
			}
			rd = req.getRequestDispatcher("eAccounting.jsp");
			rd.forward(req, resp);
		}

	}

	// Called by verifyServlet
	public static Token getAccessToken(String authCode, String refresh, String softwareName, String softwareToken) {
		String token = System.getenv("EACCOUNTING_TOKEN");
		if (token == null) {
			// Client ID
			token = "werkbonapp";
		}
		String secret = System.getenv("EACCOUNTING_SECRET");
		if (secret == null) {
			// Client Secret
			secret = "WWXa37lYWz01tlGkuYzy4PfjhEVRmDijjxPraaDG8U";
		}
		//production environment
		String host = System.getenv("EACCOUNTING_HOST");
		if (host == null) {
			// sandbox environment
			host = "https://auth-sandbox.test.vismaonline.com";
		}
		Token dbToken = new Token();
		dbToken.setConsumerToken(token);
		dbToken.setConsumerSecret(secret);
		dbToken.setSoftwareName(softwareName);
		dbToken.setSoftwareToken(softwareToken);
		String callback = "https://localhost:8080/connect/verify.do";
		String link = host + "/eaccountingapi/oauth/token";
		String input = null;
		if (authCode != null) {
			input = "client_id=" + token + "&client_secret=" + secret + "&code=" + authCode
					+ "&grant_type=authorization_code&redirect_uri=" + callback;
		} else {
			input = "client_id=" + token + "&client_secret=" + secret + "&refresh_token=" + refresh
					+ "&grant_type=refresh_token&redirect_uri=" + callback;
		}

		byte[] postData = input.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;

		String auth = token + ":" + secret;
		byte[] encodedBytes = Base64.encodeBase64(auth.getBytes());
		String encoding = new String(encodedBytes);

		String output = null;
		try {
			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			// conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
			BufferedReader br = null;
			if (conn.getResponseCode() > 200 && conn.getResponseCode() < 405) {
				br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
			} else {
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			}
			System.out.println("Output from Server .... \n");

			while ((output = br.readLine()) != null) {
				try {
					JSONObject json = new JSONObject(output);
					String refreshToken = json.getString("refresh_token");
					String accessToken = json.getString("access_token");
					dbToken.setAccessToken(accessToken);
					// refreshToken!
					dbToken.setAccessSecret(refreshToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (authCode != null) {
				TokenDAO.saveToken(dbToken);
			} else {
				TokenDAO.replaceToken(dbToken);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dbToken;
	}
}
