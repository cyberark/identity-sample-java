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
import { getStorage, setStorage, Settings, validateAllFormFields, APIErrStr } from '../utils';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { HttpStatusCode } from '@angular/common/http';

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
  loading = false;
  showConsent = false;
  leftContainerStyle: SafeStyle = "";// = this.domSanitizer.bypassSecurityTrustStyle("");

  @ViewChild('divToScroll', { static: true }) divToScroll: ElementRef;

  constructor(
    private router: Router,
    private userService: UserService,
    private formBuilder: FormBuilder,
    private domSanitizer: DomSanitizer,
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
        Validators.pattern("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,64}$")
      ])],
      "ConfirmPassword": ['', Validators.required],
      "MobileNumber": [''],
      "MFA": [false],
    }, { updateOn: 'blur' });

    const settings: Settings = JSON.parse(getStorage("settings"));
    if (settings && settings.appImage) {
      this.leftContainerStyle = this.domSanitizer.bypassSecurityTrustStyle(
        `background-image: url('${settings.appImage}'); background-size: contain;`
      );
    }

    if (getStorage("userId") !== null) {
      this.loading = true;
      this.userService.getById(getStorage("userId")).subscribe({
        next: data => {
          this.loading = false;
          if (data.success) {
            let userControls = this.registerForm.controls;
            let user = data.Result;
            if (user.DirectoryServiceType == "FDS") {
              this.registerForm.disable();
            } else {
            }
            userControls.Name.setValue(user.Name);
            userControls.Mail.setValue(user.Mail);
            userControls.DisplayName.setValue(user.DisplayName);
            userControls.MobileNumber.setValue(user.MobileNumber);
          } else {
            this.setMessage("error", data.Message); 
          }
        },
        error: error => {
          let errorMessage = APIErrStr;
          if (error.status == HttpStatusCode.Forbidden && error.error) {
            errorMessage = error.error.ErrorMessage;
          }
          console.error(error);
          this.setMessage("error", errorMessage); 
        }
      });
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
      let fieldArray = ["Name", "Mail", "DisplayName", "MobileNumber", "MFA"];
      if (!this.validateFormFields(fieldArray)) {
        this.divToScroll.nativeElement.scrollTop = 0;
        return;
      }
    } else {
      const isValid = validateAllFormFields(this.registerForm);
      this.matchPasswords();
      if (!isValid || !this.matchPasswordsCheck) {
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
      this.userService.update(user, getStorage("userId")).subscribe({
        next: data => {
          this.loading = false;
          if (data.success == true) {
            setStorage("mfaUsername", data.UserName);
            this.setMessage("info", "User information updated successfully");
            this.router.navigate(['/user']);
          } else {
            this.setMessage("error", data.Message);
          }
        },
        error: error => {
          let errorMessage = APIErrStr;
          if (error.status == HttpStatusCode.Forbidden && error.error) {
            errorMessage = error.error.ErrorMessage;
          }
          console.error(error);
          this.setMessage("error", errorMessage); 
        }
      });
    } else {
      user = Object.assign({}, form);
      this.userService.getClientIP().subscribe({
        next: ipData => {
          this.userService.register(user, ipData.ip, true).subscribe({
            next: data => {
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
            error: error => {
              console.error(error);
              this.setMessage("error", APIErrStr); 
            }
          });
        },
        error: error => {
          console.error(error);
          this.setMessage("error", error.message);
        }
      });
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
  public hasError = (controlName: string, errorName: string) => {
    let form = this.registerForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}
