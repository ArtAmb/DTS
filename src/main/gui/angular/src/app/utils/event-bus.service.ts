import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: "root"
})
export class EventBusService {
  private _subscriber = new BehaviorSubject(null);

  constructor() {}

  public getSubscriber() {
    return this._subscriber;
  }
}
