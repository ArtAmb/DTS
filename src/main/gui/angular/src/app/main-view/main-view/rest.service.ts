import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import { Node } from "./node/node.model";

@Injectable({
    providedIn: "root"
})
export class RestService {
    constructor(private http: HttpClient) {
    }

    public startSimulation(command: StartSimulationCommand): Observable<any> {
        return this.http.post(environment.server_url + "/simulation/start", command);
    }

    public stopSimulation(): Observable<any>  {
        return this.http.post(environment.server_url + "/simulation/stop", null);
    }

    public getAllNodes(): Observable<any>  { // Array<Nodes>
        return this.http.get(environment.server_url + "/simulation/nodes");//.map(res => res.json());
    }

    public isSimulationRunning(): Observable<any>  { // SimulationRunningDTO
        return this.http.get(environment.server_url + "/simulation/is-running");
    }

}

class StartSimulationCommand {
    nodesNumber: number;
}

export class SimulationRunningDTO {
    nodesNumber: number;
    running: boolean;
}