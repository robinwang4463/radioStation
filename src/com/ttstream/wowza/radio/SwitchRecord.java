package com.ttstream.wowza.radio;

public class SwitchRecord {
	
   private String streamName = null;
	
	private String sourceStreamName = null;
	
	private String startTime = null;
	
	private String endTime = null;
	
	private String mp3FileName = null;
	
	
	
	public void setStreamName(String streamName)
	{
		this.streamName = streamName;
	}
	
	public String getStreamName()
	{
		return this.streamName;
	}
	
	public void setSourceStreamName(String sourceStreamName)
	{
		this.sourceStreamName = sourceStreamName;
	}
	
	public String getSourceStreamName()
	{
		return this.sourceStreamName;
	}
	
	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}
	
	public String getStartTime()
	{
		return this.startTime;
	}
	
	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}
	
	public String getEndTime()
	{
		return this.endTime;
	}
	
	public void setMp3FileName(String mp3FileName)
	{
		this.mp3FileName = mp3FileName;
	}
	
	public String getMp3FileName()
	{
		return this.mp3FileName;
	}

}
