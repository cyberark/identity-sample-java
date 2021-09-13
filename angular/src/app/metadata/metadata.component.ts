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
import { LoginService } from '../login/login.service';
import { AuthorizationFlow, getStorage } from '../utils';
import { AuthorizationService } from './authorizationservice';
import { ajax, css } from "jquery";

@Component({
  selector: 'metadata',
  templateUrl: './metadata.component.html',
  styleUrls: ['./metadata.component.css']
})
export class Metadata implements OnInit {

  tokenSet = {};
  claims = {};
  userInfo = {};
  authResponse = {};
  loading = false;
  hideAccordian = false; 
  hideTokensAccordian = false;
  isOauthFlow = getStorage('authFlow') === AuthorizationFlow.OAUTH;
  heading: string = this.isOauthFlow ? 'OAuth Metadata' : 'OIDC Metadata';

  constructor(
    private router: Router,
    private authorizationService: AuthorizationService,
    private loginService: LoginService,
  ) { }

  ngOnInit() {
    const state = history.state;
    delete state.navigationId;
    this.authResponse = state.authResponse;

    if (!this.authResponse || this.authResponse['error']) {
      this.hideAccordian = true;
      (<any>$('#errorPopup')).modal();
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
      const token = this.isOauthFlow ? this.authResponse['access_token'] : this.authResponse['id_token'];
      this.getClaims(token);
      this.getUserInfo(token);
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

  onTryAnotherFlow(){
    this.loginService.logout().subscribe(
      data => {
        if (data.success == true) {
          const routeToNavigate = document.cookie.includes('flow2') ? 'flow2' : 'flow1';
          localStorage.clear();
          this.router.navigate([routeToNavigate]);
        }
      },
      error => {
        console.error(error);
      }
    )
  }

  onOk(){
    if(getStorage('authFlow') === AuthorizationFlow.OAUTH) {
      this.router.navigate(['oauthflow']);
    } else {
      this.router.navigate(['oidcflow']);
    }
  }
}