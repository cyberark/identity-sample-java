import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MFAWidgetComponent } from './mfawidget.component';

describe('MFAWidgetComponent', () => {
  let component: MFAWidgetComponent;
  let fixture: ComponentFixture<MFAWidgetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MFAWidgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MFAWidgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
