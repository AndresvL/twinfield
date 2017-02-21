package controller.wefact;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeFactHandler {
	String output;
	String controller = "debtor";
	String action = "list";

	// String urlParameters = "api_key=" + apiKey + "&controller="+
	// controller +"&action=" + action + "&DebtorCode=" + "DB0001";

	public HttpURLConnection getConnection(int postDataLength) throws IOException {
		String link = "https://www.mijnwefact.nl/apiv2/api.php";
		URL url = new URL(link);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);
		conn.connect();
		return conn;
	}

	public Boolean checkClientToken(String clientToken) throws IOException {
		Boolean b = false;
		String parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action;
		byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		HttpURLConnection conn = getConnection(postDataLength);
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
			BufferedReader br = new BufferedReader(
					new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));

			while ((output = br.readLine()) != null) {
				JSONObject json;
				String status;
				try {
					json = new JSONObject(output);
					status = json.getString("status");
					if (status.equals("success")) {
						b = true;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return b;
		}
	}

	// Debiteuren
	public void getRelations(String clientToken) throws IOException {
		String parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action;
		byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		// Sets up the rest call;
		HttpURLConnection conn = getConnection(postDataLength);
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));

		while ((output = br.readLine()) != null) {
			JSONObject json;
			String response;
			int totalResults;
			try {
				json = new JSONObject(output);
					totalResults = json.getInt("totalresults");
					JSONArray debtors = json.getJSONArray("debtors");
					for (int i = 0; i < debtors.length(); i++) {
						JSONObject object = debtors.getJSONObject(i);
						String debtorCode = object.getString("DebtorCode");
						System.out.println("output " + output);
					}
					System.out.println("Success : " + totalResults + " " + controller + " imported");
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
