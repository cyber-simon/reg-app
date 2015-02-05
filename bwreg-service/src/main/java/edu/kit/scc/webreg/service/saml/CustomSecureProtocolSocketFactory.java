package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

public class CustomSecureProtocolSocketFactory implements SecureProtocolSocketFactory {

	private KeyManager keyManager;
	private SSLContext sslcontext;
	
	public CustomSecureProtocolSocketFactory(X509Certificate cert, PrivateKey privateKey) 
			throws NoSuchAlgorithmException, KeyManagementException {
		keyManager = new CustomKeyManager(cert, privateKey);
		sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init(new KeyManager[] { keyManager }, null, null);
	}
	
	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress,
			int localPort) throws IOException, UnknownHostException {
		return sslcontext.getSocketFactory().createSocket(
				host, port, localAddress, localPort);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress,
			int localPort, HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		return sslcontext.getSocketFactory().createSocket(
				host, port, localAddress, localPort);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return sslcontext.getSocketFactory().createSocket(
				host, port);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslcontext.getSocketFactory().createSocket(
				socket, host, port, autoClose);
	}
	

}
