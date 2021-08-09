import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { interval } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class BasicLoginService {

  constructor(private http: HttpClient) { }

  basicLoginUser(Username:string, Password:string){

    let head = new HttpHeaders()
    .set('Content-Type', 'application/json')
    .set('Accept', 'application/json')
    .set('Access-Control-Allow-Methods', 'POST')
    .set('Access-Control-Allow-Origin', '*');
    return this.http.post<any>(environment.baseUrl + `BasicLogin`, { Username, Password }, { headers: head, withCredentials: true })
      .pipe(map(result => {
        return result;
      }));
  }
  logout(authToken : string) {
    let head = new HttpHeaders().set('Content-Type', 'application/json').set("AUTH",authToken);;
    return this.http.post<any>(environment.baseUrl + `UserOps/LogOut`, {}, { headers: head })
      .pipe(map(user => {
        return user;
      }));
  }

  completeLoginUser(sessionUuid : string, authorizationCode : string, clientId : string, codeVerifier : string){
    let head = new HttpHeaders()
    .set('Content-Type', 'application/json')
    .set('Accept', 'application/json')
    .set('Access-Control-Allow-Methods', 'POST')
    .set('Access-Control-Allow-Origin', '*');
    return this.http.post<any>(environment.baseUrl + `CompleteLogin`, { sessionUuid, authorizationCode, clientId, codeVerifier }, { headers: head, withCredentials: true })
      .pipe(map(result => {
        return result;
      }));
  }

  authorize(authentication_token : string, clientId : string, codeChallenge : string){

    let head = new HttpHeaders()
    .set('Content-Type', 'application/json')
    .set('Accept', 'application/json')
    .set('Authorization', 'Bearer ' + authentication_token);

    let url = `https://${environment.apiFqdn}/oauth2/authorize/${environment.oauthAppId}?scope=${environment.oauthScope}&client_id=${clientId}&code_challenge=${codeChallenge}&code_challenge_method=S256&response_type=code&redirect_uri=https://apidemo.cyberark.app:8080/RedirectResource`;

    return this.http.get<any>(url, { headers: head, withCredentials: true});
  }
}
