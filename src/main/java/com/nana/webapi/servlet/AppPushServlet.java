package com.nana.webapi.servlet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
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
import com.nana.common.utils.Property;
import com.nana.webapi.bean.ResponseDisplay;
import com.nana.webapi.cacher.HtmlCacher;

public class AppPushServlet extends HttpServlet {

	private static final long serialVersionUID = 4604994618124281361L;
	PushNotificationManager pushManager;

	@Override
	public void init() throws ServletException {
		String cid = Property.getInstance().getCfg("cid3");
		String topic = Property.getInstance().getCfg("topic3");

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

		String path = "LatestPush.p12";
		String password = "123456";
		pushManager = new PushNotificationManager();
		// true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
		try {
			pushManager
					.initializeConnection(new AppleNotificationServerBasicImpl(
							path, password, false));
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeystoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		System.out.print(rm.getId());
		// 精简消息 去除id
		rm.setId(null);

		String message = null;
		ResponseDisplay rd = prepareHtml(rm);
		if (rd != null && rd.getDataType() != null && "1".equals(rd.getDataType())) {
			//如果是html的信息
			message = JSON.toJSONString(rd);
		} else {
			//普通对话消息
			message = JSON.toJSONString(rm);
		}
		System.out.println("  " + message);
		sendIOS(tokens, message);
	}

	/**
	 * 将html提取并放入本地Map，并把新生成的uuid放入消息并发给客户端，客户端使用uuid请求本地Map中的html
	 * 
	 * @param rm
	 */
	private ResponseDisplay prepareHtml(ResponseMessage rm) {
		if (rm.getDisplayText() == null)
			return null;
		ResponseDisplay rd = JSON.parseObject(rm.getDisplayText(),
				ResponseDisplay.class);
		String uuid = UUID.randomUUID().toString();
		HtmlCacher.HTMLCACHE.put(uuid, rd.getContent());
		
		//test must to delete
		System.out.println(rd.getContent());
		rd.setContent(uuid);
		return rd;
	}

	/**
	 * WARNNING just for testing TODO
	 * 
	 * @param tokens
	 * @param path
	 * @param password
	 * @param message
	 */
	private void sendIOS(List<String> tokens, String message) {
		try {

			long start = System.currentTimeMillis();
			PushNotificationPayload payLoad = new PushNotificationPayload();
			payLoad.addAlert("您有一条新消息！"); // 消息内容
			payLoad.addCustomDictionary("data", message);
			List<PushedNotification> notifications = new ArrayList<PushedNotification>();
			// 发送push消息
			Device device = new BasicDevice();
			device.setToken(tokens.get(0));
			long end1 = System.currentTimeMillis();
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
			long end2 = System.currentTimeMillis();

			System.out.println("init time:" + (end1 - start));
			System.out.println("send time:" + (end2 - start));
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

		String apiKey = "UNHymwX9PMI4OnxYxsUaLDVk";
		String secretKey = "x4uYx5SSDHnyMGAb4djksDrF1M5NXHAf";
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
			// request.setDeviceType(rm.getMobileType());//3

			// request.setChannelId(Long.valueOf(rm.getId()));
			request.setUserId(rm.getId());

			// request.setMessageType(1);
			request.setMessage(rm.getDisplayText());
			// 06-19 04:21:11.604: I/System.out(1506): onBind errorCode=0
			// appid=6246871 userId=772200991652482178
			// channelId=4397593966091092445 requestId=3653006127

			// 5. 调用pushMessage接口
			PushUnicastMessageResponse response = channelClient
					.pushUnicastMessage(request);
			System.out.println("success sent:" + response.getSuccessAmount());

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