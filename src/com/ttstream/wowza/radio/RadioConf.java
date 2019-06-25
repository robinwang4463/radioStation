package com.ttstream.wowza.radio;

/*
 * This is just a setting
 */
public class RadioConf {

    static public String appName = "live";
	
    static public String radioChannelFileName = "";
	
    //because the audience maybe playback the stream by HLS protocol, the playback client may have some buffer
    //so,we need to switch the source before the start time 
    static public int advancedBufferTime = 20;
}
