package com.nana.webapi.servlet;

/**
 * MainSend.java
 * 版权所有(C) 2012 
 * 创建:cuiran 2012-07-24 11:31:35
 */

import java.util.ArrayList;
import java.util.List;

import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO
 * @author cuiran
 * @version TODO
 */
public class MainSend {
	private static Log log = LogFactory.getLog(MainSend.class.getName());
	
	 /************************************************
	 测试推送服务器地址：gateway.sandbox.push.apple.com /2195 
	 产品推送服务器地址：gateway.push.apple.com / 2195 

	需要javaPNS_2.2.jar包

	 ***************************************************/
	/**

     *这是一个比较简单的推送方法，

     * apple的推送方法

     * @param tokens   iphone手机获取的token

     * @param path 这里是一个.p12格式的文件路径，需要去apple官网申请一个 

     * @param password  p12的密码 此处注意导出的证书密码不能为空因为空密码会报错

     * @param message 推送消息的内容

     * @param count 应用图标上小红圈上的数值

     * @param sendCount 单发还是群发  true：单发 false：群发

     */

	public void sendpush(List<String> tokens,String path, String password, String message,Integer count,boolean sendCount) {

	try {
		//message是一个json的字符串{“aps”:{“alert”:”iphone推送测试”}}

			PushNotificationPayload payLoad =  PushNotificationPayload.fromJSON(message);
			
			payLoad.addAlert("iphone推送测试 www.baidu.com"); // 消息内容
			
			payLoad.addBadge(count); // iphone应用图标上小红圈上的数值
			
			payLoad.addSound("default"); // 铃音 默认
			
			

			PushNotificationManager pushManager = new PushNotificationManager();
			
			//true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
			
			pushManager.initializeConnection(new AppleNotificationServerBasicImpl(path, password, false));
			
			List<PushedNotification> notifications = new ArrayList<PushedNotification>(); 
			
			// 发送push消息
			
			if (sendCount) {
			
			log.debug("--------------------------apple 推送 单-------");
			
			Device device = new BasicDevice();
			
			device.setToken(tokens.get(0));
			
			PushedNotification notification = pushManager.sendNotification(device, payLoad, true);
			
			notifications.add(notification);
			
			} else {
			
			log.debug("--------------------------apple 推送 群-------");
			
			List<Device> device = new ArrayList<Device>();
			
			for (String token : tokens) {
			
			device.add(new BasicDevice(token));
			
			}
			
			notifications = pushManager.sendNotifications(payLoad, device);
			
			}
			
			List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);
			
			List<PushedNotification> successfulNotifications = PushedNotification.findSuccessfulNotifications(notifications);
			
			int failed = failedNotifications.size();
			
			int successful = successfulNotifications.size();
			
			 
			
			if (successful > 0 && failed == 0) {
			
			System.out.println("-----All notifications pushed 成功 (" + successfulNotifications.size() + "):");
			
			} else if (successful == 0 && failed > 0) {
			
				System.out.println("-----All notifications 失败 (" + failedNotifications.size() + "):");
			
			} else if (successful == 0 && failed == 0) {
			
				System.out.println("No notifications could be sent, probably because of a critical error");
			
			} else {
			
				System.out.println("------Some notifications 失败 (" + failedNotifications.size() + "):");
			
				System.out.println("------Others 成功 (" + successfulNotifications.size() + "):");
			
			}
			System.out.println("end");
	
	// pushManager.stopConnection();

	} catch (Exception e) {
	
	e.printStackTrace();
	
	}

}
	
	/**
	 * TODO
	 * @param args
	 */
	public static void main(String[] args) {
		MainSend send=new MainSend();
		List<String> tokens=new ArrayList<String>();
		tokens.add("2545535b30537ba130b673750d38dcb12485b7aa56a3845c7fc796b36187fb46");
//		tokens.add("2545535b30537ba130b673750d38dcb12485b7aa56a3845c7fc796b36187fb46");
		String path="/Users/lizhihui/Downloads/Push.p12";
		String password="abcabc";
		String message="{'aps':{'alert':'iphone推送测试 www.baidu.com'}}";
		Integer count=1;
		boolean sendCount=false;
		send.sendpush(tokens, path, password, message, count, sendCount);
		
	}

}

