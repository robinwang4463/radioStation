package com.ttstream.wowza.radio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import com.wowza.wms.logging.*;
import com.wowza.wms.server.*;

/*
 * This Server listener read configure file in which streamName , start time ,end time, mp3 file was setting
 */
public class LiveChannelPublisher implements IServerNotify2 {
	
	WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());
	private final String CLASS_NAME = "LiveChannelPublisher";
	private final long PERIOD_DAY = 24 * 60 * 60 * 1000;  

	public void onServerConfigLoaded(IServer server) {
		
	}

	public void onServerCreate(IServer server) {
		
	}

	public void onServerInit(IServer server) {
		logger.debug("onServerInit");
		
		String radioChannelTargetApplicationName = server.getProperties().getPropertyStr("RadioChannelTargetApplicationName","live");
		RadioConf.appName = radioChannelTargetApplicationName;
		
		String radioChannelFileName = server.getProperties().getPropertyStr("RadioChannelFileName");
		RadioConf.radioChannelFileName = radioChannelFileName;
		
		int advancedBufferTime = server.getProperties().getPropertyInt("AdvancedBufferTime",20);
		RadioConf.advancedBufferTime = advancedBufferTime;
		
		logger.debug(this.CLASS_NAME +" : read RadioChannelTargetApplicationName : "+radioChannelTargetApplicationName+ " , RadioChannelFileName : "+radioChannelFileName+" , AdvancedBufferTime : " +advancedBufferTime +"(Seconds)");
		
		if ( (RadioConf.radioChannelFileName == null) || (RadioConf.radioChannelFileName.equals("")) ){
			return;
		}
		
		
		ReadChannelList readChannelList = new ReadChannelList();
		readChannelList.init(); 
		readChannelList.readConf(); 
		readChannelList.publishStream();
		readChannelList.checkNewSwitchTask();
		
		//从明天00:00:00开始，每天执行一次
		Date tomorrow = getTomorrow();
		Timer timer = new Timer();
	    ReadChannelListTask rc = new ReadChannelListTask();
	    timer.schedule(rc, tomorrow,PERIOD_DAY); 
	}
    
	private Date getTomorrow(){
		
		Date tomorrowDate = null;
		Date todayDate = null;
		Date now = new Date();
        
		Calendar tomorrowCalendar = Calendar.getInstance();
		Calendar todayCalendar = Calendar.getInstance();
		Calendar nowCalendar = Calendar.getInstance();
	    
		nowCalendar.setTime(now);
	    int year = nowCalendar.get(Calendar.YEAR);
	    int month = nowCalendar.get(Calendar.MONTH)+1;
	    int day = nowCalendar.get(Calendar.DAY_OF_MONTH);
	        
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
	     
	    //获取今天00点00分00秒的Date
	     
	    String todayStr = year + "-" + monthStr + "-" +dayStr + " 00:00:00";
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     
	    try{
	        todayDate = sdf.parse(todayStr);
	    }catch(Exception e){
	        logger.debug(this.CLASS_NAME+": meet ParseException : " + todayStr +e.getMessage());
		    e.printStackTrace();
		}
	    
	    todayCalendar.setTime(todayDate);
	        
	    //获取明天00点00分00秒的Date
	    todayCalendar.add(Calendar.DAY_OF_MONTH,1);
	    tomorrowCalendar = todayCalendar;
	    	    
	    int tomorrowYear = tomorrowCalendar.get(Calendar.YEAR);
	    int tomorrowMonth = tomorrowCalendar.get(Calendar.MONTH)+1;
	    int tomorrowDay = tomorrowCalendar.get(Calendar.DAY_OF_MONTH);
	    int tomorrowHour =   tomorrowCalendar.get(Calendar.HOUR_OF_DAY);
	    int tomorrowMinute = tomorrowCalendar.get(Calendar.MINUTE);
	    int tomorrowSecond = tomorrowCalendar.get(Calendar.SECOND);
	    
	    String tomorrowMonthStr = "";
	    if (tomorrowMonth <10){
	    	tomorrowMonthStr = "0"+tomorrowMonth;
	    }else{
	    	tomorrowMonthStr = ""+month;
	    }
	        	
	    String tomorrowDayStr = "";
	    
	    if (tomorrowDay <10){
	    	tomorrowDayStr = "0"+tomorrowDay;
	    }else{
	    	tomorrowDayStr = ""+tomorrowDay;
	    }
	    
	    String tomorrowHourStr = "";
	    if (tomorrowHour <10){
	    	tomorrowHourStr = "0"+tomorrowHour;
	    }else{
	    	tomorrowHourStr = "" + tomorrowHour;
	    }
	    
	    String tomorrowMinuteStr = "";
	    if (tomorrowMinute < 10){
	    	tomorrowMinuteStr = "0"+tomorrowMinute;
	    }else{
	    	tomorrowMinuteStr = ""+ tomorrowMinute;
	    }
	    
	    String tomorrowSecondStr = "";
	    if (tomorrowSecond < 10){
	    	tomorrowSecondStr = "0"+tomorrowSecond;
	    }else{
	    	tomorrowSecondStr = ""+tomorrowSecond;
	    }
	    
	    String tomorrowStr = tomorrowYear + "-" + tomorrowMonthStr + "-" +tomorrowDayStr + " "+tomorrowHourStr+":"+tomorrowMinuteStr+":"+tomorrowSecondStr;
	    
	    logger.debug("\r\n");
        logger.debug("*************************************************************");
	    logger.debug(this.CLASS_NAME+": from tomorrow :"+tomorrowStr+ " ,read channel list every day.");
	    tomorrowDate = tomorrowCalendar.getTime();
	    return tomorrowDate;
		
	}
	public void onServerShutdownStart(IServer server) {
		
	}

	public void onServerShutdownComplete(IServer server) {
		
	}

}
