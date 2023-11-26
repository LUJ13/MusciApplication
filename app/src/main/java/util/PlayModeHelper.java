package util;

public class PlayModeHelper {
    public static final int PLAY_MODE_ORDER =0;
    public static  final int PLAY_MODE_CIRCLE =1;
    public static  final int PLAY_MODE_RANDOM =2;
    public static int changePlayMode(int curMode){
        switch (curMode){
            case PLAY_MODE_ORDER:
                return PLAY_MODE_CIRCLE;
            case PLAY_MODE_CIRCLE:
                return PLAY_MODE_RANDOM;
            case PLAY_MODE_RANDOM:
                return PLAY_MODE_ORDER;
            default:
                return PLAY_MODE_ORDER;
        }
    }
    public static  String strPlayMode(int mode){
        switch (mode){
            case PLAY_MODE_ORDER:
                return "Order";
            case PLAY_MODE_CIRCLE:
                return "Circle";
            case PLAY_MODE_RANDOM:
                return "Random";
            default:
                return "Order";
        }
    }

}
