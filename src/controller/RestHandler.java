package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import object.rest.Address;
import object.rest.Employee;
import object.rest.HourType;
import object.rest.Material;
import object.rest.Project;
import object.rest.Relation;
import object.rest.WorkOrder;

public class RestHandler {
	private static String version = "7";
	final static String softwareToken = "622a8ef3a712344ef07a4427550ae1e2b38e5342";

	public static int checkToken(String token) {
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/employees/?token=" + token + "&software_token="
				+ softwareToken;
		int code = 0;
		String output = null;
		try {
			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			while ((output = br.readLine()) != null) {

				JSONObject json = new JSONObject(output);
				code = json.getInt("code");
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return code;

	}
	public static void setWorkorderStatus(String id, String workorderNr, Boolean status, String type, String token){
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken + "&row_id=" + id + "&update_status=" + status;
		URL url;
		String output = null;
		try {
			url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			while ((output = br.readLine()) != null) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public static ArrayList<WorkOrder> getData(String token, String type, String stat, boolean updateStatus) {
		// Header
		// ProjectNr, performancedate, invoiceaddressnumber,
		// deliveraddressnumber, customercode, status, paymentmethod(cash, bank,
		// cheque, cashondelivery, da)
		String projectNr, workDate = null, customerEmailInvoice, customerEmail, customerDebtorNr, status, paymentMethod,
				creationDate, id, workorderNr;
		String employeeNr = null, hourType = null, description = null, duration = null;
		// line
		String materialCode, materialNr, materialUnit, materialName;
		double materialPrice;
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken + "&status=" + stat + "&update_status=" + updateStatus;
		String output = null;
		ArrayList<WorkOrder> allData = null;
		try {
			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			while ((output = br.readLine()) != null) {
				WorkOrder w = null;
				JSONObject json = new JSONObject(output);
				if (json.getInt("code") == 200) {
					allData = new ArrayList<WorkOrder>();
					JSONArray array = json.getJSONArray("object");
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = array.getJSONObject(i);
						projectNr = object.getString("ProjectNr");
						id = object.getString("id");
						workorderNr	= object.getString("WorkorderNo");
						if (projectNr.equals("")) {
							workDate = object.getString("WorkDate");
							customerEmailInvoice = object.getString("CustomerEmailInvoice");
							customerEmail = object.getString("CustomerEmail");
							customerDebtorNr = object.getString("CustomerDebtorNr");
							System.out.println("costumdbnr " + customerDebtorNr);
							employeeNr = object.getString("EmployeeNr");
							status = object.getString("status");
							creationDate = object.getString("CreationDate");
							switch (status) {
							case "Afgehandeld":
								status = "final";
								break;
							case "Klaargezet":
								status = "concept";
								break;
							default:
								status = "concept";
								break;
							}
							paymentMethod = object.getString("PaymentMethod");
							switch (paymentMethod) {
							case "pin betaling":
								paymentMethod = "bank";
								break;
							case "contant voldaan":
								paymentMethod = "cash";
								break;
							default:
								paymentMethod = "bank";
								break;
							}
							JSONArray materials = object.getJSONArray("materials");
							ArrayList<Material> alleMaterials = new ArrayList<Material>();
							for (int j = 0; j < materials.length(); j++) {
								JSONObject material = materials.getJSONObject(j);
								materialCode = material.getString("MaterialCode");
								materialNr = material.getString("MaterialNr");
								materialUnit = material.getString("MaterialUnit");
								materialName = material.getString("MaterialName");
								materialPrice = material.getDouble("MaterialPrice");
								Material m = new Material(materialCode, materialCode, materialUnit, materialName,
										materialPrice, materialNr);
								alleMaterials.add(m);
							}
							w = new WorkOrder(projectNr, workDate, customerEmailInvoice, customerEmail,
									customerDebtorNr, status, paymentMethod, alleMaterials, creationDate, id, workorderNr);
							allData.add(w);
						} else {
							JSONArray periods = object.getJSONArray("workperiods");

							if (periods.length() > 0) {
								for (int j = 0; j < periods.length(); j++) {
									JSONObject period = periods.getJSONObject(j);
									employeeNr = period.getString("EmployeeNr");
									hourType = period.getString("HourType");
									workDate = period.getString("WorkDate");
									description = period.getString("WorkRemark");
									duration = period.getString("TotalTime");
									w = new WorkOrder(employeeNr, hourType, workDate, projectNr, description, duration, id);
									allData.add(w);
								}
							}
						}
					}
				}
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return allData;

	}

	public static boolean addData(String token, Object array, String type) throws ServletException, IOException {
		boolean b = false;
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken;
		URL url = new URL(link);
		String input = null;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		switch (type) {
		case "employees":
			input = employeeInput(array);
			break;
		case "projects":
			input = projectInput(array);
			break;
		case "relations":
			input = relationInput(array);
			break;
		case "materials":
			input = materialInput(array);
			break;
		case "hourtypes":
			input = hourtypeInput(array);
			break;
		}

		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes("UTF-8"));
		os.flush();

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output;
		System.out.println("Output from Server .... \n");
		

		while ((output = br.readLine()) != null) {
			System.out.println(output);
			try {
				JSONObject json = new JSONObject(output);
				if (json.getInt("code") == 200) {
					b = true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		conn.disconnect();
		return b;

	}

	public static String employeeInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<Employee> array = (ArrayList<Employee>) obj;
		String input = "[";
		int i = 1;
		for (Employee e : array) {
			if (i == array.size()) {
				input += "{\"firstname\":\"" + e.getFirstName() + "\",\"lastname\":\"" + e.getLastName()
						+ "\",\"number\":\"" + e.getCode() + "\"}";
			} else {
				i++;
				input += "{\"firstname\":\"" + e.getFirstName() + "\",\"lastname\":\"" + e.getLastName()
						+ "\",\"number\":\"" + e.getCode() + "\"},";
			}
		}
		return input += "]";
	}

	public static String projectInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<Project> array = (ArrayList<Project>) obj;
		String input = "[";
		int i = 1;
		for (Project p : array) {
			if (i == array.size()) {
				input += "{\"code\":\"" + p.getCode() + "\",\"code_ext\":\"" + "leeg" + "\",\"debtor_number\":\""
						+ p.getDebtor_number() + "\",\"status\":\"" + p.getStatus() + "\",\"name\":\"" + p.getName()
						+ "\",\"description\":\"" + p.getDescription() + "\",\"progress\":\"" + p.getProgress()
						+ "\",\"date_start\":\"" + p.getDate_start() + "\",\"date_end\":\"" + p.getDate_end()
						+ "\",\"active\":\"" + p.getActive() + "\"}";
			} else {
				input += "{\"code\":\"" + p.getCode() + "\",\"code_ext\":\"" + "leeg" + "\",\"debtor_number\":\""
						+ p.getDebtor_number() + "\",\"status\":\"" + p.getStatus() + "\",\"name\":\"" + p.getName()
						+ "\",\"description\":\"" + p.getDescription() + "\",\"progress\":\"" + p.getProgress()
						+ "\",\"date_start\":\"" + p.getDate_start() + "\",\"date_end\":\"" + p.getDate_end()
						+ "\",\"active\":\"" + p.getActive() + "\"},";

				i++;
			}
		}
		return input += "]";
	}

	public static String relationInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<Relation> array = (ArrayList<Relation>) obj;
		String input = "[";
		int i = 0;
		for (Relation r : array) {
			i++;
			// posts the same relation with different addresses
			int j = 0;
			for (Address a : r.getAddressess()) {
				j++;
				if (i == array.size() && j == r.getAddressess().size()) {
					input += "{\"name\":\"" + r.getName() + "\",\"debtor_number\":\"" + r.getDebtorNumber()
							+ "\",\"contact\":\"" + r.getContact() + "\",\"phone_number\":\"" + a.getPhoneNumber()
							+ "\",\"email\":\"" + a.getEmail() + "\",\"email_workorder\":\"" + r.getEmailWorkorder()
							+ "\",\"street\":\"" + a.getStreet() + "\",\"house_number\":\"" + a.getHouseNumber()
							+ "\",\"postal_code\":\"" + a.getPostalCode() + "\",\"city\":\"" + a.getCity()
							+ "\",\"remark\":\"" + a.getRemark() + "\"}";
				} else {
					input += "{\"name\":\"" + r.getName() + "\",\"debtor_number\":\"" + r.getDebtorNumber()
							+ "\",\"contact\":\"" + "leeg" + "\",\"phone_number\":\"" + a.getPhoneNumber()
							+ "\",\"email\":\"" + a.getEmail() + "\",\"email_workorder\":\"" + r.getEmailWorkorder()
							+ "\",\"street\":\"" + a.getStreet() + "\",\"house_number\":\"" + a.getHouseNumber()
							+ "\",\"postal_code\":\"" + a.getPostalCode() + "\",\"city\":\"" + a.getCity()
							+ "\",\"remark\":\"" + a.getRemark() + "\"},";
				}
			}
		}
		return input += "]";

	}

	public static String materialInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<Material> array = (ArrayList<Material>) obj;
		String input = "[";
		int i = 1;
		for (Material m : array) {
			String code = m.getCode();

			if (i == array.size()) {
				input += "{\"code\":\"" + code + "\",\"description\":\"" + m.getDescription() + "\",\"price\":\""
						+ m.getPrice() + "\",\"unit\":\"" + m.getUnit() + "\"}";
			} else {
				i++;
				input += "{\"code\":\"" + code + "\",\"description\":\"" + m.getDescription() + "\",\"price\":\""
						+ m.getPrice() + "\",\"unit\":\"" + m.getUnit() + "\"},";
			}
		}
		return input += "]";
	}

	public static String hourtypeInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<HourType> array = (ArrayList<HourType>) obj;
		String input = "[";
		int i = 1;
		for (HourType h : array) {
			if (i == array.size()) {
				input += "{\"code\":\"" + h.getCode() + "\",\"name\":\"" + h.getName() + "\",\"cost_booking\":\""
						+ h.getCostBooking() + "\",\"sale_booking\":\"" + h.getSaleBooking() + "\",\"cost_price\":\""
						+ h.getCostPrice() + "\",\"sale_price\":\"" + h.getSalePrice() + "\",\"active\":\""
						+ h.getActive() + "\"}";
			} else {
				i++;
				input += "{\"code\":\"" + h.getCode() + "\",\"name\":\"" + h.getName() + "\",\"cost_booking\":\""
						+ h.getCostBooking() + "\",\"sale_booking\":\"" + h.getSaleBooking() + "\",\"cost_price\":\""
						+ h.getCostPrice() + "\",\"sale_price\":\"" + h.getSalePrice() + "\",\"active\":\""
						+ h.getActive() + "\"},";
			}
		}
		return input += "]";
	}
}
