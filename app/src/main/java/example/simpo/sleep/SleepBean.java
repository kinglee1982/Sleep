package example.simpo.sleep;

//对应用做指定处理的实体类，这里用作黑屏和亮屏
public class SleepBean {

    private String motionType;
    private String activeTime;

    public SleepBean(String motionType, String activeTime) {
        this.motionType = motionType;
        this.activeTime = activeTime;
    }

    public String getMotionType() {
        return motionType;
    }

    public void setMotionType(String motionType) {
        this.motionType = motionType;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }


}
