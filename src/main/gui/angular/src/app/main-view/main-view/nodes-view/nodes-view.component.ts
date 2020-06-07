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
  selectedNodeId: String = null;
  selectedOperationType: OperationType = OperationType.ADD;
  recordKey: String;
  recordValue: String;

  saveRecord() {
    if(this.recordKey == null) {
      this.notifyService.showMessage("Klucz rekordu jest WYMAGANY!");
      return;
    }
    
    this.restService
      .saveRecord({
        type: this.selectedOperationType,
        recordId: this.recordKey.toString(),
        recordValue: this.recordValue,
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

  saveRecordForNode() {
    if(this.recordKey == null) {
      this.notifyService.showMessage("Klucz rekordu jest WYMAGANY!");
      return;
    }

    if(this.selectedNodeId == null) {
      this.notifyService.showMessage("Prosze wybrac node do wykonania operacji");
      return;
    }

    this.restService
    .saveRecordForNode(
      this.selectedNodeId.toString(), {
      type: this.selectedOperationType,
      recordId: this.recordKey.toString(),
      recordValue: this.recordValue,
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

  saveOperationType(type: OperationType) {
    this.selectedOperationType = type;

    if(type == OperationType.DELETE) {
      this.recordValue = null;
    }
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
