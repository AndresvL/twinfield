package object.rest;


public class Project {
	private String code, code_ext, debtorNumber, status, name, dateStart, dateEnd, description, authoriser;
	private int progress, active;
	
	public Project(String code, String name){
		this.code = code;
		this.name = name;
	}
	
	public Project(String code, String code_ext, String debtor_number, String status, String name, String date_start, String date_end, String description, int progress, int active, String authoriser){
		this.code = code;
		this.code_ext = code_ext;
		this.debtorNumber = debtor_number;
		this.status = status;
		this.name = name;
		this.dateStart = date_start;
		this.dateEnd = date_end;
		this.description = description;
		this.progress = progress;
		this.active = active;
		this.setAuthoriser(authoriser);
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCode_ext() {
		return code_ext;
	}
	public void setCode_ext(String code_ext) {
		this.code_ext = code_ext;
	}
	public String getDebtor_number() {
		if(this.debtorNumber.equals("")){
			debtorNumber = "leeg";
		}
		return debtorNumber;
	}
	public void setDebtor_number(String debtor_number) {
		this.debtorNumber = debtor_number;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate_start() {
		return dateStart;
	}
	public void setDate_start(String date_start) {
		this.dateStart = date_start;
	}
	public String getDate_end() {
		return dateEnd;
	}
	public void setDate_end(String date_end) {
		this.dateEnd = date_end;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}

	public String getAuthoriser() {
		return authoriser;
	}

	public void setAuthoriser(String authoriser) {
		this.authoriser = authoriser;
	}
	
}
