package controller.twinfield;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import object.Token;
import object.twinfield.Search;
import object.workorder.Address;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Project;
import object.workorder.Relation;

public class SoapHandler {
	private final static Logger logger = Logger.getLogger(SoapHandler.class.getName());

	public static String[] getSession(Token token) {
		String sessionID = null;
		String cluster = null;
		try {
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			// Send SOAP Message to SOAP Server
			String url = "https://login.twinfield.com/webservices/session.asmx?/";
			SOAPMessage soapResponse = soapConnection.call(createSOAPSession(token), url);
			SOAPEnvelope soapPart = soapResponse.getSOAPPart().getEnvelope();
			if (soapPart != null) {
				sessionID = soapPart.getHeader().getFirstChild().getFirstChild().getTextContent();
			}
			cluster = soapPart.getBody().getFirstChild().getLastChild().getTextContent();
			soapConnection.close();
		} catch (Exception e) {
			System.err.println("Error occurred while sending SOAP Request to Server");

			e.printStackTrace();
		}
		String[] sessionArray = new String[] { sessionID, cluster};
		return sessionArray;
	}

	private static SOAPMessage createSOAPSession(Token token) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage soapMessage = messageFactory.createMessage();

		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();

		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("OAuthLogon", "", "http://www.twinfield.com/");
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("clientToken");
		soapBodyElem1.addTextNode(token.getConsumerToken());
		SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("clientSecret");
		soapBodyElem2.addTextNode(token.getConsumerSecret());
		SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("accessToken");
		soapBodyElem3.addTextNode(token.getAccessToken());
		SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("accessSecret");
		soapBodyElem4.addTextNode(token.getAccessSecret());
		soapMessage.saveChanges();

