package object.rest;

import java.util.ArrayList;

public class Relation {
	private String name, debtorNumber, contact, emailWorkorder;
	private ArrayList<Address> addresses;

	public Relation(String name, String debtorNumber, String contact, String emailWorkorder,
			ArrayList<Address> addresses) {
		this.name = name;
		this.debtorNumber = debtorNumber;
		this.contact = contact;
		this.emailWorkorder = emailWorkorder;
		this.setAddresses(addresses);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
