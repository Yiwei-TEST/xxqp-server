package com.sy.sanguo.common.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

public class GameBackLogger extends Log4JLogger 
{


	private static final long serialVersionUID = 1L;
	

	public final static Log SYS_LOG = new GameBackLogger("sys");
	public final static Log MONITOR_LOG = new GameBackLogger("monitor");

	
	public GameBackLogger(String name) 
	{
		super(name);
	}
	
}

