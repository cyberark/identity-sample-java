import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'apionly',
  templateUrl: './apionly.component.html',
  styleUrls: ['./apionly.component.css']
})
export class ApionlyComponent implements OnInit {

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
  }

  onNext() {
    this.router.navigate(['user'])
  }
}