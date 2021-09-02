import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HttpClientXsrfModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { LoginComponent } from './login/login.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { CustomComponent } from './custom/custom.component';
import { BasicLoginComponent } from './basiclogin/basiclogin.component'
import { MFAWidgetComponent } from './mfawidget/mfawidget.component';
import { FundTransferComponent } from './fundtransfer/fundtransfer.component';
import { Flow1Component } from './flow1/flow1.component';
import { LoginProtocolComponent } from './loginprotocols/loginprotocol.component';
import { ApionlyComponent } from './apionly/apionly.component';
import { Metadata } from './metadata/metadata.component';
import { Flow2Component } from './flow2/flow2.component';
import { OidcFlowComponent } from './oidcflow/oidcflow.component';
import { RedirectComponent } from './redirect/redirect.component';
import { OAuthFlowComponent } from './oauthflow/oauthflow.component';
import { HttpXsrfInterceptor } from './HttpXsrfInterceptor';
import { M2MComponent } from './m2m/m2m.component';
import { ErrorComponent } from './components/error/error.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    ErrorComponent,
    RegisterComponent,
    HomeComponent,
    CustomComponent,
    BasicLoginComponent,
    MFAWidgetComponent,
    FundTransferComponent,
    Flow1Component,
    LoginProtocolComponent,
    ApionlyComponent,
    Metadata,
    Flow2Component,
    OidcFlowComponent,
    RedirectComponent,
    OAuthFlowComponent,
    M2MComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({headerName: 'X-XSRF-TOKEN', cookieName: 'XSRF-TOKEN'})
  ],
  providers: [{provide: HTTP_INTERCEPTORS, useClass: HttpXsrfInterceptor, multi: true}],
  bootstrap: [AppComponent]
})
export class AppModule { }
