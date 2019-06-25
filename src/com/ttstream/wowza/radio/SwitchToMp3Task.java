package com.ttstream.wowza.radio;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.publish.PlaylistItem;
import com.wowza.wms.stream.publish.Stream;

public class SwitchToMp3Task extends java.util.TimerTask{
	
	WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());

	private String streamName = "";
	
	private String mp3FileName = "";
	
	private int duration = -1;   // 默认是播放整个mp3文件
	
	public void setStreamName(String streamName)
	{
		this.streamName = streamName;
	}
	
	public String getStreamName()
	{
		return this.streamName;
	}
	
	public void setMp3FileName(String mp3FileName)
	{
		this.mp3FileName = mp3FileName;
	}
	public String getMp3FileName()
	{
		return this.mp3FileName;
	}
	
	public void setDuration(int duration)
	{
		this.duration = duration;
	}
	public int getDuration()
	{
		return this.duration;
	}
	
	
	final String CLASS_NAME = "SwitchToMp3Task";
	
	public void run()
	{
	    RadioChannel rc = RadioChannelList.getRadioChannelByStreamName(this.streamName);
	    if (rc == null)
	    {
	    	return;
	    }
		
	    Stream myStream = rc.getChannelStream();
	    
	    /*
	    int poolInterval = myStream.getPollingInterval();
	    logger.debug(this.CLASS_NAME + ": poolInterval = "+poolInterval);
	    long startLiveOnPreviousBuffer = myStream.getStartLiveOnPreviousBufferTime();
	    logger.debug(this.CLASS_NAME + ": StartLiveOnPreviousBufferTime = "+startLiveOnPreviousBuffer);
	    */
	    
	    logger.debug(this.CLASS_NAME + ": begin switch stream "+this.streamName +" to " +this.mp3FileName);
	    myStream.addToPlaylist(1, "mp3:"+this.mp3FileName, 0,this.duration);
	    myStream.play(1);
	    logger.debug(this.CLASS_NAME + ": switch stream "+this.streamName +" to " +this.mp3FileName +" successed!");
	    
	    
	    try{
			Thread.sleep(1000);
		}
		catch (InterruptedException e){
        }
	    
	    
	    boolean remove = true;
	    java.util.List<PlaylistItem> items = myStream.getPlaylist();
	    int size = items.size();
	    
	    if (size > 2)
	    {
	    	for (int i=2;i<size;i++)
	    	{
	    		remove = myStream.removeFromPlaylist(i);
	    		logger.debug(this.CLASS_NAME + ": remove old mp3 file from playlist, i = "+i+" , "+remove);
	    	}
	    }
	    
	}
}
