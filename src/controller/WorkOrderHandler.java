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
	final static String softwareToken = "622a8ef3a712344ef07a4427550ae1e2b38e5342";

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
				customerRemarkInvoice, typeOfWork, workDescription, beginTime, endTime, customerCityInvoice;
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
								}else{
									m = new Material(materialCode, null, materialUnit, materialName, materialPrice,
											materialNr, null);
								}
								
								alleMaterials.add(m);
							}

							w = new WorkOrder(projectNr, workDate, customerEmailInvoice, customerEmail,
									customerDebtorNr, status, paymentMethod, alleMaterials, creationDate, id, orderNr,
									allWorkPeriods, allRelations, workTime, workEndDate, workEndTime, externProjectNr,
									typeOfWork, workDescription, null);
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
		Object amount = null;
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
		case "PostWorkorders":
			input = workorderInput(array);
			break;
		}
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes("UTF-8"));
		os.flush();

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output;
		System.out.println("Output from Server .... \n");

		while ((output = br.readLine()) != null) {
			System.out.println("OUTPUT " + output + " type "+ type);
			try {
				JSONObject json = new JSONObject(output);
				if(!json.isNull("response")){
					//Check if array exist
					if(json.optJSONArray("response") != null){
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
						+ p.getDebtorNumber() + "\",\"status\":\"" + p.getStatus() + "\",\"name\":\"" + p.getName()
						+ "\",\"description\":\"" + p.getDescription() + "\",\"progress\":\"" + p.getProgress()
						+ "\",\"date_start\":\"" + p.getDate_start() + "\",\"date_end\":\"" + p.getDate_end()
						+ "\",\"active\":\"" + p.getActive() + "\"}";
			} else {
				input += "{\"code\":\"" + p.getCode() + "\",\"code_ext\":\"" + "leeg" + "\",\"debtor_number\":\""
						+ p.getDebtorNumber() + "\",\"status\":\"" + p.getStatus() + "\",\"name\":\"" + p.getName()
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
					input += "{\"name\":\"" + r.getCompanyName() + "\",\"debtor_number\":\"" + r.getDebtorNumber()
							+ "\",\"contact\":\"" + a.getName() + "\",\"phone_number\":\"" + a.getPhoneNumber()
							+ "\",\"email\":\"" + a.getEmail() + "\",\"email_workorder\":\"" + r.getEmailWorkorder()
							+ "\",\"street\":\"" + a.getStreet() + "\",\"house_number\":\"" + a.getHouseNumber()
							+ "\",\"postal_code\":\"" + a.getPostalCode() + "\",\"city\":\"" + a.getCity()
							+ "\",\"remark\":\"" + a.getRemark() + "\"}";
				} else {
					input += "{\"name\":\"" + r.getCompanyName() + "\",\"debtor_number\":\"" + r.getDebtorNumber()
							+ "\",\"contact\":\"" + a.getName() + "\",\"phone_number\":\"" + a.getPhoneNumber()
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
			String code = null;
			if (m.getSubCode() != null && !m.getSubCode().equals("")) {
				code = m.getSubCode();
			} else {
				code = m.getCode();
			}
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

	public static String workorderInput(Object obj) {
		@SuppressWarnings("unchecked")
		ArrayList<WorkOrder> allWorkorders = (ArrayList<WorkOrder>) obj;
		String input = "[";
		int i = 0;
		for (WorkOrder w : allWorkorders) {
			i++;
			Relation r = w.getRelations().get(0);
			Address a = r.getAddressess().get(0);
			if (i == allWorkorders.size()) {
				input += "{\"WorkorderNo\":\"" + w.getWorkorderNr() + "\",\"ProjectNr\":\"" + ""
						+ "\",\"ExternProjectNr\":\"" + w.getExternProjectNr() + "\",\"CustomerName\":\"" + r.getCompanyName()
						+ "\",\"CustomerDebtorNr\":\"" + w.getCustomerDebtorNr() + "\",\"CustomerStreet\":\""
						+ a.getStreet() + "\",\"CustomerEmail\":\"" + w.getCustomerEmail() + "\",\"CustomerZIP\":\""
						+ a.getPostalCode() + "\",\"CustomerCity\":\"" + a.getCity() + "\",\"CustomerContactPerson\":\""
						+ r.getContact() + "\",\"CustomerPhone\":\"" + a.getPhoneNumber() + "\",\"CustomerRemark\":\""
						+ a.getRemark() + "\",\"CustomerNameInvoice\":\"" + r.getCompanyName()
						+ "\",\"CustomerDebtorNrInvoice\":\"" + r.getDebtorNumber() + "\",\"CustomerStreetInvoice\":\""
						+ a.getStreet() + "\",\"CustomerEmailInvoice\":\"" + w.getCustomerEmail()
						+ "\",\"CustomerZIPInvoice\":\"" + a.getPostalCode() + "\",\"CustomerCityInvoice\":\""
						+ a.getCity() + "\",\"CustomerContactPersonInvoice\":\"" + r.getContact()
						+ "\",\"CustomerPhoneInvoice\":\"" + a.getPhoneNumber() + "\",\"CustomerRemarkInvoice\":\""
						+ a.getRemark() + "\",\"TypeOfWork\":\"" + w.getTypeOfWork() + "\",\"WorkDescription\":\""
						+ w.getWorkDescription() + "\",\"PaymentMethod\":\"" + w.getPaymentMethod() + "\",\"WorkDate\":\"" + w.getWorkDate() + "\",\"Materials\": [";
				int j = 1;
				for (Material m : w.getMaterials()) {
					if (j == w.getMaterials().size()) {
						input += "{\"MaterialCode\":\"" + m.getCode() + "\",\"MaterialNr\":\"" + m.getQuantity()
								+ "\",\"MaterialPrice\":\"" + m.getPrice() + "\",\"MaterialName\":\""
								+ m.getDescription() + "\",\"MaterialUnit\":\"" + m.getUnit() + "\"}";
					} else {
						j++;
						input += "{\"MaterialCode\":\"" + m.getCode() + "\",\"MaterialNr\":\"" + m.getQuantity()
								+ "\",\"MaterialPrice\":\"" + m.getPrice() + "\",\"MaterialName\":\""
								+ m.getDescription() + "\",\"MaterialUnit\":\"" + m.getUnit() + "\"},";
					}
				}
				input += "]}";
			} else {
				input += "{\"WorkorderNo\":\"" + w.getWorkorderNr() + "\",\"ProjectNr\":\"" + ""
						+ "\",\"ExternProjectNr\":\"" + w.getExternProjectNr() + "\",\"CustomerName\":\"" + r.getCompanyName()
						+ "\",\"CustomerDebtorNr\":\"" + w.getCustomerDebtorNr() + "\",\"CustomerStreet\":\""
						+ a.getStreet() + "\",\"CustomerEmail\":\"" + w.getCustomerEmail() + "\",\"CustomerZIP\":\""
						+ a.getPostalCode() + "\",\"CustomerCity\":\"" + a.getCity() + "\",\"CustomerContactPerson\":\""
						+ r.getContact() + "\",\"CustomerPhone\":\"" + a.getPhoneNumber() + "\",\"CustomerRemark\":\""
						+ a.getRemark() + "\",\"CustomerNameInvoice\":\"" + r.getCompanyName()
						+ "\",\"CustomerDebtorNrInvoice\":\"" + r.getDebtorNumber() + "\",\"CustomerStreetInvoice\":\""
						+ a.getStreet() + "\",\"CustomerEmailInvoice\":\"" + w.getCustomerEmail()
						+ "\",\"CustomerZIPInvoice\":\"" + a.getPostalCode() + "\",\"CustomerCityInvoice\":\""
						+ a.getCity() + "\",\"CustomerContactPersonInvoice\":\"" + r.getContact()
						+ "\",\"CustomerPhoneInvoice\":\"" + a.getPhoneNumber() + "\",\"CustomerRemarkInvoice\":\""
						+ a.getRemark() + "\",\"TypeOfWork\":\"" + w.getTypeOfWork() + "\",\"WorkDescription\":\""
						+ w.getWorkDescription() + "\",\"PaymentMethod\":\"" + w.getPaymentMethod() + "\",\"WorkDate\":\"" + w.getWorkDate() + "\",\"Materials\": [";
				int j = 1;
				for (Material m : w.getMaterials()) {
					if (j == w.getMaterials().size()) {
						input += "{\"MaterialCode\":\"" + m.getCode() + "\",\"MaterialNr\":\"" + m.getQuantity()
								+ "\",\"MaterialPrice\":\"" + m.getPrice() + "\",\"MaterialName\":\""
								+ m.getDescription() + "\",\"MaterialUnit\":\"" + m.getUnit() + "\"}";
					} else {
						j++;
						input += "{\"MaterialCode\":\"" + m.getCode() + "\",\"MaterialNr\":\"" + m.getQuantity()
								+ "\",\"MaterialPrice\":\"" + m.getPrice() + "\",\"MaterialName\":\""
								+ m.getDescription() + "\",\"MaterialUnit\":\"" + m.getUnit() + "\"},";
					}
				}
				input += "]},";

			}
		}
		return input += "]";
	}
}
