import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CustomComponent } from './custom/custom.component';
import { BasicLoginComponent } from './basiclogin/basiclogin.component'
import { MFAWidgetComponent } from './mfawidget/mfawidget.component';
import { FundTransferComponent } from './fundtransfer/fundtransfer.component';

const routes: Routes = [
  { path: '', redirectTo: '/', pathMatch: 'full' },
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'user', component: RegisterComponent },
  { path: 'custom', component: CustomComponent },
  { path: 'basiclogin', component: BasicLoginComponent },
  { path: 'mfawidget', component: MFAWidgetComponent },
  { path: 'fundtransfer', component: FundTransferComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
