package com.ma.mediascanner.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class AppOpsUtils {

    public static boolean checkOps(Context c, String a) {

        // 使用反射appops管理器检查权限
        try {
            long starTime = System.currentTimeMillis();
            Object obj = c.getSystemService("appops");
            Class<?> clazz = Class.forName("android.app.AppOpsManager");
            Method checkop = clazz.getDeclaredMethod("checkOp", String.class, Integer.TYPE, String.class);
            checkop.setAccessible(true);
            int uid = android.os.Process.myUid();
            String pkg = c.getPackageName();
            int op = ((Integer) checkop.invoke(obj, a, Integer.valueOf(uid), pkg)).intValue();
            switch (op) {
                case 0:
                    long endTime = System.currentTimeMillis();
                    Log.i("AppOps", a + " 已授权"+" -------->  耗时: "+(endTime - starTime) + " 毫秒");
                    return true;
                case 1:
                    Log.e("AppOps", a + " 权限被设置为忽略");
                    return false;
                case 2:
                    Log.e("AppOps", a + " 权限被设置为拒绝 ");
                    return false;

            }
        } catch (Exception ignored) {}
        return false;
    }

}
