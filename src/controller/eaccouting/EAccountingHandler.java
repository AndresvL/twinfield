package controller.eaccouting;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import controller.WorkOrderHandler;
import controller.twinfield.SoapHandler;
import object.Token;
import object.workorder.Address;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Relation;
import object.workorder.WorkOrder;
import object.workorder.WorkPeriod;

public class EAccountingHandler {
	final String softwareName = "EAccounting";
	private final static Logger logger = Logger.getLogger(SoapHandler.class.getName());
	// Change to environment variable
	protected String host = "eaccountingapi-sandbox.test.vismaonline.com";
	
	public boolean checkAccessToken(String accessToken) throws IOException {
		String link = "https://" + host + "/v1/articles";
		
		try {
			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			// conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setUseCaches(false);
			if (conn.getResponseCode() > 200 && conn.getResponseCode() < 405) {
				return false;
			}
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// public JSONObject postJSON(String accessToken, String parameters, String
	// jsonRequest) throws IOException {
	// // String link =
	// // "https://eaccountingapi-sandbox.test.vismaonline.com/v1/articles";
	// String output = null;
	// try {
	// URL url = new URL(link);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setDoOutput(true);
	// conn.setInstanceFollowRedirects(false);
	// // conn.setDoInput(true);
	// conn.setRequestMethod("GET");
	// conn.setRequestProperty("Content-Type", "application/json");
	// conn.setRequestProperty("Authorization", "Bearer " + accessToken);
	// conn.setUseCaches(false);
	// BufferedReader br = null;
	// System.out.println("ACCESS " + accessToken);
	// if (conn.getResponseCode() > 200 && conn.getResponseCode() < 405) {
	// System.out.println(conn.getResponseMessage());
	// return false;
	// }
	// conn.disconnect();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return JSONObject;
	// }
	
	public Object getJSON(String accessToken, String parameters, String path, String method)
			throws IOException, URISyntaxException {
		String jsonString = null;
		Object object = null;
		try {
			URI uri = new URI("https", host, path, parameters, null);
			String request = uri.toASCIIString();
			System.out.println("REQUESTLINK " + request);
			URL url = new URL(request);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			// conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setUseCaches(false);
			if (conn.getResponseCode() > 200 && conn.getResponseCode() < 405) {
				return null;
			}
			BufferedReader br = new BufferedReader(
					new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
			
			while ((jsonString = br.readLine()) != null) {
				System.out.println("getJSON PARAMETERS " + parameters);
				System.out.println("getJSON RESPONSE" + jsonString);
				switch (method) {
				case "array":
					object = new JSONArray(jsonString);
					break;
				case "object":
					object = new JSONObject(jsonString);
					break;
				}
				
			}
			conn.disconnect();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public String getDateMinHour(String string) {
		System.out.println("TIME " + string);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = null;
		try {
			// String to date
			date = format.parse(string);
			// Create Calender to edit time
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.HOUR_OF_DAY, 2);
			date = cal.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Date to String
		Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String s = formatter.format(date);
		System.out.println("TIME S " + s);
		return s;
	}
	
	// Producten
	// This method also getHourtypes if unit is uur
	public String[] getMaterials(Token t, String date) throws Exception {
		boolean checkUpdate = false;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		String path = "/v1/articles";
		String parameters = "showPricesWithTwoDecimals=true";
		String errorMessage = "";
		int importCount = 0;
		int importCount2 = 0;
		int editCount = 0;
		int editCount2 = 0;
		ArrayList<Material> materials = new ArrayList<Material>();
		ArrayList<HourType> hourtypes = new ArrayList<HourType>();
		Boolean hasContent = ObjectDAO.hasContent(t.getSoftwareToken(), "materials");
		if (date != null && hasContent) {
			parameters += "&changedFromDate=" + getDateMinHour(date);
		}
		
		jsonArray = (JSONArray) getJSON(t.getAccessToken(), parameters, path, "array");
		logger.info("Materials response JSONArray " + jsonArray);
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObj = jsonArray.getJSONObject(i);
				
				String modified = jsonObj.getString("ChangedUtc");
				String productCode = jsonObj.getString("Number");
				String unitName = jsonObj.getString("UnitName");
				String description = jsonObj.getString("Name");
				Double price = jsonObj.getDouble("NetPrice");
				String unit = jsonObj.getString("UnitName");
				
				String dbModified = ObjectDAO.getModifiedDate(t.getSoftwareToken(), null, productCode, "materials");
				// Check if data is modified
				if (unitName.equals("Uur")) {
					if (dbModified == null || date == null) {
						importCount2++;
					} else {
						editCount2++;
					}
					HourType h = new HourType(productCode, description, 0, 1, 0, price, 1, modified);
					hourtypes.add(h);
				} else {
					if (dbModified == null || date == null) {
						importCount++;
					} else {
						editCount++;
					}
					Material m = new Material(productCode, null, unit, description, price, null, modified);
					materials.add(m);
				}
			}
		}
		//Materials log message
		if (!materials.isEmpty()) {
			int successAmount = (int) WorkOrderHandler.addData(t.getSoftwareToken(), materials, "materials",
					softwareName, null);
			if (successAmount > 0) {
				ObjectDAO.saveMaterials(materials, t.getSoftwareToken());
				errorMessage += importCount + " materials imported<br>";
				errorMessage += "and " + editCount + " materials edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with materials<br>";
			}
		} else {
			errorMessage += "No materials for import<br>";
		}
//		Hourtypes log message
		if (!hourtypes.isEmpty()) {
			int successAmount2 = (int) WorkOrderHandler.addData(t.getSoftwareToken(), hourtypes, "hourtypes",
					softwareName, null);
			if (successAmount2 > 0) {
				ObjectDAO.saveMaterials(materials, t.getSoftwareToken());
				errorMessage += importCount2 + " hourtypes imported<br>";
				errorMessage += "and " + editCount2 + " hourtypes edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with hourtypes<br>";
			}
		} else {
			errorMessage += "No hourtypes for import<br>";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}
	
	// Debiteuren
	public String[] getRelations(Token t, String date) throws Exception {
		boolean checkUpdate = false;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		String path = "/v1/customerlistitems";
		String parameters = "";
		String errorMessage = "";
		int importCount = 0;
		int editCount = 0;
		ArrayList<Relation> relations = new ArrayList<Relation>();
		Boolean hasContent = ObjectDAO.hasContent(t.getSoftwareToken(), "relations");
		if (date != null && hasContent) {
			parameters += "changedFromDate=" + getDateMinHour(date);
		}
		
		jsonArray = (JSONArray) getJSON(t.getAccessToken(), parameters, path, "array");
		logger.info("Relation response JSONArray " + jsonArray);
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObj = jsonArray.getJSONObject(i);
				String id = jsonObj.getString("Id");
				path = "/v1/customers/" + id;
				parameters = "";
				JSONObject debtorDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path, "object");
				logger.info("Relation response debtorDetails " + debtorDetails);
				ArrayList<Address> address = new ArrayList<Address>();
				String modified = debtorDetails.getString("ChangedUtc");
				String debtorNr = debtorDetails.getString("CustomerNumber");
				String dbModified = ObjectDAO.getModifiedDate(t.getSoftwareToken(), null, debtorNr, "relations");
				// Check if data is modified
				if (dbModified == null || date == null) {
					importCount++;
				} else {
					editCount++;
				}
				// Postal
				String firstName = debtorDetails.optString("ContactPersonName", "<leeg>");
				String companyName = debtorDetails.optString("DeliveryCustomerName", "<leeg>");
				String contact = firstName;
				String mobileNr = debtorDetails.optString("MobilePhone", "");
				String phoneNr = debtorDetails.optString("Telephone", "");
				if (phoneNr.equals("")) {
					phoneNr = mobileNr;
				}
				String email = debtorDetails.optString("EmailAddress", "");
				String street = debtorDetails.optString("DeliveryAddress1", "<leeg>");
				String postalCode = debtorDetails.optString("DeliveryPostalCode", "<leeg>");
				String city = debtorDetails.optString("DeliveryCity", "<leeg>");
				String remark = debtorDetails.optString("Note");
				// Postal address is not used!!
				Address postal = new Address(contact, phoneNr, email, street, "", postalCode, city, remark, "postal",
						2);
				// address.add(postal);
				// Invoice
				String invoiceFirstName = debtorDetails.optString("ContactPersonName");
				String invoiceCompanyName = debtorDetails.optString("Name", "<leeg>");
				if (!invoiceCompanyName.equals("<leeg>")) {
					companyName = invoiceCompanyName;
				}
				String invoiceContact = invoiceFirstName;
				String invoiceEmail = debtorDetails.optString("EmailAddress");
				String invoicestreet = debtorDetails.optString("InvoiceAddress1", "<leeg>");
				String invoicepostalCode = debtorDetails.optString("InvoicePostalCode", "<leeg>");
				String invoicecity = debtorDetails.optString("InvoiceCity", "<leeg>");
				if (!invoicecity.equals("<leeg>")) {
					Address invoice = new Address(invoiceContact, phoneNr, invoiceEmail, invoicestreet, "",
							invoicepostalCode, invoicecity, remark, "invoice", 1);
					address.add(invoice);
				}
				Relation r = new Relation(companyName, debtorNr, contact, invoiceEmail, address, modified);
				relations.add(r);
			}
		}
		if (!relations.isEmpty()) {
			int successAmount = (int) WorkOrderHandler.addData(t.getSoftwareToken(), relations, "relations",
					softwareName, null);
			if (successAmount > 0) {
				ObjectDAO.saveRelations(relations, t.getSoftwareToken());
				errorMessage += importCount + " relations imported<br>";
				errorMessage += "and " + editCount + " relations edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with relations<br>";
			}
		} else {
			errorMessage += "No relations for import<br>";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}
	
}