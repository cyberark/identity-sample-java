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
