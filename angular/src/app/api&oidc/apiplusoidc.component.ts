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
  loading = false;

  constructor(
    private router: Router,
    private oidcService: OIDCService
  ) { }

  ngOnInit() {
    this.loading = true;
    this.oidcService.getPKCEMetadata().subscribe(
      pkceMetadata => {
        this.oidcService.buildAuthorizeURL(pkceMetadata.Result.codeChallenge).subscribe(
          data => {
            this.oidcService.authorize(data.Result.authorizeUrl).subscribe(
              data => {
                this.oidcService.getTokenSet(data.Result.AuthorizationCode, pkceMetadata.Result.codeVerifier).subscribe(
                  data => {
                    this.loading = false;
                    if (data && data.Success == true) {
                        this.tokenSet = data.Result;
                        this.getClaims(data.Result.id_token);
                        this.getUserInfo(data.Result.access_token);
                    } 
                  })
              },
              error => {
                console.error(error);
                this.loading = false;
              })
          })
      });
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