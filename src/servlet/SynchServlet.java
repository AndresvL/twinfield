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
import controller.drivefx.DriveFxHandler;
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
	
	/**
	 * This method is called after the user or the system triggers the sync
	 * method, if softwareToken parameter is null the credentials from the
	 * database will be used for sychronisation
	 * 
	 * @param req
	 *            HttpServletRequest with request properties
	 * @param resp
	 *            HttpServletResponse with response properties
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		ArrayList<Token> allTokens = null;
		// Check if user is logged in
		if (softwareToken != null) {
			try {
				Token t = TokenDAO.getToken(softwareToken, softwareName);
				if (t != null && !t.getAccessSecret().equals("invalid")) {
					softwareName = t.getSoftwareName();
					// setSyncMethods(true) because user is online
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
			// Print total Users fetched from database
			System.out.println("A total of " + allTokens.size() + " users are found in database");
			int twinfieldCount = 0, wefactCount = 0, eaccountingCount = 0, moloniCount = 0, drivefxCount = 0;
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
				case "DriveFx":
					drivefxCount++;
					break;
				}
			}
			System.out.println(twinfieldCount + " Twinfield users");
			System.out.println(wefactCount + " WeFact users");
			System.out.println(eaccountingCount + " eAccounting users");
			System.out.println(moloniCount + " Moloni users");
			System.out.println(drivefxCount + " DriveFx users");
		}
	}
	
	/**
	 * This method is used to get the current Date
	 * 
	 * @param date
	 *            a Date(yyyy-MM-dd HH:mm:ss) String
	 * @return date with format yyyy-MM-dd HH:mm:ss
	 */
	public String getDate(String date) {
		String timestamp = null;
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (date != null) {
			timestamp = date;
		} else {
			timestamp = za.format(formatter);
		}
		return timestamp;
	}
	
	/**
	 * This method calls different threads to start the synchronisation process.
	 * For every softwareName a different thread will be started
	 * 
	 * @param t
	 *            Token object of the current user
	 * @param req
	 *            HttpServletRequest
	 * @param loggedIn
	 *            true if user is logged in
	 * @throws Exception
	 *             createDatabaseConnection exception
	 */
	public void setSyncMethods(Token t, HttpServletRequest req, boolean loggedIn) throws Exception {
		String date = null;
		if (!loggedIn) {
			date = TokenDAO.getModifiedDate(t.getSoftwareToken());
		}
		switch (t.getSoftwareName()) {
		case "Twinfield":
			String session = null;
			String cluster = null;
			// If req is null the system tries to synchronise and the
			// session/cluster
			// will be set with SoapHandler.getSession(t);
			if (req != null) {
				session = (String) req.getSession().getAttribute("session");
				cluster = (String) req.getSession().getAttribute("cluster");
			} else {
				String[] array = SoapHandler.getSession(t);
				session = array[0];
				cluster = array[1];
			}
			new TwinfieldThread(t, date, session, cluster).start();
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
		case "DriveFx":
			new DriveFxThread(t, date).start();
			System.out.println("DATE " + date);
			DBConnection.createDatabaseConnection(false);
			break;
		default:
			break;
		}
		
	}
	
	public class TwinfieldThread extends Thread {
		private Token t;
		private String date;
		private String session = null, cluster = null;
		private String errorMessage = "", errorDetails = "";
		private String checkUpdate = "false";
		
		TwinfieldThread(Token t, String date, String session, String cluster) {
			this.t = t;
			this.date = date;
			this.session = session;
			this.cluster = cluster;
		}
		
		public void run() {
			try {
				System.out.println("Twinfield Thread Running");
				TwinfieldHandler twinfield = new TwinfieldHandler();
				Settings set = ObjectDAO.getSettings(t.getSoftwareToken());
				if (set != null) {
					ArrayList<String> importTypes = set.getImportObjects();
					// Import section
					for (String type : importTypes) {
						String[] array = null;
						if (session == null) {
							// Get a new sessionObject if session is null
							array = SoapHandler.getSession(t);
						} else {
							// Use existing session
							array = new String[] { session, cluster };
						}
						
						if (array != null) {
							session = array[0];
							cluster = array[1];
							// Switch for different import objects
							switch (type) {
							case "employees":
								// Get all Employees from Twinfield and add them
								// to WOA
								// Returns an array with response message for
								// log
								messageArray = twinfield.getEmployees(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "projects":
								// Get all Projects from Twinfield and add them
								// to WOA
								// Return an array with response message for log
								messageArray = twinfield.getProjects(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "materials":
								// Get all Materials from Twinfield and add them
								// to WOA
								// Return an array with response message for log
								messageArray = twinfield.getMaterials(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "relations":
								// Get all Relations from Twinfield and add them
								// to WOA
								// Return an array with response message for log
								messageArray = twinfield.getRelations(set.getImportOffice(), session, cluster,
										t.getSoftwareToken(), t.getSoftwareName(), date);
								errorMessage += messageArray[0];
								if (messageArray[1].equals("true")) {
									checkUpdate = "true";
								}
								break;
							case "hourtypes":
								// Get all Projects from Twinfield and add them
								// to WOA
								// Return an array with response message for log
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
							// Get all Materials from WeFact and add them to WOA
							// Return an array with response message for log
							messageArray = wefact.getMaterials(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							// Get all Relations from WeFact and add them to WOA
							// Return an array with response message for log
							messageArray = wefact.getRelations(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "hourtypes":
							// Get all Hourtypes from WeFact and add them to WOA
							// Return an array with response message for log
							messageArray = wefact.getHourTypes(clientToken, token, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "offertes":
							// Get all Offertes from WeFact and add them as
							// WorkOrder to WOA
							// Return an array with response message for log
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
			// Check if accessToken is still valid
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
							// Get all Materials from eAccounting and add them
							// to WOA
							// Return an array with response message for log
							messageArray = eaccounting.getMaterials(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							// Get all Relations from eAccounting and add them
							// to WOA
							// Return an array with response message for log
							messageArray = eaccounting.getRelations(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "projects":
							// Get all Projects from eAccounting and add them to
							// WOA
							// Return an array with response message for log
							messageArray = eaccounting.getProjects(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "verkooporders":
							// Get all Verkooporders from eAccounting and add
							// them as WorkOrder to WOA
							// Return an array with response message for log
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
					// Type is always factuur
					if (set.getExportWerkbontype().equals("factuur")) {
						exportMessageArray = eaccounting.setFactuur(t, set, date);
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
						ObjectDAO.saveLog("Niks te importeren", errorDetails, t.getSoftwareToken());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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
			// Check if accessToken is still valid
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Moloni Thread Finished");
		}
	};
	
	public class DriveFxThread extends Thread {
		Token t;
		String date;
		String errorMessage = "", errorDetails = "";
		String checkUpdate = "false";
		
		DriveFxThread(Token t, String date) {
			this.t = t;
			this.date = date;
		}
		
		public void run() {
			System.out.println("DriveFx Thread Running");
			DriveFxHandler driveFx = new DriveFxHandler();
			// check if accessToken is still valid
			try {
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
							messageArray = driveFx.getMaterials(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "relations":
							messageArray = driveFx.getRelations(t, date);
							errorMessage += messageArray[0];
							if (messageArray[1].equals("true")) {
								checkUpdate = "true";
							}
							break;
						case "employees":
							messageArray = driveFx.getEmployees(t, date);
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
						exportMessageArray = driveFx.setFactuur(t, set, date);
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
						ObjectDAO.saveLog("Niks te importeren", errorDetails, t.getSoftwareToken());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("DriveFx Thread Finished");
		}
	};
}