		return soapMessage;
	}

	public static Object createSOAPXML(String session, String cluster, String data, String type) {
		// Create SOAP Connection
		SOAPMessage soapResponse = null;
		SOAPConnection soapConnection = null;
		String xmlString = null;
		Object obj = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			// Send SOAP Message to SOAP Server
			String url = cluster + "/webservices/processxml.asmx?wsdl";
			System.out.println("CLUSTER " + url);
			MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();

			// SOAP Envelope
			SOAPEnvelope envelope = soapPart.getEnvelope();

			// SOAP Header
			setHeader(envelope, session);

			// SOAP Body
			setXMLBody(envelope, data);
			soapMessage.saveChanges();
			soapResponse = soapConnection.call(soapMessage, url);
			xmlString = soapResponse.getSOAPPart().getEnvelope().getBody().getFirstChild().getFirstChild()
					.getTextContent();

			soapConnection.close();
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(xmlString)));

		} catch (Exception e) {
			e.printStackTrace();
		}
		int result = Integer
				.parseInt(doc.getChildNodes().item(0).getAttributes().getNamedItem("result").getNodeValue());

		if (type.equals("workorder")) {
			logger.info("UrenboekingRequest " + data);
			logger.info("UrenboekingResponse " + xmlString);
			ArrayList<String> results = new ArrayList<String>();
			NodeList workorder = doc.getChildNodes().item(0).getChildNodes();
			int workorderResult = 0;
			for (int i = 0; i < workorder.getLength(); i++) {
				workorderResult = Integer
						.parseInt(workorder.item(i).getAttributes().getNamedItem("result").getNodeValue());
				if (workorderResult == 1) {
					results.add("true");
				} else {
					results.add(xmlString);
				}
			}
			return results;
		}
		if (type.equals("workorderFactuur")) {
			logger.info("FactuurRequest " + data);
			logger.info("FactuurResponse " + xmlString);
			if (result > 0) {
				return "true";
			} else {
				return xmlString;
			}
		}
		// Check if SOAP result is 0 or 1
		if (result != 0) {
			switch (type) {
			case "project":
				logger.info("ProjectRequest " + data);
				logger.info("ProjectResponse " + xmlString);
				obj = getProjectXML(doc);
				break;
			case "material":
				logger.info("MaterialRequest " + data);
				logger.info("MaterialResponse " + xmlString);
				obj = getMaterialXML(doc);
				break;
			case "relation":
				logger.info("RelationRequest " + data);
				logger.info("RelationResponse " + xmlString);
				obj = getRelationXML(doc);
				break;
			case "hourtype":
				logger.info("HourtypeRequest " + data);
				logger.info("HourtypeResponse " + xmlString);
				obj = getHourTypeXML(doc);
				break;
			case "office":
				obj = getOffices(doc);
				break;
			}
		}
		return obj;
	}

	// See Finder methode from Twinfield
	public static ArrayList<String> createSOAPFinder(String session, String cluster, Search object) {
		// Create SOAP Connection
		SOAPMessage soapResponse = null;
		SOAPConnection soapConnection = null;
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			// Send SOAP Message to SOAP Server
			String url = cluster + "/webservices/finder.asmx?wsdl";
			MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();

			// SOAP Envelope
			SOAPEnvelope envelope = soapPart.getEnvelope();

			// SOAP Header
			setHeader(envelope, session);

			// SOAP Body
			setFinderBody(envelope, object);

			soapMessage.saveChanges();

			soapResponse = soapConnection.call(soapMessage, url);
			soapConnection.close();

//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			soapResponse.writeTo(baos);
//			String results = baos.toString();
//			System.out.println("SESSIONCALL " + results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return setArrayList(soapResponse);
	}

	private static void setFinderBody(SOAPEnvelope envelope, Search object) throws SOAPException {
		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("Search", "", "http://www.twinfield.com/");
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("type");
		soapBodyElem1.addTextNode(object.getType());
		SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("pattern");
		soapBodyElem2.addTextNode(object.getPattern());
		SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("field");
		soapBodyElem3.addTextNode("" + object.getField());
		SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("firstRow");
		soapBodyElem4.addTextNode("" + object.getFirstRow());
		SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("maxRows");
		soapBodyElem5.addTextNode("" + object.getMaxRows());
		SOAPElement soapBodyElem6 = soapBodyElem.addChildElement("options");
		String[][] options = object.getOptions();
		if (object.getOptions() != null) {
			for (int i = 0; i < options.length; i++) {
				SOAPElement soapBodyElem7 = soapBodyElem6.addChildElement(options[i][0]);
				for (int j = 2; j < options[i].length; j++) {
					SOAPElement soapBodyElem8 = soapBodyElem7.addChildElement(options[i][1]);
					soapBodyElem8.addTextNode(options[i][j]);
				}
			}
		}
	}

	// Set a body with parameter list
	private static void setXMLBody(SOAPEnvelope envelope, String data) throws SOAPException {
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("ProcessXmlString", "", "http://www.twinfield.com/");
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("xmlRequest");
		soapBodyElem1.addTextNode("<![CDATA[" + data + "]]>");
	}

	// Global header
	private static void setHeader(SOAPEnvelope envelope, String session) throws SOAPException {
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");

		// SOAP head
		SOAPHeader soapHead = envelope.getHeader();
		SOAPElement soapHeadElem = soapHead.addChildElement("Header", "", "http://www.twinfield.com/");
		SOAPElement soapHeadElem1 = soapHeadElem.addChildElement("SessionID");
		soapHeadElem1.addTextNode(session);
	}

	// Converts String to Project Object
	private static Object getProjectXML(Document doc) {
		Project p = null;
		// <dimension>
		NodeList allData = doc.getChildNodes().item(0).getChildNodes();
		// <code>
		String code = allData.item(2).getTextContent();
		// <uid>
		String code_ext = "Onbekend";
		// <name>
		String name = allData.item(4).getTextContent();
		// <dimension status>
		String status = doc.getChildNodes().item(0).getAttributes().getNamedItem("status").getNodeValue();
		// <projects>
		NodeList projectsNode = doc.getElementsByTagName("projects");
		NodeList projects = null;
		String description = null;
		String authoriser = null;
		String dateStart = null;
		String dateEnd = null;
		String debtorNumber = null;
		int active = 0;
		if (projectsNode.getLength() > 0) {
			projects = projectsNode.item(0).getChildNodes();

			// <invoicedescription>
			description = projects.item(0).getTextContent();
			authoriser = projects.item(1).getTextContent();
			// <validfrom>
			dateStart = projects.item(1).getTextContent();
			if (dateStart.equals("")) {
				dateStart = null;
			}
			// <validfrom>
			dateEnd = projects.item(2).getTextContent();
			if (dateEnd.equals("")) {
				dateEnd = null;
			}
			// <customer>
			debtorNumber = projects.item(4).getTextContent();
			// active
			
			if (status.equals("active")) {
				active = 1;
			}
		}
		p = new Project(code, code_ext, debtorNumber, status, name, dateStart, dateEnd, description, 0, active,
				authoriser);
		return p;
	}

	// Converts String to Material Object
	private static Object getMaterialXML(Document doc) {
		ArrayList<Material> materials = new ArrayList<Material>();
		Material m = null;
		// <article>
		NodeList allData = doc.getChildNodes().item(0).getChildNodes();
		// <header>
		NodeList headerData = allData.item(0).getChildNodes();
		// <code>
		String code = headerData.item(1).getTextContent();
		// <lines>
		NodeList lines = allData.item(1).getChildNodes();
		String subcode = null, unit = null, description = null;
		double price = 0d;
		for (int i = 0; i < lines.getLength(); i++) {
			// <line>
			NodeList line = lines.item(i).getChildNodes();
			price = Double.parseDouble(line.item(0).getTextContent());
			unit = line.item(2).getTextContent();
			description = line.item(3).getTextContent();
			subcode = line.item(5).getTextContent();
			// do something with the subMaterials
			m = new Material(code, subcode, unit, description, price, null, null);
			materials.add(m);
		}
		return materials;
	}

	// Converts String to Relation Object
	private static Object getRelationXML(Document doc) {
		Relation r = null;
		// <dimension>
		NodeList allData = doc.getChildNodes().item(0).getChildNodes();
		// <code>
		String debtorNumber = allData.item(2).getTextContent();
		// <uid>
		// String uid = allData.item(3).getTextContent();
		// <name>
		String name = allData.item(4).getTextContent();
		// <financials>
		NodeList financials = doc.getElementsByTagName("financials").item(0).getChildNodes();
		// <ebillmail>
		String emailWorkorder = financials.item(9).getTextContent();
		// <addresses>
		NodeList addresses = doc.getElementsByTagName("addresses").item(0).getChildNodes();
		String phoneNumber = null, email = null, street = null, houseNumber = null, postalCode = null, city = null,
				remark = null;
		ArrayList<Address> allAddresses = new ArrayList<Address>();
		for (int i = 0; i < addresses.getLength(); i++) {
			street = "";
			NodeList address = addresses.item(i).getChildNodes();
			int addressId = Integer.parseInt(addresses.item(i).getAttributes().getNamedItem("id").getNodeValue());
			String type = addresses.item(i).getAttributes().getNamedItem("type").getNodeValue();
			phoneNumber = address.item(4).getTextContent();
			email = address.item(6).getTextContent();
			if (email.equals("")) {
				email = "leeg";
			}
			String streetArray = address.item(9).getTextContent();
			String[] streetNumber = null;
			if (streetArray != null) {
				streetNumber = streetArray.split("\\s+");

				for (int j = 0; j < streetNumber.length; j++) {
					if (j == streetNumber.length - 1) {
						houseNumber = streetNumber[j];
					} else if (j == streetNumber.length - 2) {
						street += streetNumber[j];
					} else {
						street += streetNumber[j] + " ";
					}
				}
				if (street.equals("")) {
					street = "leeg";
				}
			}
			houseNumber = streetNumber[streetNumber.length - 1];
			postalCode = address.item(3).getTextContent();
			if (postalCode.equals("")) {
				postalCode = "leeg";
			}
			city = address.item(2).getTextContent();
			if (city.equals("")) {
				city = "leeg";
			}
			remark = address.item(8).getTextContent();
			if (remark.equals("")) {
				remark = "leeg";
			}
			Address a = new Address(name, phoneNumber, email, street, houseNumber, postalCode, city, remark, type,
					addressId);
			allAddresses.add(a);
		}
		r = new Relation(name, debtorNumber, name, emailWorkorder, allAddresses, null);
		return r;
	}

	// Converts String to Hourtype Object
	private static Object getHourTypeXML(Document doc) {
		HourType h = null;
		// <dimension>
		NodeList allData = doc.getChildNodes().item(0).getChildNodes();
		// <code>
		String code = allData.item(2).getTextContent();
		// <uid>
		// String uid = allData.item(3).getTextContent();
		// <name>
		String name = allData.item(4).getTextContent();
		// h = new HourType(code, name, costBooking, saleBooking, costPrice,
		// salePrice, active);
		h = new HourType(code, name, 0, 0, 0.0, 0.0, 1);
		return h;
	}

	public static ArrayList<String> setArrayList(SOAPMessage response) {
		ArrayList<String> allItems = new ArrayList<String>();
		try {
			// Get data from SOAP message
			// <Body><SearchResponse><data>
			Node data = response.getSOAPPart().getEnvelope().getBody().getFirstChild().getLastChild();
			Element element = null;
			if (data.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) data;
			}

			NodeList allData = element.getChildNodes();
			// <TotalRows>
			int totalRows = Integer.parseInt(allData.item(0).getFirstChild().getTextContent());
			if (totalRows > 0) {
				// <Columns>
				NodeList columns = allData.item(1).getChildNodes();
				// <Items>
				NodeList items = allData.item(2).getChildNodes();
				for (int i = 0; i < items.getLength(); i++) {
					String temp = null;
					// <ArrayOfString>
					NodeList content = items.item(i).getChildNodes();
					for (int j = 0; j < columns.getLength(); j++) {
						// <string>
						if (temp == null) {
							temp = content.item(j).getTextContent() + ",";
						} else if ((j + 1) != columns.getLength()) {
							temp += content.item(j).getTextContent() + ",";
						} else {
							temp += content.item(j).getTextContent();
						}
					}
					allItems.add(temp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allItems;

	}

	public static ArrayList<Map<String, String>> getOffices(Document doc) {
		ArrayList<Map<String, String>> offices = new ArrayList<Map<String, String>>();
		// <offices>
		NodeList allData = doc.getChildNodes().item(0).getChildNodes();
		for (int i = 0; i < allData.getLength(); i++) {
			String officeCode = allData.item(i).getTextContent();
			String officeName = allData.item(i).getAttributes().getNamedItem("name").getNodeValue();
			Map<String, String> office = new HashMap<String, String>();
			office.put("code", officeCode);
			office.put("name", officeName);
			offices.add(office);
		}

		return offices;
	}
}
