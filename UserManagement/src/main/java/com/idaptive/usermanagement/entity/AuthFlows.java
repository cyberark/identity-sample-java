package com.idaptive.usermanagement.entity;

import com.idaptive.usermanagement.service.BaseAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
