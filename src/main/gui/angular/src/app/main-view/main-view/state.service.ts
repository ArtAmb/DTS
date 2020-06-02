import { Injectable } from "@angular/core";
import { Node } from "./node/node.model";

@Injectable({
  providedIn: "root",
})
export class StateService {
  constructor() {}

  simulation: boolean;
  nodes: NodeState[];
  nodeCounter: number = 0;

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
  }
}


class NodeState {
  nodeIdx: number;
  nodeId: String;
  active: boolean;
}