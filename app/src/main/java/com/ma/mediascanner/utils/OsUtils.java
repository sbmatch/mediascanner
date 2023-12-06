package com.ma.mediascanner.utils;

public class OsUtils {
    public static boolean isMIUI(){
        try {
            return (boolean) ReflectUtil.callStaticObjectMethod(Class.forName("android.telephony.TelephonyBaseUtilsStub"),"isMiuiRom");
        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }
}
