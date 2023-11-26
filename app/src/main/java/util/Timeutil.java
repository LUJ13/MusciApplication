package util;

public class Timeutil {
    public static String millToTimeFormat(int timemill){
        int second=timemill / 1000;
        int minute = second / 60;
        int lastsecond = second % 60;
        String StrSecond ="";
        if(lastsecond<10){
            StrSecond="0"+lastsecond;
        }
        else {
            StrSecond=lastsecond+"";
        }

        String StrMinute ="";
        if(minute<10){
            StrMinute="0"+minute;
        }
        else {
            StrMinute=minute+"";
        }
        return StrMinute+":"+StrSecond;

    }
}
