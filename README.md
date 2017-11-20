# ImpinjCtrl

Control Impinj Speedway R420 through Octane SDK, using command prompt or socket.io requests

Launch
===================
java -DapiHost=http://APIHOST:PORT -DreaderHost=IMPINJ_IP -jar dist/impinjCtrl.jar

Optional parameter:
-DlogPath=PATH      Specify log path (e.g. Using USB thumb drive)

Commands
===================
When started and connected to reader, use these commands in command prompt:

START   Start singulating
DEBUG   Start singulating in debug mode (with searchMode: dualTarget) to validate read performance
STOP    Stop singulating
STATUS  Return reader status

Socket.io input
===================
{
  "command": "START" || "STOP" || "STATUS",
  "eventId": "event-id", // Custom parameter
  "raceId": "race-id", // Custom parameter
}

Output
===================
Response when receiving command:
{
  "type": "readerstatus",
  "payload": {
    "message": "reader command message",
    "error": true || false,
    "isSingulating": true || false,
    "startTime": timestamp-long,  // returns only on START command
    "endTime": timestamp-long,  // returns only on STOP command
    "logFile": "logfile-path"
  }
}

Tag report - race:
{
  "type": "txdata",
  "payload": {
    "raceId": "race-id",
    "records": [
      {"epc": "epc-string", time: timestamp-long}
    ]
  }
}
Tag report - test:
{
  "type": "txdata_test",
  "payload": {
    "eventId": "event-id",
    "records": [
      {"epc": "epc-string", time: timestamp-long}
    ]
  }
}

Log
===================




Tag report - complete: (Not yet implemented)
{
  "type": "txdata_complete",
  "payload": {
    "raceId": "race-id",
    "startTime": timestamp-long,
    "endTime": timestamp-long
    "records": [
      {"epc": "epc-string", time: timestamp-long}
    ]
  }
}
