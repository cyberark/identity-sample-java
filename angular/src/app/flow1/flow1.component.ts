import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'flow1',
  templateUrl: './flow1.component.html',
  styleUrls: ['./flow1.component.css']
})
export class Flow1Component implements OnInit {

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
  }

  onSignUpClick() {
    this.router.navigate(['register']);
  }

  onLoginClick() {
    this.router.navigate(['login']);
  }
}
