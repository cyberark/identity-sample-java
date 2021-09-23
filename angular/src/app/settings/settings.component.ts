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
import { FormGroup, FormControl, Validators, NgForm, FormBuilder } from '@angular/forms';
import { SafeResourceUrl } from '@angular/platform-browser';
import { getStorage, setStorage, Settings, validateAllFormFields } from '../utils';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})

export class SettingsComponent implements OnInit {
  @ViewChild('divToScroll') divToScroll: ElementRef;

  settingsForm: FormGroup;
  messageType = "info";
  errorMessage = "";
  loading = false;
  customData: any;
  showModal = false;
  selectedFile: File;
  imagePreview: SafeResourceUrl;
  settings: Settings;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private userService: UserService,
  ) { }

  ngOnInit() {
    this.settingsForm = this.formBuilder.group({
      "appImage": ['',],
      "tenantURL": ['', Validators.required],
      "loginSuffix": ['', Validators.required],
      "roleName": ['', Validators.required],
      "oauthAppId": ['', Validators.required],
      "oauthServiceUserName": ['', Validators.required],
      "oauthServiceUserPassword": ['', Validators.required],
      "oauthScopesSupported": ['', Validators.required],
      "oidcAppId": ['', Validators.required],
      "oidcClientId": ['', Validators.required],
      "oidcScopesSupported": ['', Validators.required],
    });

    this.settings = JSON.parse(getStorage('settings'));
    if(this.settings && this.settings.appImage){
      this.imagePreview = this.settings.appImage;
      this.settings.appImage = "";
      this.settingsForm.setValue(this.settings);
    }
  }

  checkMessageType(){
    return this.messageType === "info";
  }

  onImageUpload(event) {
    this.selectedFile = event.target.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result.toString();
    };
    reader.readAsDataURL(this.selectedFile);
  }

  onSave(){
    if(!validateAllFormFields(this.settingsForm)) return;

    this.loading = true;
    let data = this.settingsForm.value;
    data.appImage = this.imagePreview;
    this.userService.setSettings(data).subscribe(
      d => {
        this.loading = false;
        setStorage('settings', JSON.stringify(data));
        this.errorMessage = d.Result;
        this.divToScroll.nativeElement.scrollTop = 0;
      },
      error => {
        this.loading = false;
        this.messageType = "error";
        this.errorMessage = error.ErrorMessage;
        this.divToScroll.nativeElement.scrollTop = 0;
      }
    )
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