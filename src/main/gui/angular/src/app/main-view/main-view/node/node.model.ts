export class Node {
  nodeId: string;
  nodeState: string;

  lastCommittedIdx: number;
  lastOperationIdx: number;
  electionNumber: number;
  votedFor: String;

  disabled: boolean;

  records: any;
  operations: Array<Operation>;
}

export class Operation {
  operationId: string;
  prevIndex: number;
  operationIndex: number;
  electionNumber: number;
  state: OperationState;

  type: OperationType;
  recordId: String;
  recordValue: String;
}

export enum OperationType {
  ADD = "ADD",
  DELETE = "DELETE",
  MODIFY = "MODIFY",
}

export enum OperationState {
  CONFIRMED = "CONFIRMED",
  POTENTIAL = "POTENTIAL"
}
