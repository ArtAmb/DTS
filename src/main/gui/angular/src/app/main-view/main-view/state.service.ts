import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class StateService {
  constructor() {}

  simulation: boolean;

  public isSimulationRunning() {
    return this.simulation;
  }

  public setSimulation(simulation: boolean) {
    this.simulation = simulation;
  }
}
