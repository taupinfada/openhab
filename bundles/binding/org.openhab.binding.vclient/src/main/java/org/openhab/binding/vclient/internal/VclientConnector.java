/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I/O whith vcontrold too interact whith the boiler.
 * 
 * @author Felix CARDON
 * @since 0.1.0
 */
public class VclientConnector {

	static final Logger logger = LoggerFactory
			.getLogger(VclientConnector.class);

	private Socket s = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String serverIp = "";
	private int serverPort;

	public VclientConnector(String serverIp, int serverPort) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}

	/**
	 * connects to the boiler via network
	 * 
	 * @throws UnknownHostException
	 *             indicate that the IP address of a host could not be
	 *             determined.
	 * @throws IOException
	 *             indicate that no data can be read from vcontrold
	 */
	public void connect() {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				logger.error("can't close connexion", e);
			}
		}
		try {
			s = new Socket(serverIp, serverPort);
		} catch (UnknownHostException e) {
			logger.error("Unknow Host : " + e.getMessage());
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
		}

		try {
			out = new PrintWriter(s.getOutputStream(), true);
		} catch (IOException e) {
			logger.error("Opening of output stream fails : " + e.getMessage());
		}
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			logger.error("Opening of input stream fails : " + e.getMessage());
		}
		logger.debug("vcontrold connect");
	}

	/**
	 * read the value return by vcontrold whith the command
	 * 
	 * @param a
	 *            command to submit
	 * @return a array with all internal data of the heatpump
	 * @throws IOException
	 *             indicate that no data can be read from the heatpump
	 */
	public State getValue(VclientCommandType commandType) {
		String returnStr = null;
		logger.debug("Subbmit : " + commandType.getCommandGetter());
		out.println(commandType.getCommandGetter());
		try {
			returnStr = in.readLine();
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
		}
		Matcher match = commandType.getPattern().matcher(returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(1));
			return getState(commandType, match.group(1));
		} else {
			logger.error("the return value '" + returnStr
					+ "' don't matche with the pattern '"
					+ commandType.getPattern() + "'");
			return null;
		}
	}

	/**
	 * submit the value with the command to boiler
	 * 
	 * @param command
	 * @param value
	 * @return if command submitted is OK then return true else false
	 */
	public boolean setValue(VclientCommandType command, Command value) {
		String commandStr = getCommand(command, value);
		logger.debug("Subbmit : " + commandStr);
		out.println(commandStr);
		String returnStr = null;
		try {
			returnStr = in.readLine();
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
		}
		Matcher match = Pattern.compile("^vctrld>([OK]{2})$")
				.matcher(returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(1));
			return match.group(1).equals("OK");
		} else {
			logger.error("the return value '" + returnStr
					+ "' don't matche with the pattern '"
					+ command.getPattern() + "'");
			return false;
		}
	}

	public State getState(VclientCommandType typeClass, String value) {
		State state = null;
		if (typeClass.equals(StringType.class)) {
			state = new StringType(value);
		} else if (typeClass.equals(DecimalType.class)) {
			state = new DecimalType(value);
		} else if (typeClass.equals(OnOffType.class)) {
			if (value.equals("1") || value.equals("ON"))
				state = OnOffType.ON;
			else
				state = OnOffType.OFF;
		}
		return state;
	}

	public String getCommand(VclientCommandType typeClass, Command value) {
		String command = typeClass.getCommandSetter() + " ";
		if (typeClass.equals(StringType.class)) {
			command += value;
		} else if (typeClass.equals(DecimalType.class)) {
			command += ((DecimalType) value).longValue();
		} else if (typeClass.equals(OnOffType.class)) {
			command += value.equals(OnOffType.ON) ? "1" : "0";
		} else {
			command += value;
		}
		return command;
	}

	/**
	 * disconnect from vcontrold
	 */
	public void disconnect() {
		try {
			in.close();
		} catch (IOException e) {
			logger.error("can't close datain", e);
		}
		out.close();
		try {
			s.close();
		} catch (IOException e) {
			logger.error("can't close connexion", e);
		}
		in = null;
		out = null;
		s = null;
		logger.debug("vcontrold disconnect");
	}

}
