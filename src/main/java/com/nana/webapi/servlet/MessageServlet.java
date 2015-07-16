package com.nana.webapi.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.nana.common.message.GPS;
import com.nana.common.message.RequestMessage;
import com.nana.common.mq.MqFactory;
import com.nana.common.mq.MqProducer;
import com.nana.common.utils.Property;

public class MessageServlet extends HttpServlet {

	private static final long serialVersionUID = 2729029845665031617L;
	MqProducer opc = MqFactory.getMqProducer(Property.getInstance().getCfg("pid1"));
	String topic = Property.getInstance().getCfg("topic1");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		RequestMessage rm = buildRequestMessage(request);
		String json = JSON.toJSONString(rm);
		opc.sendMessage(topic, "", "", json.getBytes());
		out.println("OK");

	}

	private RequestMessage buildRequestMessage(HttpServletRequest request) {
		RequestMessage rm = new RequestMessage();
		rm.setId(request.getParameter("userid"));
		rm.setContent(request.getParameter("text"));
		try {
			String gpsStr = request.getParameter("gps");
			String[] gpsa = gpsStr.split(",");
			GPS gps = new GPS();
			gps.setLongitude(Double.valueOf(gpsa[0]));
			gps.setLatitude(Double.valueOf(gpsa[1]));
			gps.setHeight(Double.valueOf(gpsa[2]));
			rm.setGps(gps);
		} catch (Exception e) {
			// do nothing
		}
		String os = request.getParameter("os");
		if (os != null) {
			if ("ios".equals(os)) {
				rm.setMobileType(0);
			}
			if ("android".equals(os)) {
				rm.setMobileType(1);
			}
		}
		return rm;
	}

}
