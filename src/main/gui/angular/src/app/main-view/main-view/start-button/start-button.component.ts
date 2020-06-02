import { Component, OnInit } from "@angular/core";
import { RestService } from "../rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { StateService } from "../state.service";

@Component({
  selector: "app-start-button",
  templateUrl: "./start-button.component.html",
  styleUrls: ["./start-button.component.css"],
})
export class StartButtonComponent implements OnInit {
  nodesNumber: number = 5;
  simulationRunning = false;
  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateServcie: StateService
  ) {}

  ngOnInit() {
    this.restService.isSimulationRunning().subscribe(
      (res) => {
        this.stateServcie.setSimulation(res.running);
        this.simulationRunning = res.running;
        this.nodesNumber = res.nodesNumber;

        if (!res.running) {
          this.nodesNumber = 5;
        }

        if (res.running) {
          this.fillNodeStates();
        }
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  startSimulation() {
    this.restService
      .startSimulation({
        nodesNumber: this.nodesNumber,
      })
      .subscribe(
        (res) => {
          this.simulationRunning = true;
          this.stateServcie.setSimulation(this.simulationRunning);

          this.fillNodeStates();
        },
        (err) => {
          this.notifyService.failure(err);
        }
      );
  }

  fillNodeStates() {
    this.restService.getAllNodes().subscribe(
      (res) => {
        this.stateServcie.setNodes(res);
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  stopSimulation() {
    this.restService.stopSimulation().subscribe(
      (res) => {
        this.simulationRunning = false;
        this.stateServcie.setSimulation(this.simulationRunning);
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }
}
