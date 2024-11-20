pragma solidity >=0.6.10 <0.8.20;

contract ResourceSequencer{
    int256 private latestIndex = 0;

    function allocateIndex() public returns(int256){
        latestIndex += 1;
        return latestIndex;
   }

    function getLatestIndex() public view returns (int256){
        return latestIndex;
    }
}