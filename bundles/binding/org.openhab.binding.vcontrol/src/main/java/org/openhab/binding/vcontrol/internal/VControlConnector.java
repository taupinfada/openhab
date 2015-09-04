package org.openhab.binding.vcontrol.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VControlConnector implements ViessmannConnector {
	static final Logger logger = LoggerFactory
			.getLogger(VControlConnector.class);

	private static VControlConnector singleton = null;
	private Socket s = null;
	private BufferedReader in = null;
	private PrintWriter out = null;

	private VControlConnector() {
	}

	public static VControlConnector getVcontrolConnector() {
		if (singleton == null) {
			singleton = new VControlConnector();
		}
		return singleton;
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
	public boolean connect(String serverIp, int serverPort) {
		if (s != null && !disconnect()) {
			return false;
		}
		try {
			s = new Socket(serverIp, serverPort);
		} catch (UnknownHostException e) {
			logger.error("Unknow Host : " + e.getMessage());
			return false;
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
			return false;
		}

		try {
			out = new PrintWriter(s.getOutputStream(), true);
		} catch (IOException e) {
			logger.error("Opening of output stream fails : " + e.getMessage());
			return false;
		}
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			logger.error("Opening of input stream fails : " + e.getMessage());
			return false;
		}
		logger.debug("vcontrold connect");
		return true;
	}

	/**
	 * read the value return by vcontrold whith the command
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	public String getValue(String command) {
		String returnStr = null;
		logger.debug("Subbmit : " + command);
		out.println(command);
		try {
			returnStr = in.readLine();
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
			return null;
		}
		Matcher match = Pattern.compile("^vctrld>(.*)$").matcher(returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(1));
			return match.group(1);
		} else {
			logger.error("the command '{}' failed : {}", command, returnStr);
			return null;
		}
	}

	/**
	 * read the value return by vcontrold whith the command "commands"
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	public List<String> getCommands() {
		return getMultipleLines("commands");
	}

	/**
	 * /** read the value return by vcontrold whith the command "commands"
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	public List<String> getDetailCommand(String vCommand) {
		return getMultipleLines("detail " + vCommand);
	}

	/**
	 * read the value return by vcontrold whith the command
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	public List<String> getMultipleLines(String command) {
		logger.debug("Subbmit : " + command);
		out.println(command);
		List<String> commandsLines = new ArrayList<String>();
		String pattern = "vctrld>";
		StringBuffer sb = new StringBuffer();
		char lastChar = pattern.charAt(pattern.length() - 1);
		boolean endPrompteur = false;
		try {
			char ch = (char) in.read();
			while (true) {
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern) && endPrompteur) {
						return commandsLines;
					} else if (sb.toString().endsWith(pattern)) {
						sb = new StringBuffer();
						endPrompteur = true;
					}
				}
				if (ch == '\n') {
					logger.debug(sb.toString());
					commandsLines.add(sb.toString());
					sb = new StringBuffer();
					endPrompteur = true;
				}
				ch = (char) in.read();
			}
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
			return null;
		}
	}

	/**
	 * submit the command to boiler
	 * 
	 * @param commandStr
	 * @return if command submitted is OK then return true else false
	 */
	public boolean submit(String commandStr) {
		logger.debug("Subbmit : " + commandStr);
		out.println(commandStr);
		String returnStr = null;
		try {
			returnStr = in.readLine();
		} catch (IOException e) {
			logger.error("I/O error : " + e.getMessage());
			return false;
		}
		Matcher match = Pattern.compile("^vctrld>([OK]{2})$")
				.matcher(returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(1));
			return match.group(1).equals("OK");
		} else {
			logger.error("the command '{}' failed : {}", commandStr, returnStr);
			return false;
		}
	}

	/**
	 * submit the value with the command to boiler
	 * 
	 * @param command
	 * @param value
	 * @return if command submitted is OK then return true else false
	 */
	public boolean setValue(String command, String value) {
		return submit(command + " " + value);
	}

	/**
	 * disconnect from vcontrold
	 */
	public boolean disconnect() {
		try {
			in.close();
		} catch (IOException e) {
			logger.error("can't close datain", e);
			return false;
		}
		out.close();
		try {
			s.close();
		} catch (IOException e) {
			logger.error("can't close connexion", e);
			return false;
		}
		in = null;
		out = null;
		s = null;
		logger.debug("vcontrold disconnect");
		return true;
	}
}
