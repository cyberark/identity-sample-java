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

import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthorizationService } from '../api&oidc/authorizationservice';
import { AuthorizationFlow, AuthorizationMetadataRequest, buildAuthorizeURL, OAuthFlow } from '../utils';

@Component({
  selector: 'oauthflow',
  templateUrl: './oauthflow.component.html',
})

export class OAuthFlowComponent implements OnInit {
  @ViewChild('authorizeBtn') authorizeBtn;

  loginForm: FormGroup;
  username: string = localStorage.getItem('username');
  selectedFlow: OAuthFlow = OAuthFlow.auth;
  loading = false;
  authURL = "Authorize URL";
  isPasswordVisible = true;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authorizationService: AuthorizationService
  ) { }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      password: ['', Validators.required],
    })

    localStorage.setItem('authFlow', AuthorizationFlow.OAUTH);
    localStorage.setItem('oauthflow_flow', OAuthFlow.auth);
  }

  onSelect(val: OAuthFlow){
    this.selectedFlow = val;
    localStorage.setItem('oauthflow_flow', val);
    if(val !== OAuthFlow.auth){
      this.isPasswordVisible = false;
      localStorage.removeItem('client_secret');
    }
    else this.isPasswordVisible = true;
  }

  onBack(){
    this.router.navigate(['loginprotocols']);
  }

  onAccept(){
    this.loading = true;
    window.location.href = this.authURL + "&AUTH=" + this.authorizationService.readCookie("AUTH");
  }

  /**
   * based on the oauth flow builds the authorization url
   */
  onBuildAuthUrl(){
    let authReqMetaData = new AuthorizationMetadataRequest();
    authReqMetaData.authFlow = AuthorizationFlow.OAUTH;
    authReqMetaData.clientId = this.username;
    authReqMetaData.responseType = this.selectedFlow === OAuthFlow.implicit ? "token" : "code";
    if (this.selectedFlow === OAuthFlow.auth) {
      authReqMetaData.clientSecret = this.loginForm.get('password').value;
      localStorage.setItem('client_secret', this.loginForm.get('password').value);
    }
    if (this.selectedFlow === OAuthFlow.authPKCE){
      this.loading = true;
      this.authorizationService.getPKCEMetadata().subscribe(
        data => {
          this.loading = false;
          authReqMetaData.codeChallenge = data.Result.codeChallenge;
          localStorage.setItem('codeVerifier', data.Result.codeVerifier);
          buildAuthorizeURL(authReqMetaData, this);
        },
        error => {
          this.loading = false;
          console.error(error);
        }
      )
    } else {
      this.loading = true;
      buildAuthorizeURL(authReqMetaData, this);
    }
  }

  // #TODO Move in common util
  public hasError = (controlName: string, errorName: string) => {
    let form = this.loginForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}