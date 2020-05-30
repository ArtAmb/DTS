import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";

@Injectable({
    providedIn: "root"
})
export class RestService {
    constructor(private http: HttpClient) {
    }

    public findUserProfil(): Observable<any> {
        return this.http.get(environment.server_url + "/user/profil-data");
    }
    public updateUserProfil(o : any): Observable<any>  {
        return this.http.post(environment.server_url + "/user/profil-update", o);
    }

}