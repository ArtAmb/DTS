export class Node {
    nodeId: string;
    nodeState: string;

    lastCommittedIdx: number;
    lastOperationIdx: number;
    electionNumber: number;
    votedFor: String;

    records: any;
}

