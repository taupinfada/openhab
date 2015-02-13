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
		logger.debug("Subbmit : " + commandType.getCommand());
		out.println(commandType.getCommand());
		try {
			returnStr = in.readLine();
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
		}
		Matcher match = commandType.getPattern().matcher(returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(1));
			return commandType.getType(match.group(1));
		} else {
			logger.error("the return value '" + returnStr
					+ "' don't matche with the pattern '"
					+ commandType.getPattern() + "'");
			return null;
		}
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
