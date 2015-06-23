package com.nana.webapi.servlet;

import java.util.ArrayList;
import java.util.List;

import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.alibaba.fastjson.JSON;
import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;
import com.nana.common.message.ResponseMessage;
import com.nana.common.mq.ConsumerListener;
import com.nana.common.mq.MqFactory;

public class AppPushServlet extends HttpServlet {

	private static final long serialVersionUID = 4604994618124281361L;

	@Override
	public void init() throws ServletException {
		String cid = "CID_DEV_NANA_3";
		String topic = "DEV_NANA_3";

		ConsumerListener linstener = new ConsumerListener() {

			@Override
			public void process(String key, String tag, byte[] body) {
				ResponseMessage message = JSON.parseObject(new String(body),
						ResponseMessage.class);
				try {
					processResponse(message);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		};
		MqFactory.startMqConsumer(cid, topic, linstener);

	}

	private void processResponse(ResponseMessage rm) {
		int osType = rm.getMobileType();
		if (osType == 0) {// ios
			sendIOSResponse(rm);
		}
		if (osType == 1) {// android
			sendAndroidResponse(rm);
		}
	}

	/**
	 * 推送消息给ios设备<br>
	 * TODO optimize it
	 * 
	 * @param rm
	 */
	private void sendIOSResponse(ResponseMessage rm) {
		List<String> tokens = new ArrayList<String>();
		tokens.add(rm.getId());
		System.out.println(rm.getId() + "  " + rm.getDisplayText());
		// String path = "IntelligencePush.p12";
		String path = "NewItelligencePush.p12";
		String password = "abcabc";
		String message = rm.getDisplayText();
		sendIOS(tokens, path, password, message);
	}

	/**
	 * WARNNING just for testing TODO
	 * 
	 * @param tokens
	 * @param path
	 * @param password
	 * @param message
	 */
	private void sendIOS(List<String> tokens, String path, String password,
			String message) {
		try {
			PushNotificationPayload payLoad = new PushNotificationPayload();
			payLoad.addAlert(message); // 消息内容
			PushNotificationManager pushManager = new PushNotificationManager();
			// true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
			pushManager
					.initializeConnection(new AppleNotificationServerBasicImpl(
							path, password, false));
			List<PushedNotification> notifications = new ArrayList<PushedNotification>();
			// 发送push消息
			Device device = new BasicDevice();
			device.setToken(tokens.get(0));
			PushedNotification notification = pushManager.sendNotification(
					device, payLoad, true);
			notifications.add(notification);
			List<PushedNotification> failedNotifications = PushedNotification
					.findFailedNotifications(notifications);
			List<PushedNotification> successfulNotifications = PushedNotification
					.findSuccessfulNotifications(notifications);
			int failed = failedNotifications.size();
			int successful = successfulNotifications.size();
			if (successful > 0 && failed == 0) {
				System.out.println("-----All notifications pushed 成功 ("
						+ successfulNotifications.size() + "):");
			} else if (successful == 0 && failed > 0) {
				System.out.println("-----All notifications 失败 ("
						+ failedNotifications.size() + "):");
			} else if (successful == 0 && failed == 0) {
				System.out
						.println("No notifications could be sent, probably because of a critical error");
			} else {
				System.out.println("------Some notifications 失败 ("
						+ failedNotifications.size() + "):");
				System.out.println("------Others 成功 ("
						+ successfulNotifications.size() + "):");
			}

			// pushManager.stopConnection();

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	/**
	 * 推送消息给android设备
	 * 
	 * @param rm
	 *            消息
	 */
	private void sendAndroidResponse(ResponseMessage rm) {
		String apiKey = "ULRG70qpru7YYbXPbAML9lFq";
		String secretKey = "oCTV8NzWsXOyb60L77COjKHzYYiGUkVX";
		ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);

		// 2. 创建BaiduChannelClient对象实例
		BaiduChannelClient channelClient = new BaiduChannelClient(pair);

		// 3. 若要了解交互细节，请注册YunLogHandler类
		channelClient.setChannelLogHandler(new YunLogHandler() {
			@Override
			public void onHandle(YunLogEvent event) {
				System.out.println(event.getMessage());
			}
		});

		try {
			// userId=741732476559928435 channelId=3506718507675085896
			// 4. 创建请求类对象
			PushUnicastMessageRequest request = new PushUnicastMessageRequest();
			request.setDeviceType(3);

			request.setChannelId(Long.valueOf(rm.getId()));
			request.setUserId("772200991652482178");

			request.setMessage(rm.getDisplayText());
			// 06-19 04:21:11.604: I/System.out(1506): onBind errorCode=0
			// appid=6246871 userId=772200991652482178
			// channelId=4397593966091092445 requestId=3653006127

			// 5. 调用pushMessage接口
			PushUnicastMessageResponse response = channelClient
					.pushUnicastMessage(request);

		} catch (ChannelClientException e) {
			// 处理客户端错误异常
			e.printStackTrace();
		} catch (ChannelServerException e) {
			// 处理服务端错误异常
			System.out.println(String.format(
					"request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
		}

	}
}