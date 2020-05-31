import { Component, OnInit } from "@angular/core";
import { RestService } from "../rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { interval } from "rxjs";
import { StateService } from "../state.service";

@Component({
  selector: "app-nodes-view",
  templateUrl: "./nodes-view.component.html",
  styleUrls: ["./nodes-view.component.css"],
})
export class NodesViewComponent implements OnInit {
  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateService: StateService
  ) {}

  allNodes: Array<Node> = [];

  ngOnInit() {
    interval(100).subscribe((tmp) => {
      if (this.stateService.isSimulationRunning()) {
        this.refreshNodes();
      }
    });
  }

  private refreshNodes() {
    this.restService.getAllNodes().subscribe(
      (res) => {
        this.allNodes = res;
      },
      (err) => {
        this.notifyService.failure(err);
        this.stateService.setSimulation(false);
      }
    );
  }
}
