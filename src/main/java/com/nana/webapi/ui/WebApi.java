package com.nana.webapi.ui;

import com.nana.common.http.server.JettyCustomServer;


public class WebApi {

  public static void main(String[] args) {
    // workPath is configured in start-web.sh
    String webPath = args[0];
    JettyCustomServer server = new JettyCustomServer(webPath, "/");

    // start jetty server
    server.startServer();
  }
}
