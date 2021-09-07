import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterComponent } from '../components/footer/footer.component';
import { HeaderComponent } from '../components/header/header.component';
import { Flow2Component } from './flow2.component';

describe('Flow2Component', () => {
  let component: Flow2Component;
  let fixture: ComponentFixture<Flow2Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Flow2Component, HeaderComponent, FooterComponent ],
      imports: [RouterTestingModule, HttpClientTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Flow2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
