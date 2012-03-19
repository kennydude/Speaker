package me.kennydude.speaker;

import android.app.Application;
import android.content.Context;

public class SpeakerApp extends Application {
	static Context instance;
	public static Context getInstance(){
		if(instance == null)
			instance = new Application();
		return instance;
	}
	public SpeakerApp(){
		super();
		instance = this; // @kennydude edit!
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//Pro.getInstance();
	}
}
