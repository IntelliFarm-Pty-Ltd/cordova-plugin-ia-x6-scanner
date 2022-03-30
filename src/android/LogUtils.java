package com.intelliacc.alps2Scanner;

import android.util.Log;

/**
 * @author HuangLei 1252065297@qq.com
 * @CreateDate 2019/1/17 11:36
 * @UpdateUser HuangLei
 * @UpdateDate 2019/05/16 10:17
 */
public class LogUtils {
    private static boolean DEBUG = true;
    
    public static void setDebug(boolean debug) {
		DEBUG = debug;
	}

    public static void v(String TAG, String content) {
        if (DEBUG) {
            Log.v(TAG, content);
        }
    }

    public static void d(String TAG, String content) {
        if (DEBUG) {
            Log.d(TAG, content);
        }
    }

    public static void i(String TAG, String content) {
        if (DEBUG) {
            Log.i(TAG, content);
        }
    }

    public static void w(String TAG, String content) {
        if (DEBUG) {
            Log.w(TAG, content);
        }
    }

    public static void e(String className, String content) {
        if (DEBUG) {
            Log.e("Huang", className + ", " + content);
        }
    }
}
