import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterComponent } from '../components/footer/footer.component';
import { HeaderComponent } from '../components/header/header.component';
import { Metadata } from './metadata.component';

describe('Metadata', () => {
  let component: Metadata;
  let fixture: ComponentFixture<Metadata>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Metadata, HeaderComponent, FooterComponent ],
      imports: [RouterTestingModule, HttpClientTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    window.history.pushState({},'','')
    fixture = TestBed.createComponent(Metadata);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
