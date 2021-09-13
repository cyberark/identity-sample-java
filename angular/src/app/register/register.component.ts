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

import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, NgForm, Validators, FormControl, ValidatorFn, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';

import { UserService } from '../user/user.service';
import { HeaderComponent } from '../components/header/header.component';
import { LoginService } from '../login/login.service';
import { getStorage, setStorage } from '../utils';

@Component({
  selector: 'app-root',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})

export class RegisterComponent implements OnInit {
  @ViewChild(HeaderComponent)
  private header: HeaderComponent;

  update = false;
  submitButtonText = "Register";
  btnText = "Register";
  registerForm: FormGroup;
  messageType = "error";
  errorMessage = "";
  matchPasswordsCheck = true;
  socialUser = false;
  loading = false;
  showConsent = false;

  @ViewChild('divToScroll') divToScroll: ElementRef;

  constructor(
    private router: Router,
    private userService: UserService,
    private loginService: LoginService,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    if (getStorage("username") !== null && (this.router.url == "/register")) {
      this.router.navigate(['user']);
    } else if (getStorage("username") == null && (this.router.url == "/user")) {
      this.router.navigate(['/login']);
    }

    this.registerForm = this.formBuilder.group({
      "Name": ['', Validators.required],
      "Mail": ['', Validators.compose([
        Validators.required,
        Validators.email
      ])],
      "DisplayName": ['', Validators.required],
      "Password": ['', Validators.compose([
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(64)
      ])],
      "ConfirmPassword": ['', Validators.required],
      "MobileNumber": [''],
      "MFA": [false],
      "ForcePasswordChangeNext": [false],
      "Description": [''],
      "OfficeNumber": [''], // Validators.pattern('^(?=.*[0-9])[- +()0-9]+$')
      "HomeNumber": [''],
      "street_line_1": [''],
      "street_line_2": [''],
      "address_city": [''],
      "postal_code": [''],
      "state_code": [''],
      "country_code": ['']
    }, { updateOn: 'blur' });

    if (getStorage("userId") !== null) {
      this.loading = true;
      this.userService.getById(getStorage("userId"), JSON.parse(getStorage("social"))).subscribe(
        data => {
          this.loading = false;
          if (data.success) {
            let userControls = this.registerForm.controls;
            let user = data.Result;
            if (user.DirectoryServiceType == "FDS") {
              this.registerForm.disable();
              this.socialUser = true;
            } else {
              this.socialUser = false;
            }
            userControls.Name.setValue(user.Name);
            if (JSON.parse(getStorage("social"))) {
              userControls.Mail.setValue(user.EmailAddress);
            } else {
              userControls.Mail.setValue(user.Mail);
            }
            userControls.DisplayName.setValue(user.DisplayName);
            userControls.MobileNumber.setValue(user.MobileNumber);
          } else {
            this.setMessage("error", data.Message);
          }
        },
        error => {
          this.setMessage("error", error.message);
        }
      );
      this.update = true;
      this.submitButtonText = "Update";
    } else {
      this.update = false;
      this.submitButtonText = "Sign up";
      this.registerForm.reset();
    }
  }

  pick(obj: {}, keys) {
    return Object.assign({}, ...keys.map(k => k in obj ? { [k]: obj[k] } : {}))
  }

  checkMessageType() {
    return this.messageType == "info";
  }

  matchPasswords() {
    if (this.registerForm.controls.ConfirmPassword.pristine) {
      return;
    }
    let pass = this.registerForm.controls.Password.value;
    let confirmPass = this.registerForm.controls.ConfirmPassword.value;

    return this.matchPasswordsCheck = pass === confirmPass; // ? null : { notSame: true }
  }

  toggleUserConsentDialog() {
    return this.showConsent = !this.showConsent;
  }

  validateRegisterForm(form: NgForm) {
    if (this.update) {
      if (this.socialUser) {
        return;
      }
      let fieldArray = ["Name", "Mail", "DisplayName", "MobileNumber", "MFA"];
      if (!this.validateFormFields(fieldArray)) {
        this.divToScroll.nativeElement.scrollTop = 0;
        return;
      }
    } else {
      this.validateAllFormFields(this.registerForm);
      this.matchPasswords();
      if (this.registerForm.invalid || !this.matchPasswordsCheck) {
        this.divToScroll.nativeElement.scrollTop = 0;
        return;
      }
    }

    if (this.registerForm.controls.MFA.value && !this.update) {
      return this.toggleUserConsentDialog();
    } else {
      this.registerUser(form);
    }
  }

  registerUser(form: NgForm) {
    let user;
    this.loading = true;

    if (this.update) {
      let fieldArray = ["Name", "Mail", "DisplayName", "MobileNumber", "MFA"];

      user = this.pick(form, fieldArray)
      this.userService.update(user, getStorage("userId")).subscribe(
        data => {
          this.loading = false;
          if (data.success == true) {
            setStorage("mfaUsername", data.UserName);
            this.setMessage("info", "User information updated successfully");
            this.router.navigate(['/user']);
          } else {
            this.setMessage("error", data.Message);
          }
        },
        error => {
          this.setMessage("error", error.message);
        }
      );
    } else {
      user = Object.assign({}, form);
      this.userService.getClientIP().subscribe(
        ipData => {
          this.userService.register(user, ipData.ip, true).subscribe(
            data => {
              this.loading = false;
              if (data.success == true) {
                if (data.Result != null && data.Result.IntegrationResult != null && data.Result.IntegrationResult.IsManualApprovalTriggered == true) {
                  setStorage("registerMessageType", "error");
                  setStorage("registerMessage", "Your account sign-up request is pending approval. You will receive an email once it’s approved, and then you will be able to login.")
                } else {
                  setStorage("registerMessageType", "info");
                  setStorage("registerMessage", "User " + user.Name + " registered successfully. Enter your credentials here to proceed.")
                }
                if (document.cookie.includes('flow1'))
                  this.router.navigate(['/login']);
                else
                  this.router.navigate(['/basiclogin']);

              } else {
                this.setMessage("error", data.Message);
              }
            },
            error => {
              this.setMessage("error", error.message);
            }
          );
        },
        error => {
          this.setMessage("error", error.message);
        }
      );
    }
  }

  setMessage(messageType: string, message: string) {
    this.loading = false;
    this.messageType = messageType;
    this.errorMessage = message;
    this.divToScroll.nativeElement.scrollTop = 0;
  }

  cancelRegister() {
    this.registerForm.reset();
    if (this.update) {
      this.router.navigate(['user']);
    } else {
      this.router.navigate(['/']);
    }
  }

  onClick(event) {
    if (event.target.attributes.id && event.target.attributes.id !== "" && event.target.attributes.id.nodeValue === "signOutButton") {
      return;
    }
    this.header.signOutMenu = false;
  }

  onLogOut() {
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
      });
  }

  // #TODO Move in common util
  validateFormFields(controls: Array<string>): boolean {
    let valid = true;
    for (let i = 0; i < controls.length; i++) {
      let field = this.registerForm.get(controls[i]);
      field.markAsTouched({ onlySelf: true });
      if (field.invalid) {
        valid = false;
      }
    }
    return valid;
  }

  // #TODO Move in common util
  validateAllFormFields(registerForm: FormGroup): any {
    Object.keys(registerForm.controls).forEach(field => {
      const control = registerForm.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });
  }

  // #TODO Move in common util
  public hasError = (controlName: string, errorName: string) => {
    let form = this.registerForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}
