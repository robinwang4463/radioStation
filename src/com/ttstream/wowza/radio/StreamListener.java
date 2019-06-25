package com.ttstream.wowza.radio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.client.IClient;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStreamActionNotify3;
import com.wowza.wms.stream.publish.PlaylistItem;
import com.wowza.wms.stream.publish.Stream;



public class StreamListener implements IMediaStreamActionNotify3{
	
	
	private WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());
	final String CLASS_NAME = "StreamListener";
		
	public StreamListener(){
	}

	public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket)
	{
		
	}

	public void onPauseRaw(IMediaStream stream, boolean isPause, double location)
	{
		
	}

	public void onPause(IMediaStream stream, boolean isPause, double location)
	{
		
	}

	public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset)
	{
		
	}
	public void onSeek(IMediaStream stream, double location)
	{
		
	}

	public void onStop(IMediaStream stream)
	{
		
	}

	public void onCodecInfoAudio(IMediaStream stream,MediaCodecInfoAudio codecInfoAudio) {
	
	}

	public void onCodecInfoVideo(IMediaStream stream,MediaCodecInfoVideo codecInfoVideo) {
		
	}
	
	public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
	    logger.debug(this.CLASS_NAME+": onPublish[" + stream.getContextStr() + "]: streamName: " + streamName + " current time : "+System.currentTimeMillis()/1000);
		
	    IClient client = stream.getClient();
	    if (client == null){
	    	return;
		}
	    
	    client.getAppInstance().getVHost().getHandlerThreadPool().execute(new Runnable()
		{

			@Override
			public void run()
			{
				swapToLive(stream, streamName);
			}
        });
	    
	}
	
	
	private void swapToLive(IMediaStream stream, String streamName){
		
		boolean ready = stream.isPublishStreamReady(true, false);
	    while (!ready)
	    {
	    	try{
				Thread.sleep(100);
			}
			catch (InterruptedException e){
            } 
	    	ready = stream.isPublishStreamReady(true, false);
	    }
	    
	    Date now = new Date();
	    
	    if (!isCurrentInSchedule(streamName,now))
	    {
	    	RadioChannel rc = RadioChannelList.getRadioChannelByLiveSource(streamName); 
	    	if (rc != null)
	    	{
	    		Stream myStream = rc.getChannelStream();
	    		PlaylistItem item = myStream.getCurrentItem();
	    		if (item == null)
	    		{
	    			myStream.play(0);
	    			logger.debug(this.CLASS_NAME+": Source Live Stream "+streamName+" is online, can not get current item, we need to playback this source stream");
	    		}else{
	    			int currentIndex = item.getIndex();
	    			if (currentIndex !=0){
	    				myStream.play(0);
	    				logger.debug(this.CLASS_NAME+": Source Live Stream "+streamName+" is online, the current item is not zero, we need to playback this source stream");
	    			}else{
	    				logger.debug(this.CLASS_NAME+": Source Live Stream "+streamName+" is online, the current item is this source stream, Do nothing");
	    			}
	    				
	    		}
	    	}	
	    }
	
	}
	
	
	private boolean isCurrentInSchedule(String streamName, Date now){
		
		boolean inSchedule = false;
		
		long nowTimeStamp = now.getTime();
				
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        
        String monthStr = "";
        if (month <10){
        	monthStr = "0"+month;
        }else{
        	monthStr = ""+month;
        }
        String dateStr = "";
        if (date <10){
        	dateStr = "0"+date;
        }else{
        	dateStr = "" + date;
        }
        
        String startTimeStr = "";        		
        String endTimeStr = "";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDateTime = null;
        Date endDateTime = null;
        
		java.util.Collection<SwitchRecord> recordList = SwitchRecordList.recordList.values();
		
		Iterator<SwitchRecord> it = recordList.iterator();
	    while (it.hasNext()) {
	    	SwitchRecord sc = (SwitchRecord) it.next();
	   
			startTimeStr = 	year + "-" + monthStr + "-" +dateStr + " " + sc.getStartTime();
			endTimeStr = year + "-" + monthStr + "-" +dateStr + " " + sc.getEndTime();
			
			try{
	            startDateTime = sdf.parse(startTimeStr);
	        }catch(ParseException e){
	        	logger.debug(this.CLASS_NAME+": meet ParseException : " + startTimeStr +e.getMessage());
		       	e.printStackTrace();
		        continue;	
		    }
	        
	        try{
	            endDateTime = sdf.parse(endTimeStr);
	        }catch(ParseException e){
	        	logger.debug(this.CLASS_NAME+": meet ParseException : " + endTimeStr +e.getMessage());
		       	e.printStackTrace();
		       	continue; 
		    }
	               
	        long startTimeStamp = startDateTime.getTime();
	        long endTimeStamp = endDateTime.getTime();
	        
	        if ((nowTimeStamp > startTimeStamp) && (nowTimeStamp < endTimeStamp)){
	        	inSchedule = true;
	        	logger.debug(this.CLASS_NAME+": isSchedule = true , do nothing!");
	        	break;
	        }
		}
		
		return inSchedule;
	}
    
	   
	
    public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		logger.debug(this.CLASS_NAME+": onUnPublish[" + stream.getContextStr() + "]: streamName:" + streamName + " isRecord:" + isRecord + " isAppend:" + isAppend);
	}
	
}
