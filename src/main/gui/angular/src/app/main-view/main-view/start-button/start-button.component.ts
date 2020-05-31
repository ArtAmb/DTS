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

        if(!res.running) {
          this.nodesNumber = 5;
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
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }
}
