import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class OIDCService {

    constructor(private http: HttpClient) { }

    getPKCEMetadata(){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `oidc/pkceMetaData`, { headers: head, withCredentials: true })
    }

    buildAuthorizeURL(codeChallenge : string, responseType : string = "code"){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `oidc/buildAuthorizeURL`, { headers: head, withCredentials: true, params: new HttpParams().set("codeChallenge", codeChallenge).set('responseType', responseType) })
    }

    buildImplicitAuthURL(responseType : string){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `oidc/buildImplicitAuthURL`, { headers: head, withCredentials: true, params: new HttpParams().set('responseType', responseType) })
    }

    getTokenSet(authorizationCode : string, codeVerifier : string){
        let head = this.getHeaders();
        return this.http.post<any>(environment.baseUrl + `oidc/tokenSet`, { authorizationCode, codeVerifier }, { headers: head, withCredentials: true })
    }

    getClaims(idToken : string){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `oidc/claims`, { headers: head, withCredentials: true, params: new HttpParams().set("idToken", idToken) })
    }

    getUserInfo(accessToken : string){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `oidc/userInfo`, { headers: head, withCredentials: true, params: new HttpParams().set("accessToken", accessToken) })
    }

    getHeaders(){
        return new HttpHeaders()
            .set('Content-Type', 'application/json')
            .set('Accept', 'application/json')
            .set('Access-Control-Allow-Methods', 'POST')
            .set('Access-Control-Allow-Methods', 'GET')
            .set('Access-Control-Allow-Origin', '*');
    }

    readCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1,c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
        }
        return null;
    }
}
    