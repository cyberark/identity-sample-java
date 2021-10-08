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

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sampleapp.entity.TokenStore;
import com.sampleapp.service.AuthService;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.jsoup.Jsoup;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.util.StringUtils;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Boolean enableMFAWidgetFlow = readServletCookie(request,"flow").get().equals("flow2");
        if (enableMFAWidgetFlow && findCookie(request, ".ASPXAUTH") != null) {
            String xAuth = readServletCookie(request,".ASPXAUTH").get();
            TokenStore token = authService.GetTokenStore(xAuth);
            if (token == null) {
                throw new SecurityException();
            }
            RequestContextHolder.currentRequestAttributes().setAttribute("UserTokenStore", token, 1);
        }
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/userops");
    }

    public static Optional<String> readServletCookie(HttpServletRequest request, String name){
        return Arrays.stream(request.getCookies())
                .filter(cookie->name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findAny();
    }

    /**
     *  Remove escape characters like Html/Js scripts from input if present
     * @param str Input string
     * @return sanitize string
     */
    public static String cleanIt(String str) {
        return Jsoup.clean(
                StringEscapeUtils.escapeHtml4(StringEscapeUtils.escapeEcmaScript(StringUtils.replace(str, "'", "''")))
                , Safelist.basic());
    }

    public static String findCookie(HttpServletRequest request, String name){
        Cookie[] cookieArray = request.getCookies();
        for (Cookie cookie : cookieArray) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
