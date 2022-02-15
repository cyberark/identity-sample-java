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

	@JsonProperty("MobileNumber")
	private String mobileNumber;

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

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	@Override
	public String toString() {
		return "User [displayName=" + displayName + ", mail=" + mail + ", name=" + name
				+ ", password=" + password + ", mobileNumber=" + mobileNumber + "]";
	}

	@JsonIgnore
	public DBUser getUser(){
		return new DBUser(this.getName(),this.getPassword(),this.getMail(),this.getDisplayName(),this.getMobileNumber());
	}
}
