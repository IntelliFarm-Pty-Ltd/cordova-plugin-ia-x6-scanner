package com.intelliacc.alps2Scanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class Util {


	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;

	public static void initSoundPool(Context context){
		Util.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
    suondMap.put(1, sp.load(context,  context
        .getResources()
        .getIdentifier("x6_beep_ok", "raw", context.getPackageName())
      , 1));
	}

	public static  void play(int sound, int number){
		AudioManager am = (AudioManager)Util.context.getSystemService(Util.context.AUDIO_SERVICE);
	    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

	        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        float volumnRatio = audioCurrentVolume/audioMaxVolume;
	        sp.play(
	        		suondMap.get(sound),
	        		audioCurrentVolume,
	        		audioCurrentVolume,
	                1,
	                number,
	                1);
	    }


	public static void logE(String tag , String msg){
		Log.e(tag, msg) ;
	}


	static int count = 0 ;
	public static void writeLog(Object obj){
		File file = new File("/mnt/sdcard/count.txt") ;
		if(!file.exists()){
			count = 0 ;
		}
		count++ ;
		try {
			FileWriter fw = new FileWriter(file) ;
			fw.write("count = " + count) ;
			fw.flush() ;
			fw.close() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
