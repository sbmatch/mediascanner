package com.ma.mediascanner.utils;

import org.apache.tika.Tika;

import java.io.File;
import java.net.URL;

public class TikaUtils {
    private static Tika tika;
    private TikaUtils(){

    }
    public synchronized static Tika getInstance(){
        if (tika == null) tika = new Tika();
        return tika;
    }

    public static String detect(String filePath){
        try {
            return getInstance().detect(new File(filePath));
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }
}
