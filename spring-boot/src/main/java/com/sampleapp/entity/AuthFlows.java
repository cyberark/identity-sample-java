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

import com.sampleapp.service.BaseAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;


@Service
public class AuthFlows {

    @Autowired
    public BaseAuthorizationService[] services;

    private EnumMap<AuthorizationFlow, BaseAuthorizationService> enumMap;

    public EnumMap<AuthorizationFlow, BaseAuthorizationService> getEnumMap()
    {
        if(enumMap == null) {
            enumMap = new EnumMap<>(AuthorizationFlow.class);
            for (BaseAuthorizationService service:services) {
                enumMap.put(service.supportedAuthorizationFlow(), service);
            }
        }
        return enumMap;
    }
}
