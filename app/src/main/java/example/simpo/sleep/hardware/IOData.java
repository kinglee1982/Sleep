package example.simpo.sleep.hardware;

import android.util.Log;

/**
 * 用于解析串口数据的实体类
 * **/
public class IOData {

    //设备状态
    private int status;

    //剩余电量
    private int power;

    //电流
    private double current;

    //电压
    private float voltage;

    //说明
    private String msg;

    public IOData(byte[] bytes) {

        byte status = bytes[3];
        this.status = status;
        switch (status){
            case 0:
                msg = "未使用";
                Log.d("fsafdsafdsa", "未使用");
                break;
            case 1:
                msg = "魔灯充电中";
                Log.d("fsafdsafdsa", "魔灯充电中");
                break;
            case 4:
                msg = "魔灯放电中";
                Log.d("fsafdsafdsa", "魔灯放电中");
                break;
            default:
                msg = "未知状态";
                Log.d("fsafdsafdsa", "未知状态");
        }

        this.power = bytes[5];
        this.current = bytes[8] / 10;
        this.voltage = bytes[9] / 10;
    }

    public int getStatus() {
        return status;
    }

    public double getPower() {
        return power;
    }

    public double getCurrent() {
        return current;
    }

    public float getVoltage() {
        return voltage;
    }

    public String getMsg() {
        return msg;
    }
}
