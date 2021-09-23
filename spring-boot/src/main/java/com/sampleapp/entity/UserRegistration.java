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

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegistration{

	@JsonProperty("user")
	private User user;

	@JsonProperty("isMfa")
	private boolean isMfa;

	public UserRegistration(){

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean getIsMfa() {
		return isMfa;
	}

	public void setIsMfa(boolean isMfa) {
		this.isMfa = isMfa;
	}

}