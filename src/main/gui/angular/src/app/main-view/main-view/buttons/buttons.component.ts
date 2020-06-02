import { Component, OnInit } from "@angular/core";
import { RestService } from "../rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { StateService } from "../state.service";

@Component({
  selector: "app-buttons",
  templateUrl: "./buttons.component.html",
  styleUrls: ["./buttons.component.css"],
})
export class ButtonsComponent implements OnInit {
  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateServcie: StateService
  ) {}

  testingAlgorithm: TestingAlgorithm = TestingAlgorithm.NONE;
  ngOnInit() {
    this.setTestingAlgorithm();
  }

  isButtonEnabled() {
    return this.stateServcie.isSimulationRunning() && this.testingAlgorithm == TestingAlgorithm.NONE;
  }

  startChaosMonkey() {
    this.restService.startChaosMonkey().subscribe(
      (res) => {
        this.testingAlgorithm = TestingAlgorithm.CHAOS_MONKEY;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  stopChaosMonkey() {
    this.restService.stopChaosMonkey().subscribe(
      (res) => {
        this.testingAlgorithm = TestingAlgorithm.NONE;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  startFindAndDisableLeader() {
    this.restService.startFindAndDisableLeader().subscribe(
      (res) => {
        this.testingAlgorithm = TestingAlgorithm.FIND_AND_DISABLE_LEADER;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  stopFindAndDisableLeader() {
    this.restService.stopFindAndDisableLeader().subscribe(
      (res) => {
        this.testingAlgorithm = TestingAlgorithm.NONE;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  stopTesting() {
    switch (this.testingAlgorithm) {
      case TestingAlgorithm.FIND_AND_DISABLE_LEADER:
        this.stopFindAndDisableLeader();
        break;

      case TestingAlgorithm.CHAOS_MONKEY:
        this.stopChaosMonkey();
        break;
    }
  }

  setTestingAlgorithm() {
    this.restService.findTestingAlgorithm().subscribe(
      (res) => {
        this.testingAlgorithm = res;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }
}

enum TestingAlgorithm {
  NONE = "NONE",
  CHAOS_MONKEY = "CHAOS_MONKEY",
  FIND_AND_DISABLE_LEADER = "FIND_AND_DISABLE_LEADER",
}
