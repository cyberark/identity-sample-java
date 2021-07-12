package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

	@JsonProperty("ID")
    private String uuid;

	@JsonProperty("DisplayName")
	private String displayName;

	@JsonProperty("Mail")
	private String mail;

	@JsonProperty("Name")
	private String name;

	@JsonProperty("Password")
	private String password;

	@JsonProperty("HomeNumber")
	private String homeNumber;

	@JsonProperty("OfficeNumber")
	private String officeNumber;

	@JsonProperty("ForcePasswordChangeNext")
	private boolean forcePasswordChangeNext;

	@JsonProperty("MobileNumber")
	private String mobileNumber;

	@JsonProperty("Description")
	private String description;

	@JsonProperty("street_line_1")
	private String streetLine1;

	@JsonProperty("street_line_2")
	private String streetLine2;

	@JsonProperty("address_city")
	private String city;

	@JsonProperty("postal_code")
	private String postalCode;

	@JsonProperty("state_code")
	private String state;

	@JsonProperty("country_code")
	private String country;

	public User(String displayName, String email, String name, String password) {
		this.displayName = displayName;
		this.mail = email;
		this.name = name;
		this.password = password;
	}

	public User() {

	}
	public String getUuid() { return uuid; }

	public void setUuid(String uuid) { this.uuid = uuid; }

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHomeNumber() {
		return homeNumber;
	}

	public void setHomeNumber(String homeNumber) {
		this.homeNumber = homeNumber;
	}

	public String getOfficeNumber() {
		return officeNumber;
	}

	public void setOfficeNumber(String officeNumber) {
		this.officeNumber = officeNumber;
	}

	public boolean isForcePasswordChangeNext() {
		return forcePasswordChangeNext;
	}

	public void setForcePasswordChangeNext(boolean forcePasswordChangeNext) {
		this.forcePasswordChangeNext = forcePasswordChangeNext;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getStreetLine1() {
		return streetLine1;
	}

	public void setStreetLine1(String streetLine1) {
		this.streetLine1 = streetLine1;
	}

	public String getStreetLine2() {
		return streetLine2;
	}

	public void setStreetLine2(String streetLine2) {
		this.streetLine2 = streetLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "User [displayName=" + displayName + ", mail=" + mail + ", name=" + name
				+ ", password=" + password + ", homeNumber=" + homeNumber
				+ ", officeNumber=" + officeNumber + ", forcePasswordChangeNext="
				+ forcePasswordChangeNext + ", mobileNumber=" + mobileNumber
				+ ", description=" + description + "]";
	}

	@JsonIgnore
	public DBUser getUser(){
		return new DBUser(this.getName(),this.getPassword(),this.getMail(),this.getDisplayName(),this.getMobileNumber());
	}
}
