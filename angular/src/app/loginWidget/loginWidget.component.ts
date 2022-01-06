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

import { Component, OnInit } from '@angular/core';
import { environment } from '../../environments/environment';
import { BasicLoginService } from '../basiclogin/basiclogin.service';
import { AuthorizationService } from '../metadata/authorizationservice';
declare let LaunchLoginView: any;
import { ActivatedRoute, Router } from '@angular/router';
import { getStorage, setStorage, APIErrStr, TokenMetadataRequest, GrantType, AuthorizationFlow, Settings } from '../utils';
import { ajax, css } from "jquery";
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-loginWidget',
  templateUrl: './loginWidget.component.html',
  styleUrls: ['./loginWidget.component.css']
})

export class LoginWidgetComponent implements OnInit {

  tokenReq = new TokenMetadataRequest();
  errorMessage: string = APIErrStr;
  tokenSet = {};

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginService: BasicLoginService,
    private authorizationService: AuthorizationService,
    private userService: UserService
  ) { }

  ngOnInit() {
    var me = this;
    const settings: Settings = JSON.parse(getStorage('settings'));
    const showSignUpWidget = getStorage("showSignUpWidget") === "true";

    if (showSignUpWidget) {
      this.tokenReq.authFlow = AuthorizationFlow.OAUTH;
      this.tokenReq.grant_type = GrantType.client_credentials;
      this.authorizationService.getTokenSet(this.tokenReq).subscribe({
        next: data => {
          this.tokenSet = data.Result;
        },
        error: error => {
          this.showError(me, error);
        },
        complete: () => {
          this.loginView(me, settings, showSignUpWidget);
        }
      });
    }
    else {
      this.loginView(me, settings, showSignUpWidget);
    }

  }

  loginView(me, settings, showSignUpWidget) {
    let config = {
      "containerSelector": "#cyberark-login",
      "showSignup": showSignUpWidget,
      "allowSignUpLink": true,
      "signUpLinkText": "Sign Up",
      "apiFqdn": settings.tenantURL.split("/")[2],
      success: function (AuthData) { me.loginSuccessHandler(AuthData, me) },
    };

    if (showSignUpWidget) {
      config["bearerToken"] = this.tokenSet['access_token'];
    }
    else {
      const username = getStorage('username');
      config["widgetId"] = settings.loginWidgetId;
      config["username"] = username;
      config["autoSubmitUsername"] = username ? true : false;
    }

    LaunchLoginView(config);
  }

  onRetry(): void {
    this.router.navigate(['flow2']);
  }

  showError(context, error) {
    context.errorMessage = error.error.ErrorMessage;
    (<any>$('#errorPopup')).modal();
  }

  loginSuccessHandler(AuthData, context) {

    this.authorizationService.getPKCEMetadata().subscribe({
      next: pkceMetadata => {
        this.loginService.authorize(AuthData.Auth, AuthData.User, pkceMetadata.Result.codeChallenge).subscribe({
          next: data => {
            this.loginService.setAuthCookie("", data.Result.AuthorizationCode, AuthData.User, pkceMetadata.Result.code_verifier).subscribe({
              next: data => {
                if (data && data.Success == true) {
                  context.setUserDetails(AuthData);
                  context.fromFundTransfer = JSON.parse(context.route.snapshot.queryParamMap.get('fromFundTransfer'));

                  if (context.fromFundTransfer) {
                    context.router.navigate(['fundtransfer'], { queryParams: { isFundTransferSuccessful: true } });
                  } else {
                    context.router.navigate(['user']);
                  }

                } else {
                  context.router.navigate(['loginWidget']);
                }
              },
              error: error => {
                this.showError(context, error);
                context.router.navigate(['loginWidget']);
              }
            });
          },
          error: error => {
            this.showError(context, error);
          }
        })
      }
    });
  }

  setUserDetails(result: any) {
    setStorage("userId", result.UserId);
    setStorage("username", result.User);
    setStorage("displayName", result.DisplayName);
    setStorage("tenant", result.PodFqdn);
    setStorage("customerId", result.CustomerID);
    setStorage("custom", result.Custom);
  }
}