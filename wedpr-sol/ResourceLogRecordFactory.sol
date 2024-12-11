pragma solidity >=0.6.10 <0.8.20;

import "./ResourceSequencer.sol";
import "./ResourceLogRecord.sol";

contract ResourceLogRecordFactory{

    ResourceSequencer private sequencer;
    event addRecordEvent(address recordAddress, int256 recordIndex, int256 contractVersion);

    constructor(address sequencerAddress)public{
        sequencer = ResourceSequencer(sequencerAddress);
    }

    function addRecord(string memory recordContent, int256 contractVersion) public{
        ResourceLogRecord record = new ResourceLogRecord(recordContent);
        int256 index = sequencer.allocateIndex();
        record.setIndex(index);
        emit addRecordEvent(address(record), index, contractVersion);
    }

    function getLatestIndex() public view returns(int256){
        return sequencer.getLatestIndex();
    }
}