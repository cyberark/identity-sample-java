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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, Validators, FormBuilder, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { SafeResourceUrl } from '@angular/platform-browser';
import Tagify from '@yaireo/tagify';
import { getStorage, setStorage, Settings, validateAllFormFields } from '../utils';
import { UserService } from '../user/user.service';
import { AppComponent } from '../app.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})

export class SettingsComponent implements OnInit {
  @ViewChild('divToScroll', { static: true }) divToScroll: ElementRef;

  settingsForm: FormGroup;
  messageType = "info";
  errorMessage = "";
  loading = false;
  customData: any;
  showModal = false;
  selectedFile: File;
  imagePreview: SafeResourceUrl;
  settings: Settings;
  hasAppLogoError = false;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private userService: UserService,
    private appComp: AppComponent
  ) { }

  ngOnInit() {
    this.settingsForm = this.formBuilder.group({
      "appImage": ['',],
      "tenantURL": ['', Validators.compose([
        Validators.required,
        Validators.maxLength(80),
        this.validateURL(),
      ])],
      "loginSuffix": ['', Validators.required],
      "roleName": ['', Validators.required],
      "loginWidgetId": ['', Validators.compose([
        Validators.required,
        Validators.pattern('^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$')
      ])],
      "mfaWidgetId": ['', Validators.compose([
        Validators.required,
        Validators.pattern('^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$')
      ])],
      "oauthAppId": ['', Validators.required],
      "oauthServiceUserName": ['', Validators.compose([
        Validators.required,
        Validators.pattern(".*@.*")
      ])],
      "oauthServiceUserPassword": ['', Validators.compose([
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(64)
      ])],
      "oauthScopesSupported": ['', Validators.required],
      "oidcAppId": ['', Validators.required],
      "oidcClientId": ['', Validators.compose([
        Validators.required,
        Validators.pattern('^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$')
      ])],
      "oidcScopesSupported": ['', Validators.required],
    });

    document.querySelectorAll('input[name=basic]').forEach(ele => {
      new Tagify(ele, {
        originalInputValueFormat: valuesArr => valuesArr.map(item => item.value).join(' '),
        delimiters: " "
      });
    });

    this.settings = JSON.parse(getStorage('settings'));
    if(this.settings && this.settings.appImage){
      this.imagePreview = this.settings.appImage;
      this.settings.appImage = "";
      this.settingsForm.setValue(this.settings);
    }
  }

  validateURL(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      let isValid = false;
      try {        
        const url = new URL(control.value);
        isValid = true;
      } catch (error) {
        isValid = false;
      }
      return isValid ? null : { validateURL: control.value };
    }
  }

  onOIDCScopeChange(val: string) {
    this.settingsForm.controls.oidcScopesSupported.setValue(val);
  }

  onOAuthScopeChange(val: string) {
    this.settingsForm.controls.oauthScopesSupported.setValue(val);
  }

  checkMessageType(){
    return this.messageType === "info";
  }

  onImageUpload(event) {
    this.selectedFile = event.target.files[0];
    const fileName = this.selectedFile.name;
    if(this.selectedFile.size > 1000000 || this.checkImageFileExt(fileName.substring(fileName.lastIndexOf('.')+1, fileName.length))){
      this.hasAppLogoError = true;
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result.toString();
    };
    reader.readAsDataURL(this.selectedFile);
  }

  checkImageFileExt(extension: string){
    return !["png","jpg","gif","ico","bmp"].includes(extension);
  }

  onSave(){
    if(!validateAllFormFields(this.settingsForm)) return;

    this.loading = true;
    let data = this.settingsForm.value;
    data.appImage = this.imagePreview;
    this.userService.setSettings(data).subscribe({
      next: d => {
        this.loading = false;
        setStorage('settings', JSON.stringify(data));
        this.appComp.addChildNodes();
        this.messageType = "info"
        this.errorMessage = d.Result;
        this.divToScroll.nativeElement.scrollTop = 0;
      },
      error: error => {
        console.error(error);
        this.loading = false;
        this.messageType = "error";
        this.errorMessage = error.error.ErrorMessage;
        this.divToScroll.nativeElement.scrollTop = 0;
      }
    })
  }

  onCancel(){
    this.router.navigate(['/']);
  }

  public hasError = (controlName: string, errorName: string) => {
    let form = this.settingsForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}