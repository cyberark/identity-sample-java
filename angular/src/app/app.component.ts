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

import { Component } from '@angular/core';
import { getStorage, Settings, setStorage } from './utils';
import { UserService } from 'src/app/user/user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: [
    './app.component.css',
  ],
})
export class AppComponent {
  title = 'CyberArk Identity API Demo';

  constructor(
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit() {
    
    if (document.cookie.includes('flow1'))
      {
        document.cookie='flow=flow1';
      }
      else if (document.cookie.includes('flow2'))
      {
        document.cookie='flow=flow2';
      }
      else document.cookie='flow=flow3';

    if (getStorage("settings") === null) {
      this.userService.getSettings().subscribe({
        next: data => {
          if (data.Result.appImage) {
            setStorage("settings", JSON.stringify(data.Result));
          }
         
          if (data.Result.tenantURL) {
            this.addChildNodes();
          } else {
            this.router.navigate(["settings"]);
          }
        }, 
        error: error => {
          console.error(error);
        }
      });
    } else {
      this.addChildNodes();
    }
  }

  addChildNodes() {
    var settings: Settings = JSON.parse(getStorage('settings'));

    if (settings) {
      let node = document.createElement('script');
      node.src = settings.tenantURL + "/vfslow/lib/uibuild/standalonelogin/login.js";
      node.type = 'text/javascript';
      document.getElementsByTagName('head')[0].appendChild(node);

      let linkNode = document.createElement('link');
      linkNode.href = settings.tenantURL + "/vfslow/lib/uibuild/standalonelogin/css/login.css";
      linkNode.type = 'text/css';
      linkNode.rel = 'stylesheet';
      document.getElementsByTagName('head')[0].appendChild(linkNode);
    }
  }
}
