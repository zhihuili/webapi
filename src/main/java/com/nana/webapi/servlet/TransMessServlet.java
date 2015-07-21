package com.nana.webapi.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nana.webapi.cacher.HtmlCacher;

public class TransMessServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uuid = request.getParameter("uuid");
		
		String html = HtmlCacher.HTMLCACHE.get(uuid);
		if(html == null) return ;
		HtmlCacher.HTMLCACHE.remove(uuid);
		PrintWriter writer = response.getWriter();
		writer.write(html);
		writer.flush();
		writer.close();
	}

}
