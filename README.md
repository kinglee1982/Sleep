# Sleep
目前大部分功能只支持6.0及以下的设备，并且大部分需要系统权限
用于切换wifi和GPRS网络，获取电量，充电状态，控制放电，读取串口数据模版，控制系统休眠唤醒，获取当前网络信号强度，获取网络定位

### 1:开启关闭GPRS

这个功能需要是系统应用或者拥有系统签名才可以实现，通过NetSwitchUtils工具可查看，使用了@hide api，需要系统权限或签名。


### 2:连接指定wifi

可以实现关闭开启WLAN，并且连接到指定wifi，可能存在部分wifi难连接的情况，通过WifiConnectUtils工具可查看。


### 3:开启休眠或唤醒，获取IMe

可通过PhoneControl关闭或唤醒手机，IMEI获取也可以查看，可获取网络定位，部分手机可能会存在休眠期间间断性断网情况，但，我并没有时间研究。通过修改系统数据库实现，需要系统权限或签名。


### 4:获取当前网络信号强度

信号强度6.0情况下可分5个强度，通过给TelephonyManager设置PhoneStatListener监听可以获取到，详情可以从NetSignUtils和MainActivityt中查看。


### 4:获取电量，充电状态，控制放电

获取电量，充电状态，需要通过读取串口数据可以获取到，需要机器的厂商提供串口数据的解析方式，控制和关闭放电需要通过ndk写入数据，所以这些功能需要是定制的系统。需要系统权限或签名。
