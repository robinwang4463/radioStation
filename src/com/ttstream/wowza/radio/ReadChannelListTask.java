package com.ttstream.wowza.radio;

import java.util.TimerTask;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

public class ReadChannelListTask extends TimerTask {
	WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());
	
	public void run()
	{
		ReadChannelList rc = new ReadChannelList();
		rc.init();
		rc.readConf();
		rc.publishStream();
		rc.checkNewSwitchTask();
	}

}
