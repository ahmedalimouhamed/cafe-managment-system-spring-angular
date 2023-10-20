import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  url = environment.apiUrl;

  constructor(private httpClient: HttpClient) { }

  signup(data:any) {
    return this.httpClient.post(`${this.url}/users/signup`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  forgotPassword(data:any) {
    return this.httpClient.post(`${this.url}/users/forgotPassword`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }


  login(data:any) {
    return this.httpClient.post(`${this.url}/users/login`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  checkToken(){
    return this.httpClient.get(`${this.url}/users/checkToken`);
  }

  changePassword(data:any){
    return this.httpClient.post(`${this.url}/users/changePassword/`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getUsers(){
    return this.httpClient.get(`${this.url}/users/get`);
  }

  update(data:any){
    return this.httpClient.post(`${this.url}/users/update`,data,{
      headers:new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

}
