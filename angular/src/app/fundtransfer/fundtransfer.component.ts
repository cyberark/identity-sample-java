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

import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HeaderComponent } from '../components/header/header.component';
import { FormGroup, NgForm, FormControl } from '@angular/forms';
import { getStorage, validateAllFormFields } from '../utils';

@Component({
    selector: 'app-fundtransfer',
    templateUrl: './fundtransfer.component.html',
    styleUrls: ['./fundtransfer.component.css']
})

export class FundTransferComponent implements OnInit {
    @ViewChild(HeaderComponent, { static: true })
    private header: HeaderComponent;
    private isFundTransferSuccessful = false;

    loading = false;
    submitButtonText = "Transfer";
    fundTransferForm: FormGroup;
    messageType = "error";
    errorMessage = "";

    constructor(
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        this.fundTransferForm = new FormGroup({
            'amount': new FormControl(null)
          });

        if (getStorage("userId") == null) {
            this.router.navigate(['/login']);
        }
        this.isFundTransferSuccessful = JSON.parse(this.route.snapshot.queryParamMap.get('isFundTransferSuccessful'));

        if (this.isFundTransferSuccessful) {
            this.setMessage("info", "Funds transferred successfully");
        }
    }

    transferFunds(form: NgForm) {
        if (!validateAllFormFields(this.fundTransferForm)) {
            return;
        }
        this.router.navigate(['mfawidget'], { queryParams: { fromFundTransfer: true } });
    }

    numberOnly(event): boolean {
        const charCode = (event.which) ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
          return false;
        }
        return true;
    }
    
    hasError(controlName: string, errorName: string) {
        let form = this.fundTransferForm;
        let control = form.controls[controlName];
        return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
    }

    checkMessageType() {
        return this.messageType == "info";
    }
   
    setMessage(messageType: string, message: string) {
        this.messageType = messageType;
        this.errorMessage = message;
    }

    checkSelectedTab(href: string) {
        if (this.router.url == href) {
            return true;
        }
    }

    onClick(event) {
        if (event.target.attributes.id && event.target.attributes.id !== "" && event.target.attributes.id.nodeValue === "signOutButton") {
            return;
        }
        this.header.signOutMenu = false;
    }
}
