import { Component, OnInit, Input } from "@angular/core";
import { Node } from "./node.model";
import { RestService } from "../rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { StateService } from "../state.service";

@Component({
  selector: "app-node",
  templateUrl: "./node.component.html",
  styleUrls: ["./node.component.css"],
})
export class NodeComponent implements OnInit {
  @Input() node: Node;
  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateService: StateService
  ) {}

  ngOnInit() {}

  disableNode() {
    this.restService.disableNode(this.node.nodeId).subscribe(
      (res) => {},
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  enableNode() {
    this.restService.enableNode(this.node.nodeId).subscribe(
      (res) => {},
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  isRecordsViewEnable() {
    return this.stateService.showRecordsEnable;
  }

  isOperationsViewEnable() {
    return this.stateService.showOperationsEnable;
  }
}
