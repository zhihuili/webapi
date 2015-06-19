package com.nana.webapi.servlet;

import java.util.ArrayList;
import java.util.List;

import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;

public class MainTest {
	public static void main(String[] args) throws Exception {
		String deviceToken = "2545535b30537ba130b673750d38dcb12485b7aa56a3845c7fc796b36187fb46";
		String alert = "test from zhihui";// push的内容
		int badge = 100;// 图标小红圈的数值
		String sound = "default";// 铃音

		List<String> tokens = new ArrayList<String>();
		tokens.add(deviceToken);
		String certificatePath = "/Users/lizhihui/Downloads/Push.p12";
		String certificatePassword = "abcabc";// 此处注意导出的证书密码不能为空因为空密码会报错
		boolean sendCount = true;

		try {
			PushNotificationPayload payLoad = new PushNotificationPayload();
			payLoad.addAlert(alert); // 消息内容
			PushNotificationManager pushManager = new PushNotificationManager();
			// true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
			pushManager
					.initializeConnection(new AppleNotificationServerBasicImpl(
							certificatePath, certificatePassword, true));
			List<PushedNotification> notifications = new ArrayList<PushedNotification>();
			// 发送push消息
			if (sendCount) {
				Device device = new BasicDevice();
				device.setToken(tokens.get(0));
				PushedNotification notification = pushManager.sendNotification(
						device, payLoad, true);
				notifications.add(notification);
			} else {
				List<Device> device = new ArrayList<Device>();
				for (String token : tokens) {
					device.add(new BasicDevice(token));
				}
				notifications = pushManager.sendNotifications(payLoad, device);
			}
			List<PushedNotification> failedNotifications = PushedNotification
					.findFailedNotifications(notifications);
			List<PushedNotification> successfulNotifications = PushedNotification
					.findSuccessfulNotifications(notifications);
			int failed = failedNotifications.size();
			int successful = successfulNotifications.size();
			pushManager.stopConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
