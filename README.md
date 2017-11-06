# ImpinjCtrl


使用Impinj Octane SDK，控制 Impinj Speedway R420 的開始與結束讀卡。

執行
===================
java -DapiHost=http://APIHOST:PORT -DreaderHost=IMPINJ_IP -jar dist/impinjCtrl.jar

其他選填選項：
-DlogPath=PATH 指定log file路徑

指令
===================
連上Reader之後，直接輸入下列指令：
START   啟動讀卡機
DEBUG   啟用讀卡機Debug模式  (dualTarget)
STOP    停止讀卡機
STATUS  回傳Reader狀態
