import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { LoginComponent } from './login/login.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CustomComponent } from './custom/custom.component';
import { BasicLoginComponent } from './basiclogin/basiclogin.component'
import { MFAWidgetComponent } from './mfawidget/mfawidget.component';
import { FundTransferComponent } from './fundtransfer/fundtransfer.component';
import { Flow1Component } from './flow1/flow1.component';
import { LoginProtocolComponent } from './loginprotocols/loginprotocol.component';
import { ApionlyComponent } from './apionly/apionly.component';
import { ApiPlusOidc } from './api&oidc/apiplusoidc.component';
import { Flow2Component } from './flow2/flow2.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    RegisterComponent,
    HomeComponent,
    DashboardComponent,
    CustomComponent,
    BasicLoginComponent,
    MFAWidgetComponent,
    FundTransferComponent,
    Flow1Component,
    LoginProtocolComponent,
    ApionlyComponent,
    ApiPlusOidc,
    Flow2Component,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
