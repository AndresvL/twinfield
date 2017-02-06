package object.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WorkOrder {
	// FACTUUR
	// Header
	// ProjectNr, performancedate, invoiceaddressnumber,
	// deliveraddressnumber, customercode, status, paymentmethod(cash, bank,
	// cheque, cashondelivery, da)
	private String id, workorderNr, projectNr, workDate, customerEmailInvoice, customerEmail, customerDebtorNr, status, paymentMethod,
			creationDate;
	// UREN
	private String employeeNr, hourType, description, ratecode;
	private int duration;
	// line
	private ArrayList<Material> materials;

	// FACTUUR constructor
	public WorkOrder(String projectNr, String workDate, String customerEmailInvoice, String customerEmail,
			String customerDebtorNr, String status, String paymentMethod, ArrayList<Material> m, String creationDate, String id, String workorderNr) {
		this.projectNr = projectNr;
		setWorkDate(workDate);
		this.customerEmailInvoice = customerEmailInvoice;
		this.customerEmail = customerEmail;
		this.customerDebtorNr = customerDebtorNr;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.materials = m;
		setCreationDate(creationDate);
		this.id = id;
		this.workorderNr = workorderNr;
	}

	// UREN constructor
	public WorkOrder(String employeeNr, String hourType, String periodWorkDate, String projectNr, String description,
			String duration, String id) {
		this.employeeNr = employeeNr;
		this.hourType = hourType;
		this.setWorkDate(periodWorkDate);
		this.projectNr = projectNr;
		this.description = description;
		this.setDuration(duration);
		this.id = id;
	}

	public String getProjectNr() {
		return projectNr;
	}

	public void setProjectNr(String projectNr) {
		this.projectNr = projectNr;
	}

	public String getWorkDate() {
		if (workDate == null) {
			return "leeg";
		} else {
			return workDate;
		}
	}

	public void setWorkDate(String workDate) {
		if (workDate != null) {
			try {
				SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
				Date date = dt.parse(workDate);
				SimpleDateFormat dt1 = new SimpleDateFormat("yyyyMMdd");
				this.workDate = dt1.format(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public String getCustomerEmailInvoice() {
		return customerEmailInvoice;
	}

	public void setCustomerEmailInvoice(String customerEmailInvoice) {
		this.customerEmailInvoice = customerEmailInvoice;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getCustomerDebtorNr() {
		return customerDebtorNr;
	}

	public void setCustomerDebtorNr(String customerDebtorNr) {
		this.customerDebtorNr = customerDebtorNr;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public ArrayList<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(ArrayList<Material> materials) {
		this.materials = materials;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		try {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			Date date = dt.parse(creationDate);
			SimpleDateFormat dt1 = new SimpleDateFormat("yyyyMMdd");
			this.creationDate = dt1.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getEmployeeNr() {
		if (employeeNr == null) {
			return "leeg";
		} else {
			return employeeNr;
		}
	}

	public void setEmployeeNr(String employeeNr) {
		this.employeeNr = employeeNr;
	}

	public String getHourType() {
		if (hourType == null) {
			return "leeg";
		} else {
			return hourType;
		}
	}

	public void setHourType(String hourType) {
		this.hourType = hourType;
	}

	public String getDescription() {
		if (description == null) {
			return "leeg";
		} else {
			return description;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRatecode() {
		if (ratecode == null) {
			return "leeg";
		} else {
			return ratecode;
		}
	}

	public void setRatecode(String ratecode) {
		this.ratecode = ratecode;
	}

	public int getDuration() {
		if (duration == 0) {
			return 0;
		} else {
			return duration;
		}
	}

	public void setDuration(String duration) {
		String[] time = duration.split(":");
		int hours = Integer.parseInt(time[0]);
		hours = hours * 60;
		int mins = Integer.parseInt(time[1]);
		int total = mins + hours;
		this.duration = total;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWorkorderNr() {
		return workorderNr;
	}

	public void setWorkorderNr(String workorderNr) {
		this.workorderNr = workorderNr;
	}

}
