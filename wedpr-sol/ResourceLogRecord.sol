pragma solidity >=0.6.10 <0.8.20;

contract ResourceLogRecord{
    // the record index
    int256 private index;
    // the resource data(json string)
    string private resourceRecord;

     constructor(string memory _resourceRecord) public{
        resourceRecord = _resourceRecord;
     }

    function setIndex(int256 _index) public{
        index = _index;
    }

    function getRecord() public view returns(int256, string memory){
        return (index, resourceRecord);
    }
}