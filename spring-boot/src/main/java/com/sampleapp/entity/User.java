/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sampleapp.entity;

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
	public char[] password;

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

	@JsonProperty("ReCaptchaToken")
	public String ReCaptchaToken;

	public User(String displayName, String email, String name, char[] password) {
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

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
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
