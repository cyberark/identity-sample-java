import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: [
    './app.component.css',
  ],
})
export class AppComponent {
  title = 'CyberArk Identity API Demo';

  ngOnInit() {
    document.cookie = document.cookie.includes('flow2') ? 'flow=flow2' : 'flow=flow1';
  }
}
