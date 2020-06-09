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

    public startChaosMonkey(): Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/chaos-monkey/start", null);
    }

    public stopChaosMonkey(): Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/chaos-monkey/stop", null);
    }

    public startFindAndDisableLeader(): Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/find-and-disable-leader/start", null);
    }

    public stopFindAndDisableLeader(): Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/find-and-disable-leader/stop", null);
    }

    public findTestingAlgorithm() : Observable<any>  { 
        return this.http.get(environment.server_url + "/simulation/testing-algorithm");
    }

    public disableNode(nodeId: string) : Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/nodes/" + nodeId + "/disable", null);
    }
    
    public enableNode(nodeId: string) : Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/nodes/" + nodeId + "/enable", null);
    }

    public switchNodeState(nodeId: string) : Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/nodes/" + nodeId + "/switch", null);
    }

    public saveRecord(cmd: RecordCommand) : Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/record/save", cmd);
    }

    public saveRecordForNode(nodeId: string, cmd: RecordCommand): Observable<any>  { 
        return this.http.post(environment.server_url + "/simulation/record/save/node/" + nodeId + "/", cmd);
    }
    public setRestrictionValue(enabled: boolean): Observable<any> {
        return this.http.post(environment.server_url + "/simulation/raft/success-restriction/" + enabled, null);
    }

    public isRestrictionEnabled(): Observable<any> {
        return this.http.get(environment.server_url + "/simulation/raft/success-restriction");
    }
    
    public twoCandidates() {
        return this.http.post(environment.server_url + "/simulation/two-candidates", null);
    }

    public twoLeaders() {
        return this.http.post(environment.server_url + "/simulation/two-leaders", null);
    }

}

class StartSimulationCommand {
    nodesNumber: number;
}

export class SimulationRunningDTO {
    nodesNumber: number;
    running: boolean;
}

export class RecordCommand {
    type: OperationType;
    recordId: string;
    recordValue: String;
}

export  enum OperationType {
    ADD="ADD",
    DELETE="DELETE",
    MODIFY="MODIFY"
}