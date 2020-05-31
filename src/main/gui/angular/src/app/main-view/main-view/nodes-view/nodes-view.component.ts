import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-nodes-view',
  templateUrl: './nodes-view.component.html',
  styleUrls: ['./nodes-view.component.css']
})
export class NodesViewComponent implements OnInit {

  constructor() { }

  allNodes: Array<Node> = [];

  ngOnInit() {
  }

}
