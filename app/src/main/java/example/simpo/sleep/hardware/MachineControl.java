package example.simpo.sleep.hardware;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 利用读取串口数据和传值用于控制开启和关闭机器放电和获取机器电量的工具类
 * 使用此功能必须是系统应用或者使用类系统签名
 * 并且需要配置
 * <uses-permission android:name="android.permission.WRITE_APN_SETTINGS" />
 * android:sharedUserId="android.uid.system"
 * 结合lib中的so包完成ndk的串口数据读取
 *
 * 目前这个demo无法展示该功能，因为没有系统签名
 **/

public class MachineControl {

    private static final String TAG = "PortablePowerBankCtrl";
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private static final int UARTDATASHOW = 1;
    private SerialPort mSerialPort = null;
    //读串口数据
    private ReadThread mReadThread;
    //串口
    private String uartPort = "/dev/ttyMT2";
    //串口波特率
    private int uartBaudrate = 115200;
    //充电宝使能控制
    private static final String PORTABLEPOWERBANK_EN_NODE = "/sys/devices/portable_power_bank/portable_power_bank_en";

    private static MachineControl machineControl;
    private OnElectricLisenter electricLisenter;
    private List<OnElectricLisenter> electricLisenters = new ArrayList<>();
    private static int status = 0;   //当前的状态,0就绪，1放电
    private double currentPower = 0;  //当前的电量

    private MachineControl() {

    }

    public static MachineControl getInstance() {
        if (machineControl == null) {
            synchronized (MachineControl.class) {
                if (machineControl == null) {
                    machineControl = new MachineControl();
                }
            }
        }
        return machineControl;
    }

    //开始运行获取串口数据线程,最好在Application中调用
    public void start() {
        if (mSerialPort == null) {
            try {
                mSerialPort = new SerialPort(new File(uartPort), uartBaudrate, 0);
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();
                mReadThread = new ReadThread();
                mReadThread.start();
            } catch (IOException e) {
                Log.d(TAG, "ReadThread start Error:" + e.getMessage() + "!!!!");
                e.printStackTrace();
            }
        }
    }

    //开始放电
    public void open() {
        if (status == 0) {
            portablePowerBankEn(true);
            byte[] testUart = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, '\n'};
            try {
                mOutputStream.write(testUart);
                status = 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //关闭放电
    public void close() {
        if (status == 1) {
            try {
                portablePowerBankEn(false);
                status = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 读串口数据
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                int size = 0;
                try {
                    byte[] buffer = new byte[128];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);

                    if (size > 0) {
                        String dataStr = "";
                        int data;
                        for (int i = 0; i < size; i++) {
                            Log.d(TAG, "buffer[" + i + "]:" + buffer[i]);
                            data = buffer[i] & 0xff;
                            dataStr += String.valueOf(data) + " ";
                        }

                        Log.d(TAG, "ReadThread uart data:" + dataStr + "!!!!");
                        Message message = new Message();
                        message.what = UARTDATASHOW;
                        message.obj = dataStr;
                        final IOData ioData = new IOData(buffer);
                        currentPower = ioData.getPower();
                        Log.d(TAG, "ReadThread uart data:" + ioData.getPower() + "!!!!");
                        if (electricLisenters != null && electricLisenters.size() > 0) {
                            for (int i = 0; i < electricLisenters.size(); i++) {
                                try {
                                    electricLisenters.get(i).electric(ioData.getStatus(), ioData.getPower());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "ReadThread uart Error:" + e.getMessage() + "!!!!");
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //获取当前设备的电量
    public double getCurrentPower() {
        return currentPower;
    }

    public void setOnElectricLisenter(OnElectricLisenter electricLisenter) {
        electricLisenters.add(electricLisenter);
    }

    public interface OnElectricLisenter {
        void electric(int status, Double power);
    }

    /**
     * 以String的格式写入节点，
     */
    private void writeNode(String data, String printNode) {
        BufferedWriter bufWriter = null;
        try {
            bufWriter = new BufferedWriter(new FileWriter(printNode));
            bufWriter.write(data);
            bufWriter.flush();
            Log.e(TAG, "no crash");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "can't write the " + printNode);
            Log.e(TAG, " ***ERROR*** Here is what I know: " + e.getMessage());
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                Log.i(TAG, "bufWriter is null ");
            }
        } finally {
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "bufWriter is null ");
            }
        }
    }

    /**
     * 读取节点数据，返回String
     */
    private String readNode(String printNode) {
        String prop = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(printNode));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "can't read the " + printNode);
            Log.e(TAG, " ***ERROR*** Here is what I know: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.w(TAG, "readFile from " + printNode + "data -> prop = " + prop);


        return prop;
    }

    private void portablePowerBankEn(boolean en) {
        if (en) {
            writeNode("1", PORTABLEPOWERBANK_EN_NODE);
            if ("1".equals(readNode(PORTABLEPOWERBANK_EN_NODE))) {
                Log.d(TAG, "The PORTABLEPOWERBANK has been successfully opened!!");
            } else {
                Log.d(TAG, "Fail to open the PORTABLEPOWERBANK");
            }
        } else {
            writeNode("0", PORTABLEPOWERBANK_EN_NODE);
            if ("0".equals(readNode(PORTABLEPOWERBANK_EN_NODE))) {
                Log.d(TAG, "The PORTABLEPOWERBANK has been successfully closed!!");
            } else {
                Log.d(TAG, "Fail to close the device");
            }
        }
    }

    //最好在Application中调用
    public void onDestroy() {
        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
        portablePowerBankEn(false);
    }

}
