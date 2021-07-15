import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../login/login.service';
import { UserService } from 'src/app/user/user.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

const imgSrc = "../../../assets/images/acme_logo.png";
const accentcolor = "#313131";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  @Input() isLoginVisible: boolean = false;
  @Input() isSignUpVisible: boolean = false;
  @Input() isHomeVisible: boolean = false;

  page = "home";
  name = "";
  signOutMenu = false;
  homeMenu = false;
  loading = false;
  custom = localStorage.getItem("custom") == "true";
  imageSource: string;
  accentColor = accentcolor;
  ribbonColor = accentcolor;

  constructor(
    private loginService: LoginService,
    private userService: UserService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) { }

  ngOnInit() {
    if (localStorage.getItem("logo") === null || localStorage.getItem("logo") === "") {
      this.loading = true;
      this.userService.getClientCustomData().subscribe(
        data => {
          this.loading = false;
          if (data.appImage) {
            this.imageSource = imgSrc;
            localStorage.setItem("logo", this.imageSource);
            localStorage.setItem("accent", data.accentColor);
            this.accentColor = data.accentColor;
            localStorage.setItem("ribbon", data.ribbonColor);
            this.ribbonColor = data.ribbonColor;
          } else {
            console.log("Incorrect data response");
          }
        }, error => {
          this.loading = false;
          console.log("Error response");
        }
      );
    } else {
      this.imageSource = imgSrc;
      this.accentColor = localStorage.getItem("accent") || accentcolor;
      this.ribbonColor = localStorage.getItem("ribbon") || accentcolor;
    }

    if (localStorage.getItem("displayName") !== null && localStorage.getItem("displayName") !== "") {
      this.name = localStorage.getItem("displayName");
    } else if (localStorage.getItem("username") !== null) {
      this.name = localStorage.getItem("username");
    }

    switch (this.router.url) {
      case "/":
      case "/login":
      case "/register":
        this.page = "home";
        break;
      case "/dashboard":
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
    this.signOutMenu = false;
    this.loginService.logout().subscribe(
      data => {
        if (data.success == true) {
          localStorage.clear();
          this.router.navigate(['']);
        }
      },
      error => {
        console.log(error);
      });
  }
}