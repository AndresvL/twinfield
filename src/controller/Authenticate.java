package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;


public abstract class Authenticate {
	public abstract void authenticate(String token, String secret, String softwareToken, HttpServletRequest req , HttpServletResponse resp) throws ClientProtocolException, IOException, ServletException;
}
