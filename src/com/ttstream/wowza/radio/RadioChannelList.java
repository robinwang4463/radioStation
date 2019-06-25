package com.ttstream.wowza.radio;

import java.util.ArrayList;

public class RadioChannelList {
	
    public static java.util.List<RadioChannel> radioChannelList =  new ArrayList<RadioChannel>();
	
	// ���һ���������������ͬ���ģ���˵���Ѿ����ˣ����÷����ˣ�ֱ�ӷ���
    // add a object of RadioChannel
    public static void add(RadioChannel radioChannel){
		
		String streamName = radioChannel.getStreamName();
		      
	    for(int i=0;i<radioChannelList.size();i++){
			
			if (radioChannelList.get(i).getStreamName().equals(streamName)){
			     return;
			}
		}
	    synchronized(radioChannelList){
	    	radioChannelList.add(radioChannel);
	    }
	}
	
	//ɾ��һ����
    //delete a RadioChannel object by streamName
	public static void remove(String streamName){
		
		for(int i=0;i<radioChannelList.size();i++){
			
			if (radioChannelList.get(i).getStreamName().equals(streamName)){
				synchronized(radioChannelList){
					radioChannelList.remove(i);
				}
			    break;
			}
		}
	}
	
	public static boolean hasStreamName(String streamName)
	{
		boolean hasStreamName = false;
		for(int i=0;i<radioChannelList.size();i++){
			
			if (radioChannelList.get(i).getStreamName().equals(streamName)){
				hasStreamName = true;
			    break;
			}
		}
		
		return hasStreamName;
	}
	
	public static RadioChannel getRadioChannelByStreamName(String streamName)
	{
        RadioChannel rc = null; 
		for(int i=0;i<radioChannelList.size();i++)
         {
			
			if (radioChannelList.get(i).getStreamName().equals(streamName)){
				rc = radioChannelList.get(i);
			    break;
			}
		}
		return rc;
	}
	
	
	public static RadioChannel getRadioChannelByLiveSource(String sourceStreamName)
	{
        RadioChannel rc = null; 
		for(int i=0;i<radioChannelList.size();i++)
         {
			
			if (radioChannelList.get(i).getSourceStreamName().equals(sourceStreamName)){
				rc = radioChannelList.get(i);
			    break;
			}
		}
		return rc;
	}
}
