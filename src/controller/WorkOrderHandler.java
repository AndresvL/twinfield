package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import DBUtil.DBConnection;
import object.workorder.Address;
import object.workorder.Employee;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Project;
import object.workorder.Relation;
import object.workorder.WorkOrder;
import object.workorder.WorkPeriod;

//In this class you'll find all the methodes that are used to communicate with WBA
public class WorkOrderHandler {
	private static String version = "8";
	// WorkOrder Api key
	// Env variable!
	// TWINFIELD
	// final static String softwareToken =
	// "622a8ef3a712344ef07a4427550ae1e2b38e5342";
	// WEFACT
	final static String softwareToken = "872e5ad04c2607e59ba610712344ef07a4427550ae09bc33f1120a20ffe4";

	// change later
	public static int checkWorkOrderToken(String token, String softwareName) {
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/employees/?token=" + token + "&software_token="
				+ softwareToken;
		if (System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) != null) {
			link = "https://www.werkbonapp.nl/openapi/" + version + "/employees/?token=" + token + "&software_token="
					+ System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase());
		}
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

	public static void setWorkorderStatus(String id, String workorderNr, Boolean status, String type, String token,
			String softwareName) {
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken + "&row_id=" + id + "&update_status=" + status;
		if (System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) != null) {
			link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token + "&software_token="
					+ System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) + "&row_id=" + id + "&update_status="
					+ status;
		}
		URL url;
		try {
			url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null) {
				// System.out.println("setWorkorderStatus " + output);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static ArrayList<WorkOrder> getData(String token, String type, String stat, boolean updateStatus,
			String softwareName) {
		// Header
		// ProjectNr, performancedate, invoiceaddressnumber,
		// deliveraddressnumber, customercode, status, paymentmethod(cash, bank,
		// cheque, cashondelivery, da)
		// all content from getWorkorder
		String projectNr, workDate = null, customerEmailInvoice, customerEmail, customerDebtorNr, status, paymentMethod,
				creationDate, id, orderNr, workTime, workEndDate, workEndTime, externProjectNr, customerName,
				customerStreet, customerStreetNo, customerZIP, customerCity, customerContactPerson, customerPhone,
				customerRemark, customerNameInvoice, customerDebtorNrInvoice, customerStreetInvoice,
				customerStreetNoInvoice, customerZIPInvoice, customerContactPersonInvoice, customerPhoneInvoice,
				customerRemarkInvoice, typeOfWork, workDescription, beginTime, endTime, customerCityInvoice, pdfUrl, workStatus;
		String employeeNr = null, hourType = null, description = null, duration = null;
		// line
		String materialCode, materialNr, materialUnit, materialName;
		double materialPrice;

		// Request to WorkOrderApp
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken + "&status=" + stat + "&update_status=" + updateStatus;
		if (System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) != null) {
			link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token + "&software_token="
					+ System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) + "&status=" + stat
					+ "&update_status=" + updateStatus;
		}
		String output = null;
		ArrayList<WorkOrder> allData = null;
		System.out.println("LINK " + link);
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
					JSONArray array = json.getJSONArray("response");
					for (int i = 0; i < array.length(); i++) {
						// only choose recent workorders(not archieved or
						// deleted)
						JSONObject object = array.getJSONObject(i);
						if (object.getInt("Archived") == 0) {
							id = object.getString("id");
							orderNr = object.getString("OrderNr");
							workDate = object.getString("WorkDate");
							workTime = object.getString("WorkTime");
							workEndDate = object.getString("WorkEndDate");
							workEndTime = object.getString("WorkEndTime");
							projectNr = object.getString("ProjectNr");
							externProjectNr = object.getString("ExternProjectNr");
							customerName = object.getString("CustomerName");
							customerDebtorNr = object.getString("CustomerDebtorNr");
							customerStreet = object.getString("CustomerStreet");
							customerStreetNo = object.getString("CustomerStreetNo");
							customerEmail = object.getString("CustomerEmail");
							customerZIP = object.getString("CustomerZIP");
							customerCity = object.getString("CustomerCity");
							customerContactPerson = object.getString("CustomerContactPerson");
							customerPhone = object.getString("CustomerPhone");
							customerRemark = object.getString("CustomerRemark");
							customerNameInvoice = object.getString("CustomerNameInvoice");
							customerDebtorNrInvoice = object.getString("CustomerDebtorNrInvoice");
							customerStreetInvoice = object.getString("CustomerStreetInvoice");
							customerStreetNoInvoice = object.getString("CustomerStreetNoInvoice");
							customerEmailInvoice = object.getString("CustomerEmailInvoice");
							customerZIPInvoice = object.getString("CustomerZIPInvoice");
							customerCityInvoice = object.getString("CustomerCityInvoice");
							customerContactPersonInvoice = object.getString("CustomerContactPersonInvoice");
							customerPhoneInvoice = object.getString("CustomerPhoneInvoice");
							customerRemarkInvoice = object.getString("CustomerRemarkInvoice");
							typeOfWork = object.getString("TypeOfWork");
							workDescription = object.getString("WorkDescription");
							paymentMethod = object.getString("PaymentMethod");
							creationDate = object.getString("CreationDate");
							employeeNr = object.getString("EmployeeNr");
							status = object.getString("status");
							creationDate = object.getString("CreationDate");
							pdfUrl = object.getString("PdfUrl");
							workStatus = object.getString("WorkStatus");
							// Set postal and invoice relation
							ArrayList<Relation> allRelations = new ArrayList<Relation>();
							ArrayList<Address> invoiceAddress = new ArrayList<Address>();
							// id 1 is invoice
							Address invoice = new Address(customerContactPersonInvoice, customerPhoneInvoice,
									customerEmailInvoice, customerStreetInvoice, customerStreetNoInvoice,
									customerZIPInvoice, customerCityInvoice, customerRemarkInvoice, "invoice", 1);
							invoiceAddress.add(invoice);
							Relation customerRelationInvoice = new Relation(customerNameInvoice,
									customerDebtorNrInvoice, customerContactPersonInvoice, customerEmailInvoice,
									invoiceAddress, null);

							ArrayList<Address> postaleAddress = new ArrayList<Address>();
							// id 2 is postal
							Address postal = new Address(customerContactPerson, customerPhone, customerEmail,
									customerStreet, customerStreetNo, customerZIP, customerCity, customerRemark,
									"postal", 2);
							postaleAddress.add(postal);
							Relation customerRelationPostal = new Relation(customerName, customerDebtorNr,
									customerContactPerson, customerEmail, postaleAddress, null);
							// add Relations to ArrayList
							allRelations.add(customerRelationInvoice);
							allRelations.add(customerRelationPostal);

							// workperiods
							ArrayList<WorkPeriod> allWorkPeriods = new ArrayList<WorkPeriod>();
							JSONArray periods = object.getJSONArray("Workperiods");
							if (periods.length() > 0) {
								for (int j = 0; j < periods.length(); j++) {
									WorkPeriod work = null;
									JSONObject period = periods.getJSONObject(j);
									beginTime = period.getString("BeginTime");
									duration = period.getString("TotalTime");
									description = period.getString("WorkRemark");
									workDate = period.getString("WorkDate");
									endTime = period.getString("EndTime");
									// travel = period.getString("Travel");
									employeeNr = period.getString("EmployeeNr");
									hourType = period.getString("HourType");
									work = new WorkPeriod(employeeNr, hourType, workDate, projectNr, description,
											duration, id, beginTime, endTime);
									allWorkPeriods.add(work);
								}
							}
							// materials
							JSONArray materials = object.getJSONArray("Materials");
							ArrayList<Material> alleMaterials = new ArrayList<Material>();
							Connection con = DBConnection.createDatabaseConnection();
							for (int j = 0; j < materials.length(); j++) {
								JSONObject material = materials.getJSONObject(j);
								materialCode = material.getString("MaterialCode");
								materialNr = material.getString("MaterialNr");
								materialUnit = material.getString("MaterialUnit");
								materialName = material.getString("MaterialName");
								materialPrice = material.getDouble("MaterialPrice");
								// Get material from db
								Material sub = ObjectDAO.getMaterials(token, materialCode, materialName);
								Material m = null;
								if (sub != null) {
									// Check if material has subCode
									if (sub.getSubCode() != null) {
										m = new Material(sub.getCode(), materialCode, materialUnit, materialName,
												materialPrice, materialNr, null);
									} else {
										m = new Material(materialCode, null, materialUnit, materialName, materialPrice,
												materialNr, null);
									}
								} else {
									m = new Material(materialCode, null, materialUnit, materialName, materialPrice,
											materialNr, null);
								}

								alleMaterials.add(m);
							}

							w = new WorkOrder(projectNr, workDate, customerEmailInvoice, customerEmail,
									customerDebtorNr, status, paymentMethod, alleMaterials, creationDate, id, orderNr,
									allWorkPeriods, allRelations, workTime, workEndDate, workEndTime, externProjectNr,
									typeOfWork, workDescription, null, pdfUrl, workStatus);
							allData.add(w);
							con.close();
						}
					}
				}
			}
		} catch (IOException | JSONException | SQLException e) {
			e.printStackTrace();
		}
		return allData;

	}

	public static Object addData(String token, Object array, String type, String softwareName, String clientToken)
			throws ServletException, IOException {
		JSONObject completeJSON = new JSONObject();
		Object amount = 0;
		String link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token
				+ "&software_token=" + softwareToken;
		if (System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase()) != null) {
			link = "https://www.werkbonapp.nl/openapi/" + version + "/" + type + "/?token=" + token + "&software_token="
					+ System.getenv("SOFTWARETOKEN_" + softwareName.toUpperCase());
		}
		URL url = new URL(link);
		String input = null;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		switch (type) {
		case "employees":
			input = employeeInput(array) + "";
			break;
		case "projects":
			input = projectInput(array) + "";
			break;
		case "relations":
			input = relationInput(array) + "";
			break;
		case "materials":
				input = materialInput(array) + "";
			break;
		case "hourtypes":
			input = hourtypeInput(array) + "";
			break;
		case "PostWorkorders":
			input = workorderInput(array) + "";
			break;
		case "workstatusses":
			//Array is JSONObject
			input = array + "";
			break;
		}
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes("UTF-8"));
		System.out.println("input " + input);
		os.flush();

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output;
		System.out.println("Output from Server .... \n");

		while ((output = br.readLine()) != null) {
			System.out.println("OUTPUT " + output + " type " + type);
			try {
				JSONObject json = new JSONObject(output);
				if (!json.isNull("response")) {
					// Check if array exist
					if (json.optJSONArray("response") != null) {
						amount = json.getJSONArray("response");
					} else {
						amount = json.getInt("response");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		conn.disconnect();
		return amount;

	}

	public static JSONArray employeeInput(Object obj) {
		JSONArray JSONArray = new JSONArray();
		JSONObject JSONObject = null;
		@SuppressWarnings("unchecked")
		ArrayList<Employee> array = (ArrayList<Employee>) obj;
		for (Employee emp : array) {
		JSONObject = new JSONObject();
		try {
			JSONObject.put("firstname", emp.getFirstName());
			JSONObject.put("lastname", emp.getLastName());
			JSONObject.put("number", emp.getCode());
			JSONArray.put(JSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	return JSONArray;
	}

	public static JSONArray projectInput(Object obj) {
		JSONArray JSONArray = new JSONArray();
		JSONObject JSONObject = null;
		@SuppressWarnings("unchecked")
		ArrayList<Project> array = (ArrayList<Project>) obj;
		for (Project p : array) {
			JSONObject = new JSONObject();
			try {
				JSONObject.put("code", p.getCode());
				JSONObject.put("code_ext", "<leeg>");
				JSONObject.put("debtor_number", p.getDebtorNumber());
				JSONObject.put("status", p.getStatus());
				JSONObject.put("name", p.getName());
				JSONObject.put("description", p.getDescription());
				JSONObject.put("progress", p.getProgress());
				JSONObject.put("date_start", p.getDate_start());
				JSONObject.put("date_end", p.getDate_end());
				JSONObject.put("active", p.getActive());
				JSONArray.put(JSONObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return JSONArray;
	}

	public static JSONArray relationInput(Object obj) {
		JSONArray JSONArray = new JSONArray();
		JSONObject JSONObject = null;
		@SuppressWarnings("unchecked")
		ArrayList<Relation> array = (ArrayList<Relation>) obj;
		for (Relation r : array) {
			//Post the same relation with different addresses
			for (Address a : r.getAddressess()) {
				JSONObject = new JSONObject();
				try {
					JSONObject.put("name", r.getCompanyName());
					JSONObject.put("debtor_number", r.getDebtorNumber());
					JSONObject.put("contact", a.getName());
					JSONObject.put("phone_number", a.getPhoneNumber());
					JSONObject.put("email", a.getEmail());
					JSONObject.put("email_workorder", r.getEmailWorkorder());
					JSONObject.put("street", a.getStreet());
					JSONObject.put("house_number", a.getHouseNumber());
					JSONObject.put("postal_code", a.getPostalCode());
					JSONObject.put("city", a.getCity());
					JSONObject.put("remark", a.getRemark());
					JSONArray.put(JSONObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return JSONArray;
	}
	
	//Create JSONArray from Materials
	public static JSONArray materialInput(Object obj){
		JSONArray JSONArray = new JSONArray();
		JSONObject JSONObject = null;
		@SuppressWarnings("unchecked")
		ArrayList<Material> array = (ArrayList<Material>) obj;
		for (Material m : array) {
			JSONObject = new JSONObject();
			String code = null;
			//Set code with subCode if subCode != null
			if (m.getSubCode() != null && !m.getSubCode().equals("")) {
				code = m.getSubCode();
			} else {
				code = m.getCode();
			}
			try {
				JSONObject.put("code", code);
				JSONObject.put("description", m.getDescription());
				JSONObject.put("price", m.getPrice());
				JSONObject.put("unit", m.getUnit());
				JSONArray.put(JSONObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return JSONArray;
	}

	public static JSONArray hourtypeInput(Object obj) {
		JSONArray JSONArray = new JSONArray();
		JSONObject JSONObject = null;
		@SuppressWarnings("unchecked")
		ArrayList<HourType> array = (ArrayList<HourType>) obj;
		for (HourType h : array) {
			JSONObject = new JSONObject();
			try {
				JSONObject.put("code", h.getCode());
				JSONObject.put("name", h.getName());
				JSONObject.put("cost_booking", h.getCostBooking());
				JSONObject.put("sale_booking", h.getSaleBooking());
				JSONObject.put("cost_price", h.getCostPrice());
				JSONObject.put("sale_price", h.getSalePrice());
				JSONObject.put("active", h.getActive());
				JSONArray.put(JSONObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return JSONArray;
	}

	public static JSONArray workorderInput(Object obj) {
		JSONArray JSONArray = new JSONArray();
		JSONArray JSONArrayMaterials = null;
		JSONObject JSONObject = null;
		JSONObject JSONObjecMaterial = null;
		@SuppressWarnings("unchecked")
		ArrayList<WorkOrder> allWorkorders = (ArrayList<WorkOrder>) obj;
		for (WorkOrder w : allWorkorders) {
			Relation r = w.getRelations().get(0);
			Address a = r.getAddressess().get(0);
			JSONObject = new JSONObject();
			try {
				JSONObject.put("WorkorderNo", w.getWorkorderNr());
				JSONObject.put("ProjectNr", "");
				JSONObject.put("ExternProjectNr", w.getExternProjectNr());
				JSONObject.put("CustomerName", r.getCompanyName());
				JSONObject.put("CustomerDebtorNr", w.getCustomerDebtorNr());
				JSONObject.put("CustomerStreet",a.getStreet());
				JSONObject.put("CustomerEmail", w.getCustomerEmail());
				JSONObject.put("CustomerZIP", a.getPostalCode());
				JSONObject.put("CustomerCity", a.getCity());
				JSONObject.put("CustomerContactPerson", r.getContact());
				JSONObject.put("CustomerPhone", a.getPhoneNumber());
				JSONObject.put("CustomerRemark", a.getRemark());
				JSONObject.put("CustomerNameInvoice", r.getCompanyName());
				JSONObject.put("CustomerDebtorNrInvoice", r.getDebtorNumber());
				JSONObject.put("CustomerStreetInvoice", a.getStreet());
				JSONObject.put("CustomerEmailInvoice", w.getCustomerEmail());
				JSONObject.put("CustomerZIPInvoice", a.getPostalCode());
				JSONObject.put("CustomerCityInvoice", a.getCity());
				JSONObject.put("CustomerContactPersonInvoice", r.getContact());
				JSONObject.put("CustomerPhoneInvoice", a.getPhoneNumber());
				JSONObject.put("CustomerRemarkInvoice", a.getRemark());
				JSONObject.put("TypeOfWork", w.getTypeOfWork());
				JSONObject.put("WorkDescription", w.getWorkDescription());
				JSONObject.put("PaymentMethod", w.getPaymentMethod());
				JSONObject.put("WorkDate", w.getWorkDate());
				JSONArrayMaterials = new JSONArray();
				for (Material m : w.getMaterials()) {
					JSONObjecMaterial = new JSONObject();
					JSONObjecMaterial.put("MaterialCode", m.getCode());
					JSONObjecMaterial.put("MaterialNr",  m.getQuantity());
					JSONObjecMaterial.put("MaterialPrice", m.getPrice());
					JSONObjecMaterial.put("MaterialName", m.getDescription());
					JSONObjecMaterial.put("MaterialUnit", m.getUnit());
					JSONArrayMaterials.put(JSONObjecMaterial);
				}
				JSONObject.put("Materials", JSONArrayMaterials);
				JSONArray.put(JSONObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return JSONArray;
	}
}
