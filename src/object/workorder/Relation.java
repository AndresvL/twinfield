package object.workorder;

import java.util.ArrayList;

public class Relation {
	private String companyName, debtorNumber, contact, emailWorkorder, modified;
	private ArrayList<Address> addresses;

	public Relation(String companyName, String debtorNumber, String contact, String emailWorkorder,
			ArrayList<Address> addresses, String modified) {
		this.companyName = companyName;
		this.debtorNumber = debtorNumber;
		this.contact = contact;
		this.emailWorkorder = emailWorkorder;
		this.setAddresses(addresses);
		this.setModified(modified);
	}
	//CompanyName
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String name) {
		this.companyName = name;
	}

	public String getDebtorNumber() {
		return debtorNumber;
	}

	public void setDebtorNumber(String debtorNumber) {
		this.debtorNumber = debtorNumber;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getEmailWorkorder() {
		return emailWorkorder;
	}

	public void setEmailWorkorder(String emailWorkorder) {
		this.emailWorkorder = emailWorkorder;
	}

	public ArrayList<Address> getAddressess() {
		return addresses;
	}

	public void setAddresses(ArrayList<Address> addresses) {
		this.addresses = addresses;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}
}
