import { Component, OnInit } from "@angular/core";
import { RestService, OperationType } from "../rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { interval } from "rxjs";
import { StateService } from "../state.service";
import { Node } from "../node/node.model";

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

  recordKey: String;
  recordValue: String;

  saveRecord() {
    this.restService
      .saveRecord({
        type: OperationType.ADD,
        recordId: this.recordKey.toString(),
        recordValue: this.recordValue.toString(),
      })
      .subscribe(
        (res) => {
          this.notifyService.success("Rekord dodany");
          this.recordKey = null;
          this.recordValue = null;
        },
        (err) => {
          this.notifyService.failure(err);
        }
      );
  }

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

  getNodesInfo() {
    return this.stateService.nodes;
  }

  switchNodeState(nodeId: string) {
    this.restService.switchNodeState(nodeId).subscribe(
      (res) => {},
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }


  isHorizontal(): boolean {
    return this.stateService.nodeFlow == 'HORIZONTAL';
  }

  isVertical(): boolean {
    return this.stateService.nodeFlow == 'VERTICAL';
  }
}
