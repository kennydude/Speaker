package me.kennydude.speaker;

import android.media.AudioManager;

public class FroyoDucking {
	public static void getDucking(AudioManager manager){
		manager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
	}
	public static void looseDucking(AudioManager manager){
		manager.abandonAudioFocus(null);
	}
}
