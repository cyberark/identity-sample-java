import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

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
    this.router.navigate(['register']);
  }

  onLoginClick() {
    this.router.navigate(['basiclogin']);
  }
}
