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

package com.sampleapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final String[] CSRF_IGNORE = {"/auth/beginAuth", "/BasicLogin", "/user/register", "/updateSettings"};

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/**").permitAll();
		CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfTokenRepository.setCookiePath("/");
		http.csrf()
				.ignoringAntMatchers(CSRF_IGNORE)
				.csrfTokenRepository(csrfTokenRepository);
		http.cors();
	}

	@Bean
	public CorsFilter corsFilter() {
	    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    final CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
	    config.addExposedHeader("Set-Cookie");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("GET");
	    config.addAllowedMethod("OPTIONS");
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	

}
