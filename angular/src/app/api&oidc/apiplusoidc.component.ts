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
import { Router } from '@angular/router';
import { AuthorizationFlow } from '../utils';
import { AuthorizationService } from './authorizationservice';

@Component({
  selector: 'apiplusoidc',
  templateUrl: './apiplusoidc.component.html',
  styleUrls: ['./apiplusoidc.component.css']
})
export class ApiPlusOidc implements OnInit {

  tokenSet = {};
  claims = {};
  userInfo = {};
  authResponse = {};
  loading = false;
  hideAccordian = false; 
  hideTokensAccordian = false;
  isOauthFlow = localStorage.getItem('authFlow') === AuthorizationFlow.OAUTH;
  heading: string = this.isOauthFlow ? 'OAuth Metadata' : 'OIDC Metadata';

  constructor(
    private router: Router,
    private authorizationService: AuthorizationService
  ) { }

  ngOnInit() {
    const state = history.state;
    delete state.navigationId;
    this.authResponse = state.authResponse;

    if (this.authResponse['error']) {
      this.hideAccordian = true;
      return;
    }

    if (this.authResponse['code']) {
      this.loading = true;
      this.authorizationService.getTokenSet(state.tokenReq).subscribe(
        data => {
          this.loading = false;
          this.tokenSet = data.Result;
          this.getClaims(this.tokenSet['access_token']);
          this.getUserInfo(this.tokenSet['access_token']);
        },
        error => {
          this.loading = false;
          console.error(error);
        }
      )
    } else {
      //implicit flow
      this.hideTokensAccordian = true;
      this.loading = true;
      this.getClaims(this.authResponse['access_token']);
      this.getUserInfo(this.authResponse['access_token']);
      this.loading = false;
    }

  }
  
  getClaims(idToken : string) {
    this.authorizationService.getClaims(idToken).subscribe(
      data => {
        if (data && data.Success == true) {
            this.claims = data.Result;
        } 
      },
      error => {
        console.error(error);
      });
  }

  getUserInfo(accessToken : string){
    if (this.isOauthFlow) return;
    this.authorizationService.getUserInfo(accessToken).subscribe(
      data => {
        if (data && data.Success == true) {
            this.userInfo = data.Result;
        } 
      },
      error => {
        console.error(error);
      });
  }

  dataKeys(object : Object) { return Object.keys(object); }

  onNext() {
    this.router.navigate(['user'])
  }
}