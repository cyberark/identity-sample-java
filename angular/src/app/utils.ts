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

import { FormControl, FormGroup } from "@angular/forms";

export const authURLStr = "Authorize URL";

export enum AuthorizationFlow {
  OAUTH = "OAUTH",
  OIDC = "OIDC"
}

export enum OAuthFlow {
  auth = "auth",
  implicit = "implicit",
  authPKCE = "authPKCE",
  clientCreds = "clientCreds",
  resourceOwner = "resourceOwner",
}

export enum GrantType {
  authorization_code = "authorization_code",
  client_credentials = "client_credentials",
  password = "password"
}

export enum OidcFlow {
  auth = "auth",
  implicit = "implicit",
  hybrid = "hybrid"
}

export class PKCEMetaData {
  codeVerifier: string;
  codeChallenge: string;
}

export class Settings {
  appImage: string;
  tenantURL: string;
  loginSuffix: string;
  roleName: string;
  oauthAppId: string;
  oauthServiceUserName: string;
  oauthServiceUserPassword: string;
  oauthScopesSupported: string;
  oidcAppId: string;
  oidcClientId: string;
  oidcScopesSupported: string;
}

export class AuthorizationMetadataRequest extends PKCEMetaData {
  authFlow: AuthorizationFlow = AuthorizationFlow.OIDC;
  clientId: string;
  clientSecret: string;
  responseType: string = "code";
}

export class TokenMetadataRequest extends PKCEMetaData {
  authFlow: AuthorizationFlow = AuthorizationFlow.OIDC;
  authorizationCode: string;
  grantType: GrantType = GrantType.authorization_code;
  userName: string;
  password: string;
  clientId: string;
  clientSecret: string;
}

/**
 * Fetches the authorization URL
 * @param authRequest AuthorizationMetadataRequest
 * @param context any - The component class context
 */
export const buildAuthorizeURL = (authRequest: AuthorizationMetadataRequest, context: any) => {
  context.authorizationService.buildAuthorizeURL(authRequest).subscribe(
    data => {
      context.loading = false;
      context.authURL = data.Result.authorizeUrl;
      context.authorizeBtn.nativeElement.disabled = false;
    },
    error => {
      console.error(error);
      context.loading = false;
    }
  );
}

/**
 * stores the key value pair in local storage where value is base64 encoded
 * @param key string key
 * @param val string value
 */
export const setStorage = (key: string, val: string) => {
  localStorage.setItem(key, btoa(val));
}

/**
 * gets the value from local storage based on key and decodes it
 * @param key string key
 * @returns base64 decoded string value
 */
export const getStorage = (key: string) => {
  const val = localStorage.getItem(key);
  if (val) return atob(val);
  else return val;
}

/**
 * post call body string with each param on new line
 * @param payload object having all the request body params
 * @returns string with body param with values in new lines
 */
export const tokenEndpointBody = (payload: Object) => {
  let resultStr = '';
  Object.keys(payload).forEach(k => {
      resultStr += `${k}=${payload[k]}\n`;
  });
  return resultStr;
}

/**
 * Validates are the form fields in the given form group
 * @param form form group instance to be validated
 * @returns true if all fields are valid otherwise false
 */
export const validateAllFormFields = (form: FormGroup): boolean => {
  let valid = true;
  Object.keys(form.controls).forEach(field => {
    const control = form.get(field);
    if (control instanceof FormControl) {
      control.markAsTouched({ onlySelf: true });
      if (control.invalid) {
        valid = false;
      }
    } else if (control instanceof FormGroup) {
      validateAllFormFields(control);
    }
  });
  return valid;
}