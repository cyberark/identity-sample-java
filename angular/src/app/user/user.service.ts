import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { User } from '../user/user';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class UserService {

  constructor(private http: HttpClient) { }
  private baseUrl ="https://apidemo.cyberark.app:8080/";
  private userUrl = "https://apidemo.cyberark.app:8080/user/";
  private userOpsUrl = "https://apidemo.cyberark.app:8080/userops/";
  private configUrl = "https://apidemo.cyberark.app:8080/config/";
  getById(id: string, social: boolean) {
    let head = new HttpHeaders().set('Content-Type', 'application/json');
    let url = this.userOpsUrl + `${id}`;
    if (social) {
      url = this.userOpsUrl + `info/${id}`;
    }
    return this.http.get<any>(url, { headers: head, withCredentials: true });
  }

  register(user: User, clientIP: string, isMfa: boolean) {
    let head = new HttpHeaders().set('Content-Type', 'application/json').set('CLIENT_IP', clientIP);
    return this.http.post<any>(this.userUrl + `register`, {user: user, isMfa: isMfa}, { headers: head, withCredentials: true });
  }

  getClientIP(){
    return this.http.get<any>("https://api.ipify.org/?format=json");
  }

  update(user: {}, id: string) {
    let head = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put<any>(this.userOpsUrl + `${id}`, user, { headers: head, withCredentials: true });
  }

  getClientCustomData() {
    let head = new HttpHeaders().set('Content-Type', 'application/json');
    let url = this.configUrl + `getclientconfig`;
    return this.http.get<any>(url, { headers: head, withCredentials: true });
  }

  getCustomData() {
    let head = new HttpHeaders().set('Content-Type', 'application/json');
    let url = this.configUrl + `getconfig`;
    return this.http.get<any>(url, { headers: head, withCredentials: true });
  }

  setCustomData(custom: any) {
    let head = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put<any>(this.configUrl + `updateconfig`, custom, { headers: head, withCredentials: true });
  }

  refreshActuators() {
    //this.http.post<any>(this.userUrl + `refresh`, { withCredentials: true }).subscribe();
    this.http.post<any>(this.baseUrl + `actuator/refresh`, { withCredentials: true }).subscribe();
  }
}
