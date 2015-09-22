package com.leap12.common;

import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	private String username;
	private String password;
	private String from;
	private String subject = "";
	private String body = "";
	private List<String> recipients;
	private boolean tls = true;
	private boolean auth = true;
	private String host = "smtp.gmail.com";
	private int port = 587;

	public SendMail() {
	}

	public SendMail(String username, String password, String from) {
		this.username = username;
		this.password = password;
		this.from = from;
	}

	public void setAuth(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSmtpHost(String host) {
		this.host = host;
	}

	public void setSmtpAuth(boolean auth) {
		this.auth = auth;
	}

	public void setTLS(boolean tls) {
		this.tls = tls;
	}

	public void send() throws Exception {
		validate();
		Properties props = new Properties();
		props.put("mail.smtp.auth", String.valueOf(auth));
		props.put("mail.smtp.starttls.enable", String.valueOf(tls));
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", String.valueOf(port));

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		for (String recipient : recipients) {
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
		}
		message.setSubject(subject);
		message.setText(body);
		Transport.send(message);
	}

	private void validate() {
		String errMsg = "";
		if (StrUtl.isEmpty(username)) {
			errMsg = errMsg + "username is empty;";
		}
		if (StrUtl.isEmpty(password)) {
			errMsg = errMsg + "password is empty;";
		}
		if (StrUtl.isEmpty(from)) {
			errMsg = errMsg + "From is empty;";
		}
		if (recipients == null || recipients.size() == 0) {
			errMsg = errMsg + "No recipients;";
		}
		if (errMsg.length() > 0) {
			throw new IllegalStateException(errMsg);
		}
	}
}
