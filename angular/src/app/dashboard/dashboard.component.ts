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
import { UserService } from '../user/user.service';
import { Router } from '@angular/router';
import { HeaderComponent } from '../components/header/header.component';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})

export class DashboardComponent implements OnInit {
    @ViewChild(HeaderComponent)
    private header: HeaderComponent;

    appList = [];
    loading = false;

    constructor(
        private userService: UserService,
        private router: Router
    ) { }

    ngOnInit() {
        if (localStorage.getItem("username") == null) {
            this.router.navigate(['/login']);
        }

        this.loading = true;
        this.userService.getAllApps(localStorage.getItem("username")).subscribe(
            data => {
                this.loading = false;
                this.createAppList(data);
            }, error => {
                this.loading = false;
            }
        );
    }

    createAppList(data) {
        let tenant = localStorage.getItem("tenant");
        for (let key in data) {
            let app = {
                name: key,
                url: "https://" + tenant + "/run?appkey=" + data[key][0].AppKey + "&amp;customerID=" + localStorage.getItem("customerId"),
                icon: "https://" + tenant + data[key][0].Icon
            }
            this.appList.push(app);
        }
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
