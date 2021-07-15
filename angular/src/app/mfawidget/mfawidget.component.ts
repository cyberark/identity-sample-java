import { Component, OnInit } from '@angular/core';
import { environment } from '../../environments/environment';
import { BasicLoginService } from '../basiclogin/basiclogin.service';
declare let LaunchLoginView: any;
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-mfawidget',
  templateUrl: './mfawidget.component.html',
  styleUrls: ['./mfawidget.component.css']
})
export class MFAWidgetComponent implements OnInit {

  private fromFundTransfer = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginService: BasicLoginService
  ) { }

  ngOnInit() {
    var me = this;

    LaunchLoginView({
      "containerSelector": ".cyberark-login",
      "initialTitle": "Login",
      "defaultTitle": "Authentication",
      "allowSocialLogin": true,
      "allowRememberMe": true,
      "allowRegister": true,
      "allowForgotUsername": false,
      "apiFqdn": environment.apiFqdn,
      "username": localStorage.getItem('mfaUsername'),
      autoSubmitUsername: true,
      success: function (AuthData) { me.loginSuccessHandler(AuthData, me) },
    });
  }
  loginSuccessHandler(AuthData, context) {

    this.loginService.authorize(AuthData.Auth).subscribe(
      data => {
        this.loginService.completeLoginUser(localStorage.getItem("sessionUuid"), data.Result.AuthorizationCode).subscribe(
          data => {
            if (data && data.Success == true) {
              context.setUserDetails(AuthData);
              context.fromFundTransfer = JSON.parse(context.route.snapshot.queryParamMap.get('fromFundTransfer'));

              if (context.fromFundTransfer) {
                context.router.navigate(['fundtransfer'], { queryParams: { isFundTransferSuccessful: true } });
              } else {
                context.router.navigate(['user']);
              }

            } else {
              context.router.navigate(['login']);
            }
          },
          error => {
            console.error(error);
            context.router.navigate(['login']);
          });
      },
      error => {
        if (error.error.Success == false) {
          localStorage.setItem("registerMessageType", "error");
          localStorage.setItem("registerMessage", error.error.ErrorMessage);
        }
        console.error(error);
        context.router.navigate(['login']);
      });
  }
  setUserDetails(result: any) {
    localStorage.setItem("userId", result.UserId);
    localStorage.setItem("username", result.User);
    localStorage.setItem("displayName", result.DisplayName);
    localStorage.setItem("tenant", result.PodFqdn);
    localStorage.setItem("customerId", result.CustomerID);
    localStorage.setItem("social", "false");
    localStorage.setItem("custom", result.Custom);
  }
}