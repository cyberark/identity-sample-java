import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'logic-protocol',
  templateUrl: './loginprotocol.component.html',
  styleUrls: ['./loginprotocol.component.css']
})
export class LoginProtocolComponent implements OnInit {

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
  }

  onApiOnlyClick() {
    this.router.navigate(['user'])
  }

  onApiOidcClick(){
    this.router.navigate(['api&oidc'])
  }
}
