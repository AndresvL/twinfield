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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import DAO.ObjectDAO;
import controller.WorkOrderHandler;
import controller.twinfield.SoapHandler;
import object.Settings;
import object.Token;
import object.workorder.Address;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Project;
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
	
	public Object putJSON(String accessToken, JSONObject json, String path, String method) {
		String jsonString = null;
		Object object = null;
		String jsonRequestString = json + "";
		byte[] postData = jsonRequestString.getBytes(StandardCharsets.UTF_8);
		
		int postDataLength = postData.length;
		try {
			URI uri = new URI("https", host, path, "", null);
			String request = uri.toASCIIString();
			URL url = new URL(request);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			// conn.setDoInput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
			
			if (conn.getResponseCode() > 200 && conn.getResponseCode() < 405) {
				System.out.println("RESPONSEMESSAGE " + conn.getResponseMessage());
				return null;
			}
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
			
			while ((jsonString = br.readLine()) != null) {
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
		} catch (IOException | JSONException | URISyntaxException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public String getDateMinHour(String string) {
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
		return s;
	}
	
	public String convertDate(String string, String formatResponse) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = null;
		try {
			// String to date
			date = format.parse(string);
			// Create Calender to edit time
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.HOUR_OF_DAY, 0);
			date = cal.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Date to String
		Format formatter = new SimpleDateFormat(formatResponse);
		String s = formatter.format(date);
		return s;
	}
	
	public String getCurrentDate(String date) {
		String timestamp;
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		if (date != null) {
			timestamp = date;
		} else {
			timestamp = za.format(formatter);
		}
		return timestamp;
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
			// yyyy-MM-dd hh:mm:ss
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
		// Materials log message
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
		// Hourtypes log message
		if (!hourtypes.isEmpty()) {
			int successAmount2 = (int) WorkOrderHandler.addData(t.getSoftwareToken(), hourtypes, "hourtypes",
					softwareName, null);
			if (successAmount2 > 0) {
				ObjectDAO.saveHourTypes(hourtypes, t.getSoftwareToken());
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
	
	// Verkooporders
	public String[] getOrders(Token t, String date, Settings set) throws Exception {
		boolean checkUpdate = false;
		WorkOrder w = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		String path = "/v1/orderlistitems";
		String parameters = "";
		String errorMessage = "";
		int importCount = 0;
		ArrayList<Relation> relations = new ArrayList<Relation>();
		ArrayList<WorkOrder> workorders = new ArrayList<WorkOrder>();
		JSONObject orderDetails = null;
		jsonArray = (JSONArray) getJSON(t.getAccessToken(), parameters, path, "array");
		logger.info("Order response JSONArray " + jsonArray);
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				ArrayList<Address> address = new ArrayList<Address>();
				jsonObj = jsonArray.getJSONObject(i);
				int status = jsonObj.getInt("Status");
				if (status == 2) {
					String id = jsonObj.getString("Id");
					path = "/v1/orders/" + id;
					parameters = "";
					// OrderDetails
					orderDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path, "object");
					logger.info("orderDetails response " + orderDetails);
					
					String customerId = orderDetails.getString("CustomerId");
					path = "/v1/customers/" + customerId;
					// Debiteur
					JSONObject debtorDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path, "object");
					logger.info("orderDetails - debtorDetails response " + orderDetails);
					// Get relations
					// Invoice
					String invoiceCompanyName = orderDetails.optString("InvoiceCustomerName", "<leeg>");
					String invoiceDebtorNr = debtorDetails.optInt("CustomerNumber") + "";
					String invoiceEmail = debtorDetails.optString("EmailAddress", "");
					String invoicestreet = debtorDetails.optString("InvoiceAddress1", "<leeg>");
					String invoicepostalCode = debtorDetails.optString("InvoicePostalCode", "<leeg>");
					String invoicecity = debtorDetails.optString("InvoiceCity", "<leeg>");
					String mobileNr = debtorDetails.optString("MobilePhone", "");
					String phoneNr = debtorDetails.optString("Telephone", "");
					if (phoneNr.equals("")) {
						phoneNr = mobileNr;
					}
					String remark = debtorDetails.optString("Note", "");
					if (!invoicecity.equals("<leeg>")) {
						Address invoice = new Address(invoiceCompanyName, phoneNr, invoiceEmail, invoicestreet, "",
								invoicepostalCode, invoicecity, remark, "invoice", 1);
						address.add(invoice);
					}
					// Postal
					String companyName = orderDetails.optString("DeliveryCustomerName", invoiceCompanyName);
					String debtorNr = debtorDetails.optInt("CustomerNumber") + "";
					String email = debtorDetails.optString("EmailAddress", invoiceEmail);
					String street = debtorDetails.optString("DeliveryAddress1", invoicestreet);
					String postalCode = debtorDetails.optString("DeliveryPostalCode", invoicepostalCode);
					String city = debtorDetails.optString("DeliveryCity", invoicecity);
					
					if (!invoicecity.equals("<leeg>")) {
						Address postal = new Address(invoiceCompanyName, phoneNr, invoiceEmail, street, "", postalCode,
								city, remark, "postal", 2);
						address.add(postal);
					}
					String contact = orderDetails.optString("YourReference","");
					Relation r = new Relation(companyName, invoiceDebtorNr, contact, invoiceEmail, address, null);
					relations.add(r);
					
					// Materials
					ArrayList<Material> materials = new ArrayList<Material>();
					Material m = null;
					JSONArray rows = orderDetails.getJSONArray("Rows");
					String workDescription = null;
					for (int j = 0; j < rows.length(); j++) {
						JSONObject rowDetails = rows.getJSONObject(j);
						boolean isTextRow = rowDetails.getBoolean("IsTextRow");
						if (!isTextRow) {
							String productId = rowDetails.getString("ArticleId");
							path = "/v1/articles/" + productId;
							System.out.println("PATH " + path);
							JSONObject materialDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path,
									"object");
							logger.info("orderDetails - materialDetails response " + materialDetails);
							String productCode = materialDetails.optString("Number", "<leeg>");
							String unit = materialDetails.optString("UnitName");
							String description = materialDetails.optString("Name");
							Double price = materialDetails.getDouble("NetPrice");
							String quantity = rowDetails.getInt("Quantity") + "";
							m = new Material(productCode, null, unit, description, price, quantity, null);
							materials.add(m);
						} else {
							workDescription += rowDetails.getString("Text");
						}
					}
					
					String workDate = orderDetails.getString("OrderDate");
					String externProjectNr = orderDetails.optInt("Number") + "";
					String typeofwork = set.getImportOffice();
					String paymentMethod = set.getExportOffice();
					w = new WorkOrder(null, convertDate(workDate, "dd-MM-yyyy hh:mm:ss"), email, email, invoiceDebtorNr,
							status + "", paymentMethod, materials, workDate, null, id, null, relations, null, null,
							null, externProjectNr, typeofwork, workDescription, null, null, null);
					
					importCount++;
					workorders.add(w);
				}
			}
		}
		if (!workorders.isEmpty()) {
			JSONArray responseArray = (JSONArray) WorkOrderHandler.addData(t.getSoftwareToken(), workorders,
					"PostWorkorders", softwareName, null);
			System.out.println("WORKORDERS POST" + workorders.toString());
			for (int i = 0; i < responseArray.length(); i++) {
				JSONObject object = responseArray.getJSONObject(i);
				String id = object.optString("workorder_no", "");
				setOrderStatus(t, id, true);
			}
			int successAmount = responseArray.length();
			
			if (successAmount > 0) {
				// ObjectDAO.saveRelations(relations, t.getSoftwareToken());
				errorMessage += importCount + " verkooporders imported<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with verkooporders<br>";
			}
		} else {
			errorMessage += "No verkooporders for import<br>";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}
	
	// Projects
	public String[] getProjects(Token t, String date) throws Exception {
		boolean checkUpdate = false;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		String path = "/v1/projects";
		String parameters = "includeFinished=false";
		String errorMessage = "";
		int importCount = 0;
		int editCount = 0;
		ArrayList<Project> projects = new ArrayList<Project>();
		Boolean hasContent = ObjectDAO.hasContent(t.getSoftwareToken(), "projects");
		if (date != null && hasContent) {
			parameters += "&changedFromDate=" + getDateMinHour(date);
		}
		
		jsonArray = (JSONArray) getJSON(t.getAccessToken(), parameters, path, "array");
		logger.info("Projects response JSONArray " + jsonArray);
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObj = jsonArray.getJSONObject(i);
				String id = jsonObj.getString("Id");
				String number = jsonObj.optString("Number");
				String name = jsonObj.getString("Name");
				String startDate = jsonObj.getString("StartDate");
				String endDate = jsonObj.optString("EndDate", "");
				// String modified = jsonObj.getString("ModifiedUtc");
				String description = jsonObj.getString("Notes");
				// String status = jsonObj.getInt("Status") + "";
				String debtorId = jsonObj.optString("CustomerId", "<leeg>");
				String debtorNr = "<leeg>";
				if (!debtorId.equals("<leeg>")) {
					path = "/v1/customers/" + debtorId;
					parameters = "";
					JSONObject debtorDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path, "object");
					logger.info("Projects response debtorDetails " + debtorDetails);
					debtorNr = debtorDetails.getString("CustomerNumber");
				}
				if (!endDate.equals("")) {
					endDate = convertDate(endDate, "yyyyMMdd");
				}
				Project p = new Project(number, id, debtorNr, "Lopend", name, convertDate(startDate, "yyyyMMdd"),
						endDate, description, 0, 1, null);
				projects.add(p);
				importCount++;
			}
			System.out.println("PROJECTS " + projects.toString());
		}
		if (!projects.isEmpty())
		
		{
			int successAmount = (int) WorkOrderHandler.addData(t.getSoftwareToken(), projects, "projects", softwareName,
					null);
			if (successAmount > 0) {
				ObjectDAO.saveProjects(projects, t.getSoftwareToken());
				errorMessage += importCount + " projects imported<br>";
				errorMessage += "and " + editCount + " projects edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with projects<br>";
			}
		} else {
			errorMessage += "No projects for import<br>";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}
	
	// Set verkooporder status after sending it to WerkbonApp
	public void setOrderStatus(Token t, String id, Boolean accepted)
			throws JSONException, IOException, URISyntaxException {
		String path = "/v1/orders/" + id;
		String parameters = "";
		// OrderDetails
		JSONObject orderDetails = (JSONObject) getJSON(t.getAccessToken(), parameters, path, "object");
		logger.info("orderDetails response " + orderDetails);
		
		// int status = orderDetails.getInt("Status");
		Double amount = orderDetails.getDouble("Amount");
		String customerId = orderDetails.getString("CustomerId");
		double vatAmount = orderDetails.getDouble("VatAmount");
		int roundingsAmount = orderDetails.getInt("RoundingsAmount");
		boolean euThirdParty = orderDetails.getBoolean("EuThirdParty");
		boolean customerIsPrivatePerson = orderDetails.getBoolean("CustomerIsPrivatePerson");
		String orderDate = orderDetails.getString("OrderDate");
		boolean reverseChargeOnConstructionServices = orderDetails.getBoolean("ReverseChargeOnConstructionServices");
		String currencyCode = orderDetails.getString("CurrencyCode");
		String invoiceCity = orderDetails.getString("InvoiceCity");
		String invoiceCountryCode = orderDetails.getString("InvoiceCountryCode");
		String invoiceCustomerName = orderDetails.getString("InvoiceCustomerName");
		String invoicePostalCode = orderDetails.getString("InvoicePostalCode");
		int rotReducedInvoicingType = orderDetails.getInt("RotReducedInvoicingType");
		JSONArray rows = orderDetails.getJSONArray("Rows");
		ArrayList<Material> materials = new ArrayList<Material>();
		for (int j = 0; j < rows.length(); j++) {
			JSONObject rowDetails = rows.getJSONObject(j);
			
			// Abuse material object to set data
			String productId = rowDetails.optString("ArticleId", "null");
			String isWorkCost = rowDetails.getBoolean("IsWorkCost") + "";
			String isVatFree = rowDetails.getBoolean("IsVatFree") + "";
			String text = rowDetails.getString("Text");
			String isTextRow = rowDetails.getBoolean("IsTextRow") + "";
			Double quantity = rowDetails.getDouble("Quantity");
			Double price = rowDetails.getDouble("UnitPrice");
			Material m = new Material(productId, isWorkCost, isVatFree, text, price, quantity+"", isTextRow);
			materials.add(m);
			
		}
		JSONObject JSONObject = new JSONObject();
		try {
			// Set status to 3
			JSONObject.put("Status", 3);
			JSONObject.put("Amount", amount);
			JSONObject.put("CustomerId", customerId);
			JSONObject.put("VatAmount", vatAmount);
			JSONObject.put("RoundingsAmount", roundingsAmount);
			JSONObject.put("EuThirdParty", euThirdParty);
			JSONObject.put("CustomerIsPrivatePerson", customerIsPrivatePerson);
			JSONObject.put("OrderDate", orderDate);
			JSONObject.put("ReverseChargeOnConstructionServices", reverseChargeOnConstructionServices);
			JSONObject.put("CurrencyCode", currencyCode);
			JSONObject.put("InvoiceCity", invoiceCity);
			JSONObject.put("InvoiceCountryCode", invoiceCountryCode);
			JSONObject.put("InvoiceCustomerName", invoiceCustomerName);
			JSONObject.put("InvoicePostalCode", invoicePostalCode);
			JSONObject.put("RotReducedInvoicingType", rotReducedInvoicingType);
			JSONObject.put("OrderDate", orderDate);
			JSONObject.put("ShippedDateTime", getCurrentDate(null));
			JSONArray jsonArray = new JSONArray();
			int lineNumber = 0;
			for (Material m : materials) {
				JSONObject json = new JSONObject();
				json.put("ArticleId", m.getCode());
				json.put("LineNumber", lineNumber);
				json.put("IsWorkCost", Boolean.parseBoolean(m.getSubCode()));
				json.put("IsVatFree", Boolean.parseBoolean(m.getUnit()));
				json.put("IsTextRow", Boolean.parseBoolean(m.getModified()));
				json.put("Text", m.getDescription());
				json.put("DeliveredQuantity", m.getQuantity());
				json.put("UnitPrice", m.getPrice());
				json.put("Quantity", m.getQuantity());
				jsonArray.put(json);
				lineNumber ++;
			}
			JSONObject.put("Rows", jsonArray);
			System.out.println("JSONObject setOrderStatus" + JSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSONObject setOrderResponse = (JSONObject) putJSON(t.getAccessToken(), JSONObject, path, "object");
		System.out.println("setOrderResponse " + setOrderResponse);
	}
	
	// public String[] setFactuur(Token t, String date, Settings set) {
	// int exportAmount = 0;
	// // Get WorkOrders
	// ArrayList<WorkOrder> allData =
	// WorkOrderHandler.getData(t.getSoftwareToken(), "GetWorkorders",
	// set.getFactuurType(), false, softwareName);
	// for (WorkOrder w : allData) {
	// exportAmount++;
	// JSONObject JSONObject = factuurJSON(w, t, set.getRoundedHours());
	// // Send invoice
	// errorDetails += sendFactuur(w, clientToken, roundedHours, token, "", "");
	// }
	// return null;
	// }
	//
	
	// Create verkoopfactuur
//	public String[] setFactuur(Token t, Settings set, String date)
//			throws JSONException, IOException, URISyntaxException {
//		// Get WorkOrders
//		int exportAmount = 0;
//		JSONObject JSONObject = new JSONObject();
//		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(t.getSoftwareToken(), "GetWorkorders",
//				set.getExportWerkbontype(), false, softwareName);
//		for (WorkOrder w : allData) {
//			exportAmount++;
//			JSONObject = factuurJSON(w, t, set.getRoundedHours());
//			for (Relation r : w.getRelations()) {
//				Address a = r.getAddressess().get(0);
//				if (a.getType().equals("invoice")) {
//					
//				}
//			}
//		}
//		return null;
//	}
//	
//	public JSONObject factuurJSON(WorkOrder w, Token t, int roundedHours) {
//		JSONArray JSONArray = null;
//		for (Relation r : w.getRelations()) {
//			Address a = r.getAddressess().get(0);
//			if (a.getType().equals("invoice")) {
//				JSONArray = new JSONArray();
//				// Map date
//				String workDate = null;
//				try {
//					SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
//					Date formatDate = dt.parse(w.getWorkDate());
//					SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//					workDate = dt1.format(formatDate);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//				JSONObject JSONObject = new JSONObject();
//				try {
//					JSONObject.put("EuThirdParty", false);
//					JSONObject.put("CurrencyCode", "EUR");
//					JSONObject.put("TotalAmount", "EUR");
//					// Set status to 3
//					JSONObject.put("EU", 3);
//					JSONObject.put("Amount", amount);
//					JSONObject.put("CustomerId", customerId);
//					JSONObject.put("VatAmount", vatAmount);
//					JSONObject.put("RoundingsAmount", roundingsAmount);
//					
//					JSONObject.put("CustomerIsPrivatePerson", customerIsPrivatePerson);
//					JSONObject.put("OrderDate", orderDate);
//					JSONObject.put("ReverseChargeOnConstructionServices", reverseChargeOnConstructionServices);
//					JSONObject.put("CurrencyCode", currencyCode);
//					JSONObject.put("InvoiceCity", invoiceCity);
//					JSONObject.put("InvoiceCountryCode", invoiceCountryCode);
//					JSONObject.put("InvoiceCustomerName", invoiceCustomerName);
//					JSONObject.put("InvoicePostalCode", invoicePostalCode);
//					JSONObject.put("RotReducedInvoicingType", rotReducedInvoicingType);
//					JSONObject.put("OrderDate", orderDate);
//					JSONObject.put("ShippedDateTime", getCurrentDate(null));
//					JSONArray jsonArray = new JSONArray();
//					for (Material m : materials) {
//						JSONObject json = new JSONObject();
//						json.put("ArticleId", m.getCode());
//						json.put("LineNumber", Integer.parseInt(m.getQuantity()));
//						json.put("IsWorkCost", Boolean.parseBoolean(m.getSubCode()));
//						json.put("IsVatFree", Boolean.parseBoolean(m.getUnit()));
//						json.put("IsTextRow", Boolean.parseBoolean(m.getModified()));
//						json.put("Text", m.getDescription());
//						json.put("DeliveredQuantity", (int) m.getPrice());
//						json.put("Quantity", (int) m.getPrice());
//						jsonArray.put(json);
//					}
//					for
//					JSONObject.put("Rows", jsonArray);
//					System.out.println("JSONObject setOrderStatus" + JSONObject);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return JSONObject;
//	}
}