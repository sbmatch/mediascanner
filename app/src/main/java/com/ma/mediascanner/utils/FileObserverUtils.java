package com.ma.mediascanner.utils;

import android.os.FileObserver;

public class FileObserverUtils extends FileObserver {
    private String mFolderPath;
    private OnFileChangeListener mListener;

    public FileObserverUtils(String folderPath, OnFileChangeListener listener) {
        super(folderPath, FileObserver.CREATE | FileObserver.DELETE | FileObserver.MODIFY);
        mFolderPath = folderPath;
        mListener = listener;
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event){
            case FileObserver.CREATE:
                mListener.onFileCreated(mFolderPath, path);
                break;
            case FileObserver.DELETE:
                mListener.onFileDeleted(mFolderPath, path);
                break;
            case FileObserver.MODIFY:
                mListener.onFileModify(mFolderPath, path);
                break;
        }
    }

    public interface OnFileChangeListener {
        void onFileCreated(String folderPath, String fileName);
        void onFileDeleted(String folderPath, String fileName);
        void onFileModify(String folderPath, String fileName);
    }
}