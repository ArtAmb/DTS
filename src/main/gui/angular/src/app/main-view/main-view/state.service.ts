import { Injectable } from "@angular/core";
import { Node } from "./node/node.model";

@Injectable({
  providedIn: "root",
})
export class StateService {
  
  constructor() {}

  simulation: boolean;
  nodes: NodeState[];

  nodesByIdMAP = {}

  nodeCounter: number = 0;
  showRecordsEnable: boolean = false;
  showOperationsEnable: boolean = false;
  nodeFlow: string = 'VERTICAL';

  public isSimulationRunning() {
    return this.simulation;
  }

  public setSimulation(simulation: boolean) {
    this.simulation = simulation;

    if(!this.simulation) {
      this.nodes = null;
    }
  }

  public setNodes(nodes: Node[]) {
    this.nodeCounter = 0;
    this.nodes = nodes.map( tmp => {
      return {
        nodeId: tmp.nodeId,
        active: !tmp.disabled,
        nodeIdx: ++this.nodeCounter
      } 
    });

    this.nodes.forEach(node => {
      this.nodesByIdMAP[node.nodeId.toString()] = node;
    })
  }

  public setShowRecords(value: boolean) {
    this.showRecordsEnable = value;
  }

  public setShowOperations(value: boolean) {
    this.showOperationsEnable = value;
  }

  public setNodesFlow(value: string) {
    this.nodeFlow = value;
  }

  public getNodeIdx(nodeUUID: string): number {
    // console.log("getNodeIdx");
    // console.log(nodeUUID);
    // console.log(this.nodesByIdMAP);
    return this.nodesByIdMAP[nodeUUID].nodeIdx;
  }
}


class NodeState {
  nodeIdx: number;
  nodeId: String;
  active: boolean;
}