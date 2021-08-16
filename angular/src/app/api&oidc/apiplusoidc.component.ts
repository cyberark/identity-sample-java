import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OIDCService } from './oidc.service';

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

  constructor(
    private router: Router,
    private oidcService: OIDCService
  ) { }

  ngOnInit() {
    const state = history.state;
    delete state.navigationId;
    this.authResponse = state;

    if (this.authResponse['error']) {
      this.hideAccordian = true;
      return;
    }

    if (this.authResponse['code']) {
      this.oidcService.getTokenSet(this.authResponse['code'], localStorage.getItem('codeVerifier')).subscribe(
        data => {
          this.tokenSet = data.Result;
          this.getClaims(this.tokenSet['id_token']);
          this.getUserInfo(this.tokenSet['access_token']);
        }
      )
    } else {
      //implicit flow
      this.hideTokensAccordian = true;
      this.getClaims(this.authResponse['id_token']);
      this.getUserInfo(this.authResponse['id_token']);
    }

  }
  
  getClaims(idToken : string) {
    this.oidcService.getClaims(idToken).subscribe(
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

    this.oidcService.getUserInfo(accessToken).subscribe(
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