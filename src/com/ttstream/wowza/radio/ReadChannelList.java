package com.ttstream.wowza.radio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.publish.Stream;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.VHostSingleton;
import java.text.ParseException;

public class ReadChannelList {
	
	WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());
	
	private final String CLASS_NAME = "ReadChannelList";
	private IVHost vhost = null;
	
	public void init(){
		try{
			vhost = VHostSingleton.getInstance(IVHost.VHOST_DEFAULT);
			if(vhost == null){
				logger.debug(this.CLASS_NAME + ": Failed to get Vhost, can not check channel list file !");
			}
		}catch (Exception e)
		{
			logger.debug(CLASS_NAME + ": Failed to get Vhost, can not check channel list file !");
			e.printStackTrace();
		}
		
		//每次先将记录清空
        SwitchRecordList.recordList.clear();
        
        Iterator<Timer> timers = SwitchTimerList.timerList.values().iterator();
        while(timers.hasNext()){
        	timers.next().cancel();
        }
        SwitchTimerList.timerList.clear();
	}
	
	//把配置文件读取到内存中，保存在SwichRecordList中，然后关闭文件，注意: 重复的记录会被跳过
	// read the setting from the configure file, then save them in SwichRecordList,then close the file
	public void readConf(){
		
        logger.debug("\r\n");
        logger.debug("*************************************************************");
        logger.debug(this.CLASS_NAME+": reload config file : "+ RadioConf.radioChannelFileName);
                
        IVHost vhost =  VHostSingleton.getInstance(IVHost.VHOST_DEFAULT);
		String homePath = vhost.getHomePath();
		String CONTENT_BASE_PATH = homePath + File.separator +"conf" ;
		
		try{
			FileInputStream fis = new FileInputStream(CONTENT_BASE_PATH + File.separator +RadioConf.radioChannelFileName);
		    InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
	        BufferedReader br = new BufferedReader(isr);
	        
	        String line="";
	        int lineNo = 0;
	        String[] arrs=null;
	        String streamName = "";
	        String sourceStreamName = "";
	        String startTime = "";
	        String endTime = "";
	        String mp3FileName = "";
	        
	        while ((line=br.readLine())!=null){
	            lineNo = lineNo + 1;    
	        	arrs=line.split("\\s+");
	            if (arrs.length != 5)
	            {
	            	logger.debug("\r\n");
	                logger.debug("*************************************************************");
	            	logger.debug(this.CLASS_NAME+": This line miss some params, skip it. : "+line );
	            	continue;
	            }
	            streamName = arrs[0];
	            sourceStreamName = arrs[1];
	            startTime = arrs[2];
	            endTime = arrs[3];
	            mp3FileName = arrs[4];
	                
	            logger.debug("\r\n");
	            logger.debug("*************************************************************");
	            logger.debug(this.CLASS_NAME+ ": read line("+lineNo+") : "+ streamName + " : " + sourceStreamName + " : " + startTime +" : " + endTime +" : " + mp3FileName);
	            
	            String thisKey = streamName + "*" + sourceStreamName + "*" + startTime + "*" + endTime + "*" + mp3FileName;
	            
	            //将这条记录添加到记录列表中
	            if(!SwitchRecordList.recordList.containsKey(thisKey)){
	            	
	            	SwitchRecord sc = new SwitchRecord();
		            sc.setStreamName(streamName);
		            sc.setSourceStreamName(sourceStreamName);
		            sc.setStartTime(startTime);
		            sc.setEndTime(endTime);
		            sc.setMp3FileName(mp3FileName);
		            synchronized (SwitchRecordList.recordList)
		            {
		              SwitchRecordList.recordList.put(thisKey, sc);
		            }
	            }
	        }
	        
	        br.close();
	        isr.close();
	        fis.close();
	        logger.debug("\r\n");
	        logger.debug("*************************************************************");
            logger.debug(this.CLASS_NAME+": complete read channel file successed, file name : "+RadioConf.radioChannelFileName);
            logger.debug("\r\n");
	        logger.debug("*************************************************************");
		
		}catch (IOException e)
		{
			logger.debug(this.CLASS_NAME+ ": read channel file failed, file name : "+RadioConf.radioChannelFileName);
	       	e.printStackTrace();
	       	return;
		}
	}
	
	//发布一个虚拟直播流,并开始播放源流，并把它加入RadioChannelList中，已经发布的不会重复发布，流的名字是streamName , 源流的名字是sourceStreamName
	//publish a stream,and playback the source stream, and then add it to RadioChannelList
	public void publishStream(){
		Iterator<SwitchRecord> iterator = SwitchRecordList.recordList.values().iterator();
		while (iterator.hasNext()){
		
			SwitchRecord sr = iterator.next();
			String streamName = sr.getStreamName();
			String sourceStreamName = sr.getSourceStreamName();
			if (!RadioChannelList.hasStreamName(streamName))
	        {
				logger.debug("\r\n");
		        logger.debug("*************************************************************");
				logger.debug(this.CLASS_NAME+": It's a new stream, we need to publish it in server-side : "+ streamName + " and source = "+sourceStreamName);
	    	    Stream myStream = Stream.createInstance(this.vhost, RadioConf.appName, streamName);
	            myStream.setRepeat(true);
	            myStream.setSwitchLog(true);
	            myStream.setStartLiveOnPreviousBufferTime(10000);  //这个参数的作用，目前还没有测试出来
	            myStream.addToPlaylist(0, sourceStreamName, -2, -1);
	            myStream.play(0);
	            logger.debug(this.CLASS_NAME+ ": start play source stream :"+sourceStreamName +" , publish stream name = "+streamName);
	            RadioChannelList.add(new RadioChannel(streamName,sourceStreamName,myStream));
	        }    	
	   }	
	}
	
	//检查一条记录，看看是否要添加到定时切换任务中，还没有开始的，加入定时任务，已经开始的还没有结束的，立即执行，已经结束的，跳过
	//check each line in configure file to adjust whether it was needed to add to the switch task or not
	public void checkNewSwitchTask()
	{
		Date now = new Date();
        long nowTimeStamp = now.getTime()/1000;
    
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
    
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour =   calendar.get(Calendar.HOUR_OF_DAY);
	    int minute = calendar.get(Calendar.MINUTE);
	    int second = calendar.get(Calendar.SECOND);
	    
        String monthStr = "";
        if (month <10){
        	monthStr = "0"+month;
        }else{
        	monthStr = ""+month;
        }
    	
        String dayStr = "";
        if (day <10){
        	dayStr = "0"+day;
        }else{
        	dayStr = ""+day;
        }
        
        
        String hourStr = "";
	    if (hour <10){
	    	hourStr = "0"+hour;
	    }else{
	    	hourStr = "" + hour;
	    }
	    
	    String minuteStr = "";
	    if (minute < 10){
	    	minuteStr = "0"+minute;
	    }else{
	    	minuteStr = ""+ minute;
	    }
	    
	    String secondStr = "";
	    if (second < 10){
	    	secondStr = "0"+second;
	    }else{
	    	secondStr = ""+second;
	    }
        
        
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDateTime = null;
        Date advancedStartDateTime = null;
        Date endDateTime = null;
        
		Iterator<SwitchRecord> iterator = SwitchRecordList.recordList.values().iterator();
		while (iterator.hasNext()){
				
			SwitchRecord sr = iterator.next();
			String streamName = sr.getStreamName();
			String sourceStreamName = sr.getSourceStreamName();
			String startTime = sr.getStartTime();
			String endTime = sr.getEndTime();
			String mp3FileName =  sr.getMp3FileName();
		
			logger.debug("\r\n");
	        logger.debug("*************************************************************");
			logger.debug(this.CLASS_NAME+ ": It's a new line,we need to check if it shoud be added to switch task!");
	        
            String startTimeStr = year + "-" + monthStr + "-" +dayStr + " " + startTime;
            String endTimeStr = year + "-" + monthStr + "-" +dayStr + " " + endTime;
            String nowTimeStr = year + "-" + monthStr + "-" +dayStr + " " +hourStr+":"+minuteStr+":"+secondStr;
            logger.debug(this.CLASS_NAME+": nowTime = "+nowTimeStr+" , startTime = "+startTimeStr+" , endTime = "+endTimeStr);
        
            try{
            	startDateTime = sdf.parse(startTimeStr);
            }catch(ParseException e){
            	e.printStackTrace();
            	logger.debug(this.CLASS_NAME+": meet ParseException ,skip to next record: " + startTimeStr +e.getMessage());
                continue;  	
	        }
        
            try{
            	endDateTime = sdf.parse(endTimeStr);
            }catch(ParseException e){
            	e.printStackTrace();
            	logger.debug(this.CLASS_NAME+": meet ParseException ,skip to next record: " + endTimeStr +e.getMessage());
	        }
               
            long startTimeStamp = startDateTime.getTime()/1000;
            long endTimeStamp = endDateTime.getTime()/1000;
        
            //logger.debug(this.CLASS_NAME+": nowTimeStamp:"+nowTimeStamp+" , startTimeStamp:"+startTimeStamp+" , endTimeStamp:"+endTimeStamp);
        
            advancedStartDateTime = new Date();
            advancedStartDateTime.setTime((startTimeStamp - RadioConf.advancedBufferTime) * 1000);
        
            
            SwitchToMp3Task st = null;
            Timer timer = null;
        
            //如果还没到开始时间,设置一个切换任务
            if (startTimeStamp >= nowTimeStamp)
            {
        	   	st = new SwitchToMp3Task();
        	   	st.setStreamName(streamName);
        	   	st.setMp3FileName(mp3FileName);
        	   	timer = new Timer();
        	   	timer.schedule(st, advancedStartDateTime);  
                logger.debug(this.CLASS_NAME+": add new switch task! startTime : "+startTime);
            }else if(nowTimeStamp < endTimeStamp)   // 已经过了开始时间，但是还在结束时间之前的，要立即切换
            {
            	int duration = (int)(endTimeStamp - nowTimeStamp); // 计算还需要播放的时间(秒)
            	duration = duration - RadioConf.advancedBufferTime;  // 因为有buffer，所以要提前结束；
            	if (duration > 0){
            		st = new SwitchToMp3Task();
            		st.setStreamName(streamName);
            		st.setMp3FileName(mp3FileName);
            		st.setDuration(duration); // 设定需要播放的时间,  set the playback duration
            		timer = new Timer();
            		timer.schedule(st, 0); //立即执行  need to playback immediately
            		logger.debug(this.CLASS_NAME+": add new switch task! switch now!");
            	}else{
            		logger.debug(this.CLASS_NAME+": the current time is in the middle of startTime and endTime, but the rest duration is too short to playback,skip it. ");
            	}
            }
            else{
            	logger.debug(this.CLASS_NAME+": Both startTime and endTime are past time,skip it!");
            }
            
            //如果有切换的定时任务，就把这个任务加入列表中,
            if (timer != null){
            	String key =  streamName + "*" + sourceStreamName + "*" + startTime + "*" + endTime + "*" + mp3FileName;
            	SwitchTimerList.timerList.put(key, timer);
        	}
		}
	}
}
