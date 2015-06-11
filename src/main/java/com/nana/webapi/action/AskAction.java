package com.nana.webapi.action;


public class AskAction {
	private String ask;


	public String getAsk() {
		return ask;
	}


	public void setAsk(String ask) {
		this.ask = ask;
	}


	public String execute() throws Exception {
		return "received";
	}
}
