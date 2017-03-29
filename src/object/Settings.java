package object;

import java.io.Serializable;
import java.util.ArrayList;

public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;
	private String importOffice, exportOffice, factuurType, user, exportWerkbontype;
	private ArrayList<String> importObjects;

	public Settings(String iO, String eO, String fT, ArrayList<String> iObj, String sU, String eWT) {
		setImportOffice(iO);
		setExportOffice(eO);
		setFactuurType(fT);
		setImportObjects(iObj);
		setUser(sU);
		setExportWerkbontype(eWT);
	}

	public void setUser(String sU) {
		user = sU;
	}

	public String getUser() {
		return user;
	}

	public String getImportOffice() {
		return importOffice;
	}

	public void setImportOffice(String importOffice) {
		this.importOffice = importOffice;
	}

	public String getExportOffice() {
		return exportOffice;
	}

	public void setExportOffice(String exportOffice) {
		this.exportOffice = exportOffice;
	}

	public String getFactuurType() {
		return factuurType;
	}

	public void setFactuurType(String factuurType) {
		this.factuurType = factuurType;
	}

	public ArrayList<String> getImportObjects() {
		return importObjects;
	}

	public void setImportObjects(ArrayList<String> importObjects) {
		this.importObjects = importObjects;
	}

	public String getExportWerkbontype() {
		return exportWerkbontype;
	}

	public void setExportWerkbontype(String exportWerkbontype) {
		this.exportWerkbontype = exportWerkbontype;
	}
}
