import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterComponent } from '../components/footer/footer.component';
import { HeaderComponent } from '../components/header/header.component';
import { Flow1Component } from './flow1.component';

describe('Flow1Component', () => {
  let component: Flow1Component;
  let fixture: ComponentFixture<Flow1Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Flow1Component, HeaderComponent, FooterComponent ],
      imports: [RouterTestingModule, HttpClientTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Flow1Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
