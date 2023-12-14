package com.ma.mediascanner.utils;

import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MultiJarClassLoader extends ClassLoader {
    private static MultiJarClassLoader multiJarClassLoader;
    List<DexClassLoader> dexClassLoaders = new ArrayList<>();
    private MultiJarClassLoader(ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    public synchronized static MultiJarClassLoader getInstance(){
        if (multiJarClassLoader == null){
            multiJarClassLoader = new MultiJarClassLoader(ClassLoader.getSystemClassLoader());
            multiJarClassLoader.init();
        }
        return multiJarClassLoader;
    }

    public void init(){
        getInstance().addJar("/system/system_ext/framework/miui-framework.jar");
    }

    public void addJar(String jarPath) {
        DexClassLoader dexClassLoader = new DexClassLoader(
                jarPath,
                null,
                null, // 额外的库路径，可以为 null
                getParent() // 父类加载器
        );
        dexClassLoaders.add(dexClassLoader);
    }
}