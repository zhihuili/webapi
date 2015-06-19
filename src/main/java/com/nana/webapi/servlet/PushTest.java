package com.nana.webapi.servlet;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class PushTest {
	public static void main( String[] args )
	{
		ApnsService service =
			    APNS.newService()
			    .withCert("/Users/lizhihui/Downloads/Push.p12", "abcabc")
			    .withSandboxDestination()
			    .build();

		String payload = APNS.newPayload().alertBody("Can't be simpler than this!").build();
		String token = "2545535b30537ba130b673750d38dcb12485b7aa56a3845c7fc796b36187fb46";
		service.push(token, payload);
	}
}
