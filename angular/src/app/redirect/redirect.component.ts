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

@Component({
    selector: 'redirect',
    templateUrl: './redirect.component.html',
})
export class RedirectComponent implements OnInit {

    loading = false;

    constructor(
        private router: Router
    ) { }

    ngOnInit() {
        if (window.location.hash.length > 0) {
            const authParams = this.parseParms(window.location.hash.substring(1));
            this.router.navigateByUrl('/api&oidc', {state: authParams});
        } else {
            //Auth code flow
            const code = this.parseParms(window.location.search.substring(1));
            this.router.navigateByUrl('/api&oidc', {state: code});
        }
    }

    // Parses the URL parameters and returns an object
    parseParms(str) {
        let pieces = str.split("&"), data = {}, i, parts;
        // process each query pair
        for (i = 0; i < pieces.length; i++) {
            parts = pieces[i].split("=");
            if (parts.length < 2) {
                parts.push("");
            }
            data[decodeURIComponent(parts[0])] = decodeURIComponent(parts[1]);
        }
        return data;
    }
}
