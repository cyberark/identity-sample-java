import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthorizationMetadataRequest, TokenMetadataRequest } from '../utils';

@Injectable({
  providedIn: 'root'
})

export class AuthorizationService {

    constructor(private http: HttpClient) { }

    getPKCEMetadata(){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `pkceMetaData`, { headers: head, withCredentials: true })
    }

    buildAuthorizeURL(authorizationMetadataRequest: AuthorizationMetadataRequest){
        let head = this.getHeaders();
        return this.http.post<any>(environment.baseUrl + `buildAuthorizeURL`, authorizationMetadataRequest, { headers: head, withCredentials: true })
    }

    getTokenSet(tokenMetadataRequest: TokenMetadataRequest){
        let head = this.getHeaders();
        return this.http.post<any>(environment.baseUrl + `tokenSet`, tokenMetadataRequest, { headers: head, withCredentials: true })
    }

    getTokenRequestPreview(tokenPreviewReq: TokenMetadataRequest){
        let head = this.getHeaders();
        return this.http.post<any>(environment.baseUrl + `tokenRequestPreview`, tokenPreviewReq, { headers: head, withCredentials: true })
    }

    getClaims(token : string){
        let head = this.getHeaders();
        return this.http.get<any>(environment.baseUrl + `claims`, { headers: head, withCredentials: true, params: new HttpParams().set("token", token) })
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
    