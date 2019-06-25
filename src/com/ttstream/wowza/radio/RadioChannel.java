package com.ttstream.wowza.radio;

import com.wowza.wms.stream.publish.Stream;

/*
 * This is object, include Stream,streamName and sourceStreamName
 * Stream is a object of com.wowza.wms.stream.publish.Stream, it is a stream published by server side api
 * streamName is the streamName which audience will playback from Wowza Streaming Engine
 * sourceStreamName is live feed come from radio station, it is also a source of stream object, the other source maybe a mp3 file 
 */
public class RadioChannel {

	private Stream channelStream = null;
	
	private String streamName = null;
	
	private String sourceStreamName = null;
	
	public RadioChannel(String streamName,String sourceStreamName,Stream channelStream)
	{
		this.streamName = streamName;
		this.sourceStreamName = sourceStreamName;
		this.channelStream = channelStream;
	}
	
	public void setChannelStream(Stream channelStream)
	{
		this.channelStream = channelStream;
	}
	
	public Stream getChannelStream()
	{
		return this.channelStream;
	}
	
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
}


