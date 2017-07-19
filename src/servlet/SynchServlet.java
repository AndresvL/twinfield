package servlet;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import DBUtil.DBConnection;
import controller.WorkOrderHandler;
import controller.eaccouting.EAccountingHandler;
import controller.eaccouting.OAuthEAccounting;
import controller.moloni.MoloniHandler;
import controller.moloni.OAuthMoloni;
import controller.twinfield.SoapHandler;
import controller.twinfield.TwinfieldHandler;
import controller.wefact.WeFactHandler;
import object.Settings;
import object.Token;

public class SynchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String redirect = System.getenv("REDIRECT");
	private String[] messageArray = null;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		ArrayList<Token> allTokens = null;
		// Sync data from current user
		if (softwareToken != null) {
			try {
				Token t = TokenDAO.getToken(softwareToken, softwareName);
				if (t != null && !t.getAccessSecret().equals("invalid")) {
					softwareName = t.getSoftwareName();
					// setSync(true) because user is online
					this.setSyncMethods(t, req, true);
					if (redirect != null) {
						resp.sendRedirect(
								redirect + "OAuth.do?token=" + softwareToken + "&softwareName=" + softwareName);
					} else {
						// CHANGE LATER FOR OTHER INTEGRATION!!
						resp.sendRedirect("https://www.localhost:8080/connect/OAuth.do?token=" + softwareToken
								+ "&softwareName=" + softwareName);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Get all users from database to Sync their data all at once
		} else {
			try {
				allTokens = TokenDAO.getSoftwareTokens();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String token = null;
			for (Token t : allTokens) {
				softwareName = t.getSoftwareName();
				token = t.getSoftwareToken();
				if (WorkOrderHandler.checkWorkOrderToken(token, softwareName) == 200) {
					try {
						setSyncMethods(t, null, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("A total of " + allTokens.size() + " users are found in database");
			int twinfieldCount = 0, wefactCount = 0, eaccountingCount = 0, moloniCount = 0;
			for (Token t : allTokens) {
				switch (t.getSoftwareName()) {
				case "Twinfield":
					twinfieldCount++;
					break;
				case "WeFact":
					wefactCount++;
					break;
				case "eAccounting":
					eaccountingCount++;
					break;
				case "Moloni":
					moloniCount++;
					break;
				}
			}
			System.out.println(twinfieldCount + " Twinfield users");
			System.out.println(wefactCount + " WeFact users");
			System.out.println(eaccountingCount + " eAccounting users");
			System.out.println(moloniCount + " Moloni users");
		}
	}
	
	public String getDate(String date) {
		String timestamp;
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (date != null) {
			timestamp = date;
		} else {
			timestamp = za.format(formatter);
		}
		return timestamp;
	}
	
	public void setSyncMethods(Token t, HttpServletRequest req, boolean loggedIn) throws Exception {
		String date = null;
		if (!loggedIn) {
			date = TokenDAO.getModifiedDate(t.getSoftwareToken());
		}
		switch (t.getSoftwareName()) {
		case "Twinfield":
			new TwinfieldThread(t, date).start();
			DBConnection.createDatabaseConnection(false);
			break;
		case "WeFact":
			new WeFactThread(t.getSoftwareToken(), t.getAccessToken(), date).start();
			DBConnection.createDatabaseConnection(false);
			break;
		case "eAccounting":
			new eAccountingThread(t, date).start();
			System.out.println("DATE " + date);
			DBConnection.createDatabaseConnection(false);
			break;
		case "Moloni":
			new MoloniThread(t, date).start();
			System.out.println("DATE " + date);
			DBConnection.createDatabaseConnection(false);
			break;
		default:
			break;
		}
		
	}
	
	public class TwinfieldThread extends Thread {
		Token t;
		String date;
		String errorMessage = "", errorDetails = "";
		String checkUpdate = "false";
		
		TwinfieldThread(Token t, String date) {
			this.t = t;
			this.date = date;
		}
		
		public void run() {
			try {
				System.out.println("Twinfield Thread Running");
				String session = null, cluster = null;
				TwinfieldHandler twinfield = new TwinfieldHandler();
				Settings set = ObjectDAO.getSettings(t.getSoftwareToken());
				if (set != null) {
					ArrayList<String> importTypes = set.getImportObjects();
					// Import section
					for (String type : importTypes) {
						String[] array = SoapHandler.getSession(t);
						if (array != null) {
							session = array[0];
							cluster = array[1];
							
							switch (type) {
							case "employees":
								messageArray = twinfield.getEmployees(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "projects":
								messageArray = twinfield.getProjects(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "materials":
								messageArray = twinfield.getMaterials(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "relations":
								messageArray = twinfield.getRelations(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "hourtypes":
								messageArray = twinfield.getHourTypes(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							
							}
						}
					}
					// Export section
					String[] exportMessageArray = twinfield.setWorkOrders(set.getExportOffice(), session, cluster,
							t.getSoftwareToken(), set.getFactuurType(), t.getSoftwareName(), set.getUser());
					errorMessage += exportMessageArray[0];
					if (exportMessageArray[1] != null) {
						errorDetails = exportMessageArray[1];
					}
					if (checkUpdate.equals("true")) {
						TokenDAO.saveModifiedDate(getDate(null), t.getSoftwareToken());
					}
					if (!errorMessage.equals("")) {
						ObjectDAO.saveLog(errorMessage, errorDetails, t.getSoftwareToken());
					} else {
						ObjectDAO.saveLog("Niks te importeren", errorDetails, t.getSoftwareToken());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Twinfield Thread finished");
		}
	}
	
	public class WeFactThread extends Thread {
		String token, clientToken, date;
		String errorMessage = "", errorDetails = "";
		String checkUpdate = "false";
		
		WeFactThread(String token, String clientToken, String date) {
			this.token = token;
			this.clientToken = clientToken;
			this.date = date;
		}
		
		public void run() {
			try {
				System.out.println("WeFact Thread Running");
				
				WeFactHandler wefact = new WeFactHandler();
				Settings set = ObjectDAO.getSettings(token);
				if (set != null) {
					if (date == null) {
						date = set.getSyncDate();
						DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						Date newDate = null;
						try {
							// String to date
							newDate = format.parse(date);
							Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							date = formatter.format(newDate);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					ArrayList<String> importTypes = set.getImportObjects();
					// Import section
					for (String type : importTypes) {
						switch (type) {
						case "materials":
							messageArray = wefact.getMaterials(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							messageArray = wefact.getRelations(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "hourtypes":
							messageArray = wefact.getHourTypes(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "offertes":
							messageArray = wefact.getOffertes(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						}
					}
					// Export section
					String[] exportMessageArray = null;
					// Type is factuur
					if (set.getExportWerkbontype().equals("factuur")) {
						exportMessageArray = wefact.setFactuur(clientToken, token, set.getFactuurType(),
								set.getRoundedHours());
						// Type is offerte
					} else {
						exportMessageArray = wefact.setOfferte(clientToken, token, set.getFactuurType(),
								set.getRoundedHours());
					}
					errorMessage += exportMessageArray[0];
					if (exportMessageArray[1] != null) {
						errorDetails = exportMessageArray[1];
					}
					if (checkUpdate.equals("true")) {
						TokenDAO.saveModifiedDate(getDate(null), token);
					}
					if (!errorMessage.equals("")) {
						ObjectDAO.saveLog(errorMessage, errorDetails, token);
					} else {
						ObjectDAO.saveLog("Niks te importeren", errorDetails, token);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("WeFact Thread finished");
		}
	}
	
	public class eAccountingThread extends Thread {
		Token t;
		String date;
		String errorMessage = "", errorDetails = "";
		String checkUpdate = "false";
		
		eAccountingThread(Token t, String date) {
			this.t = t;
			this.date = date;
		}
		
		public void run() {
			System.out.println("eAccounting Thread Running");
			EAccountingHandler eaccounting = new EAccountingHandler();
			// check if accessToken is still valid
			try {
				if (t.getAccessSecret() != null && !eaccounting.checkAccessToken(t.getAccessToken())) {
					// Get accessToken with refreshToken
					t = OAuthEAccounting.getAccessToken(null, t.getAccessSecret(), t.getSoftwareName(),
							t.getSoftwareToken());
				}
				
				Settings set = ObjectDAO.getSettings(t.getSoftwareToken());
				if (set != null) {
					if (date == null) {
						date = set.getSyncDate();
						if (!date.equals("")) {
							DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							Date newDate = null;
							try {
								// String to date
								newDate = format.parse(date);
								Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								date = formatter.format(newDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}
						} else {
							date = null;
						}
					}
					ArrayList<String> importTypes = set.getImportObjects();
					// Import section
					for (String type : importTypes) {
						switch (type) {
						case "materials":
							messageArray = eaccounting.getMaterials(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							messageArray = eaccounting.getRelations(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "projects":
							messageArray = eaccounting.getProjects(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "verkooporders":
							messageArray = eaccounting.getOrders(t, date, set);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						}
					}
					// Export section
					String[] exportMessageArray = null;
					// Type is factuur
					if (set.getExportWerkbontype().equals("factuur")) {
						exportMessageArray = eaccounting.setFactuur(t, set, date);
						errorMessage += exportMessageArray[0];
						if (exportMessageArray[1] != null) {
							errorDetails = exportMessageArray[1];
						}
					}
					// setErrorMessageDetailsWeFact(exportMessageArray);
					if (checkUpdate.equals("true")) {
						TokenDAO.saveModifiedDate(getDate(null), t.getSoftwareToken());
					}
					if (!errorMessage.equals("")) {
						ObjectDAO.saveLog(errorMessage, errorDetails, t.getSoftwareToken());
					} else {
						ObjectDAO.saveLog("Niks te importeren", errorDetails, t.getSoftwareToken());
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("eAccounting Thread Finished");
		}
	};
	
	public class MoloniThread extends Thread {
		Token t;
		String date;
		String errorMessage = "", errorDetails = "";
		String checkUpdate = "false";
		
		MoloniThread(Token t, String date) {
			this.t = t;
			this.date = date;
		}
		
		public void run() {
			System.out.println("Moloni Thread Running");
			MoloniHandler moloni = new MoloniHandler();
			// check if accessToken is still valid
			try {
				if (t.getAccessSecret() != null && !moloni.checkAccessToken(t.getAccessToken())) {
					// Get accessToken with refreshToken
					t = OAuthMoloni.getAccessToken(null, t.getAccessSecret(), t.getSoftwareName(),
							t.getSoftwareToken());
				}
				
				Settings set = ObjectDAO.getSettings(t.getSoftwareToken());
				if (set != null) {
					if (date == null) {
						date = set.getSyncDate();
						if (!date.equals("")) {
							DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							Date newDate = null;
							try {
								// String to date
								newDate = format.parse(date);
								Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								date = formatter.format(newDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}
						} else {
							date = null;
						}
					}
					ArrayList<String> importTypes = set.getImportObjects();
					// Import section
					for (String type : importTypes) {
						switch (type) {
						case "materials":
							messageArray = moloni.getMaterials(t, date, set.getImportOffice());
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							messageArray = moloni.getRelations(t, date, set.getImportOffice());
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "employees":
							messageArray = moloni.getEmployees(t, date, set.getImportOffice());
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						// case "projects":
						// messageArray = eaccounting.getProjects(t, date);
						// errorMessage += messageArray[0];
						// if (messageArray[1].equals("true")) {
						// checkUpdate = "true";
						// }
						// break;
						// case "verkooporders":
						// messageArray = eaccounting.getOrders(t, date, set);
						// errorMessage += messageArray[0];
						// if (messageArray[1].equals("true")) {
						// checkUpdate = "true";
						// }
						// break;
						}
					}
					// Export section
					String[] exportMessageArray = null;
					// Type is factuur
					if (set.getExportWerkbontype().equals("factuur")) {
						exportMessageArray = moloni.setFactuur(t, set, date);
						errorMessage += exportMessageArray[0];
						if (exportMessageArray[1] != null) {
							errorDetails = exportMessageArray[1];
						}
					}
					if (checkUpdate.equals("true")) {
						TokenDAO.saveModifiedDate(getDate(null), t.getSoftwareToken());
					}
					if (!errorMessage.equals("")) {
						ObjectDAO.saveLog(errorMessage, errorDetails, t.getSoftwareToken());
					} else {
						ObjectDAO.saveLog("Nothing to import", errorDetails, t.getSoftwareToken());
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Moloni Thread Finished");
		}
	};
}