import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl, NgForm, AbstractControl } from '@angular/forms';
import { BasicLoginService } from './basiclogin.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './basiclogin.component.html',
  styleUrls: ['./basiclogin.component.css']
})

export class BasicLoginComponent implements OnInit {
  loginForm: FormGroup;
  loginButtonText ="Login";
  authMessage = "";
  messageType = "error";
  loading = false;
  constructor(
    private formBuilder: FormBuilder,
    private loginService: BasicLoginService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    if (localStorage.getItem("username") !== null) {
      this.router.navigate(['user']);
    }
    if (localStorage.getItem("registerMessageType") !== null) {
      this.messageType = localStorage.getItem("registerMessageType");
      this.authMessage = localStorage.getItem("registerMessage");
      localStorage.setItem("registerMessage", "");
    }
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
   
  }

  // Getter for easy access to form fields
  get formControls() { return this.loginForm.controls; }

  checkMessageType() {
    return this.messageType == "info";
  }

  loginUser(form: NgForm) {
    if(!this.validateAllFormFields(this.loginForm)){
      return;
    }
    this.loading = true;
    this.loginService.basicLoginUser(this.formControls.username.value,this.formControls.password.value).subscribe(
      data=>{
        this.loading = false;
        if (data && data.Success == true) {
          this.redirectToMFA(data.Result);
        }else{
          this.onLoginError(data.ErrorMessage);
        }
      },
      error => {
        this.onLoginError(error.error.ErrorMessage);
      });
  }

  onLoginError(message) {
    this.loading = false;
    this.authMessage = message;
    this.messageType = "error";
    this.resetFormFields();
  }
  redirectToMFA(result: any) {
    localStorage.setItem("sessionUuid", result.SessionUuid);
    localStorage.setItem("mfaUsername", result.MFAUserName);
    this.router.navigate(['mfawidget']);
  }

  resetFormFields(): boolean {
    let valid = true;
    Object.keys(this.loginForm.controls).forEach(fieldname => {
      const field = this.loginForm.get(fieldname);
      field.markAsUntouched({ onlySelf: true });
      if (field.invalid) {
        valid = false;
      }
    });
    return valid;
  }

  // #TODO Move in common util
  validateAllFormFields(form: FormGroup): any {
    let valid = true;
    Object.keys(form.controls).forEach(field => {
      const control = form.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
        if (control.invalid) {
          valid = false;
        }
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });
    return valid;
  }

  // #TODO Move in common util
  public hasError = (controlName: string, errorName: string) => {
    let form = this.loginForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}
