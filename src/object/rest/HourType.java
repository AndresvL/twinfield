package object.rest;

public class HourType {
	private String code, name;
	private int costBooking, saleBooking;
	private double costPrice, salePrice;
	private int active;
	
	public HourType(String c, String n, int cB, int sB, double cP, double sP, int a){
		this.code = c;
		this.name = n;
		this.costBooking = cB;
		this.saleBooking = sB;
		this.salePrice = sP;
		this.costPrice = cP;
		this.active = a;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCostBooking() {
		return costBooking;
	}
	public void setCostBooking(int costBooking) {
		this.costBooking = costBooking;
	}
	public int getSaleBooking() {
		return saleBooking;
	}
	public void setSaleBooking(int saleBooking) {
		this.saleBooking = saleBooking;
	}
	public double getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
	}
	public double getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
}
