import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'apiplusoidc',
  templateUrl: './apiplusoidc.component.html',
  styleUrls: ['./apiplusoidc.component.css']
})
export class ApiPlusOidc implements OnInit {

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
  }

  onNext() {
    this.router.navigate(['user'])
  }
}