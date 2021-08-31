package com.sy599.game.udplog;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;


/**
 * 日志信息处理器
 * @author taohuiliang
 * @datae 2012-07-16
 *
 */
public class LogClientHandler extends IoHandlerAdapter {
//	Logger log = Logger.getLogger("msg");
	
	 //当与游戏服务器建立连接时调用
    @Override  
    public void sessionOpened(IoSession session)throws Exception {  
//        System.out.println("myclient Opened:"+session.getRemoteAddress());
       // log.info("myclient:"+session.getRemoteAddress());
    }  
    
    //处理游戏服务器发过来的日志信息
    @Override  
    public void messageReceived(IoSession session, Object message)throws Exception {  
        
//    	System.out.println("myclient messageReceived");
          
    }  
    
    //当与游戏服务器的连接关闭时调用
    @Override  
    public void sessionClosed(IoSession session)throws Exception {  
//        System.out.println("myclient closed"+session.getRemoteAddress());
        
    }  
    
    //异常断开时调用
    @Override  
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception{ 
//       cause.printStackTrace();
        
    } 
}
