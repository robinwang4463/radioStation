package com.ttstream.wowza.radio;

import com.wowza.wms.application.*;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;

public class SourceStreamListener extends ModuleBase {
	
	WMSLogger logger = WMSLoggerFactory.getInstance().getLoggerObj(this.getClass().getName());

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate: " + stream.getSrc());
		
		if (stream.getClient() == null){
			return;
		}
		
		logger.debug("onStreamCreate: "+ stream.getName() + " contextStr = " +stream.getContextStr()+ ",and it is rtmp income stream, prepare add StreamListener");
		
		IMediaStreamActionNotify3 actionNotify = new StreamListener();			
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			props.put("streamActionNotifier", actionNotify);
		}
		stream.addClientListener(actionNotify);
	}

	public void onStreamDestroy(IMediaStream stream) {
		//if it is not a rtmp push stream¡£  
		if (stream.getClient() == null){
			return;
		}
				
		// only print log for rtmp income stream
		logger.debug("onStreamDestroy: " + stream.getName() + "  contextStr = " +stream.getContextStr()+ " ,and it is rtmp income stream, prepare remove StreamListener");
				
		IMediaStreamActionNotify3 actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			actionNotify = (IMediaStreamActionNotify3) stream.getProperties().get("streamActionNotifier");
		}
		if (actionNotify != null)
		{
			stream.removeClientListener(actionNotify);
			stream.getProperties().remove("streamActionNotifier");
		}
	}
}
