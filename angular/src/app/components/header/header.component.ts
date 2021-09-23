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

import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../login/login.service';
import { UserService } from 'src/app/user/user.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { getStorage, setStorage } from 'src/app/utils';

const imgSrc = "../../../assets/images/acme_logo.png";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  @Input() isLoginVisible: boolean = false;
  @Input() isSignUpVisible: boolean = false;
  @Input() isHomeVisible: boolean = false;
  @Input() isSettingsVisible: boolean = false;
  @Input() isLogoutVisible: boolean = false;

  page = "home";
  name = "";
  signOutMenu = false;
  homeMenu = false;
  loading = false;
  imageSource: string;

  constructor(
    private loginService: LoginService,
    private userService: UserService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) { }

  ngOnInit() {
    if (getStorage("settings") === null) {
      this.loading = true;
      this.userService.getSettings().subscribe(
        data => {
          this.loading = false;
          if (data.Result.appImage) {
            this.imageSource = data.Result.appImage;
            setStorage("settings", JSON.stringify(data.Result));
          } else {
            console.log("Incorrect data response");
          }
        }, error => {
          this.loading = false;
          console.log("Error response");
        }
      );
    } else {
      this.imageSource = JSON.parse(getStorage("settings")).appImage;
    }

    if (getStorage("displayName") !== null && getStorage("displayName") !== "") {
      this.name = getStorage("displayName");
    } else if (getStorage("username") !== null) {
      this.name = getStorage("username");
    }

    switch (this.router.url) {
      case "/":
      case "/login":
      case "/register":
        this.page = "home";
        break;
      case "/fundtransfer":
      case "/fundtransfer?isFundTransferSuccessful=true":
      case "/mfawidget?fromFundTransfer=true":
      case "/user":
      case "/custom":
        this.page = "user";
        break;
    }
  }

  getTrimmedImageData(appImage) {
    return appImage.substr(1, appImage.length - 2);
  }

  checkPage(page: String) {
    return this.page == page;
  }

  checkFlow2UserPage() {
    return document.cookie.includes('flow2') && this.page === 'user';
  }

  checkSelectedTab(href: String) {
    if (this.router.url == href) {
      return true;
    }
  }

  notRegister() {
    return !this.checkSelectedTab('/register');
  }

  onTabClick(href: String) {
    if (href === 'login' && document.cookie.includes('flow2')) href = 'basiclogin';
    this.router.navigate([href]);
    return false;
  }

  toggleHomeMenu() {
    return this.homeMenu = !this.homeMenu;
  }

  toggleSignOutMenu() {
    return this.signOutMenu = !this.signOutMenu;
  }

  logout() {
    this.loginService.logout().subscribe(
      data => {
        if (data.success == true) {
          const routeToNavigate = document.cookie.includes('flow2') ? 'flow2' : 'flow1';
          localStorage.clear();
          this.router.navigate([routeToNavigate]);
        }
      },
      error => {
        console.log(error);
      }
    );
  }
}