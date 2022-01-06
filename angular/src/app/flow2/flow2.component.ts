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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { setStorage } from '../utils';

@Component({
  selector: 'flow2',
  templateUrl: './flow2.component.html',
})
export class Flow2Component implements OnInit {

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
  }

  onSignUpClick() {
    setStorage("showSignUpWidget", "true");
    this.router.navigateByUrl('/loginWidget');  }

  onLoginClick() {
    setStorage("showSignUpWidget", "false");
    this.router.navigateByUrl('/loginWidget');  }
}
