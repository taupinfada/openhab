package org.openhab.binding.vcontrol.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		if (returnStr == null) {
			logger.error("the command '{}' returned nothing", command);
			return null;
		}
		Matcher match = Pattern.compile(
				"^(vctrld>)?(\\S*) ?"
						+ VControlAvailableCommand.getVControlAvailableCommand(
								this).getUnit(command) + "\\s*$").matcher(
				returnStr);
		if (match.find()) {
			logger.debug("return value : " + match.group(2));
			return match.group(2);
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
		List<String> commands = new ArrayList<String>();
		commands.addAll(getCommandsWithDescripting().keySet());
		return commands;
	}

	public Map<String, String> getCommandsWithDescripting() {
		Map<String, String> commands = new HashMap<String, String>();
		for (String command : getMultipleLines("commands")) {
			commands.put(command.split(":")[0].trim(),
					command.split(":")[1].trim());
		}
		return commands;
	}

	/**
	 * /** read the value return by vcontrold whith the command "commands"
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	private List<String> getDetailCommand(String vCommand) {
		return getMultipleLines("detail " + vCommand);
	}

	/**
	 * read the value return by vcontrold whith the command
	 * 
	 * @param command
	 *            to submit
	 * @return Boiler return String
	 */
	private List<String> getMultipleLines(String command) {
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

	public boolean isAvailableCommand(String command) {
		return VControlAvailableCommand.getVControlAvailableCommand(this)
				.isAvailable(command);
	}

	@Override
	public String toString() {
		String toString = "";
		VControlAvailableCommand vcac = VControlAvailableCommand
				.getVControlAvailableCommand(this);
		for (String command : vcac.getAvailableCommands().keySet()) {
			toString += "#### Command '" + command + "' : "
					+ vcac.getAvailableCommands().get(command) + "\n";
			if (vcac.getUnit(command) != null
					&& !vcac.getUnit(command).isEmpty()) {
				toString += "\tUnit '" + vcac.getUnit(command) + "'\n";
			}
			if (vcac.getAcceptedValues(command) != null
					&& vcac.getAcceptedValues(command).size() > 0) {
				toString += "\taccepted Values :\n";
				for (String acceptedValue : vcac.getAcceptedValues(command)) {
					toString += "\t - " + acceptedValue + "\n";
				}
			}
			toString += "\n";
		}
		return toString;
	}

	private static class VControlAvailableCommand {

		private static VControlAvailableCommand singleton = null;
		private InetAddress host;
		private int port;
		private Map<String, String> availableCommands;
		private Map<String, String> commandUnit;
		private Map<String, List<String>> acceptedValues;

		private static final Pattern TYPE_PATTERN = Pattern
				.compile("Type: (.*)");
		private static final Pattern UNIT_PATTERN = Pattern
				.compile("Einheit: (.*)");
		private static final Pattern ENUM_PATTERN = Pattern
				.compile("Enum Bytes:(.*) Text");

		private VControlAvailableCommand(VControlConnector vcc) {
			this.host = vcc.s.getInetAddress();
			this.port = vcc.s.getPort();
			this.availableCommands = vcc.getCommandsWithDescripting();
			commandUnit = new HashMap<String, String>();
			acceptedValues = new HashMap<String, List<String>>();
			for (String command : availableCommands.keySet()) {
				List<String> detailsCommand = vcc.getDetailCommand(command);
				for (String detail : detailsCommand) {
					Matcher typeMatcher = TYPE_PATTERN.matcher(detail);
					// We save only the first Type, which matches with the
					// submit or return values
					// If a second type find, it matches with the state of
					// submit request (OK or NOT OK)
					// This state does'nt interest in our context
					if (typeMatcher.find() && !commandUnit.containsKey(command)) {
						if (typeMatcher.group(1).equals("enum")) {
							commandUnit.put(command, null);
							acceptedValues
									.put(command, new ArrayList<String>());
						} else {
							acceptedValues.put(command, null);
							commandUnit.put(command, "");
						}
					}

					Matcher unitMatcher = UNIT_PATTERN.matcher(detail);
					if (unitMatcher.find() && commandUnit.get(command) != null) {
						logger.debug("The unit use for {} is {}.", command,
								unitMatcher.group(1));
						String unit = unitMatcher.group(1).equals("(null)") ? ""
								: unitMatcher.group(1);
						commandUnit.put(command, unit);
					}

					Matcher valuesMatcher = ENUM_PATTERN.matcher(detail);
					if (valuesMatcher.find()
							&& acceptedValues.get(command) != null) {
						if (!acceptedValues.containsKey(command)) {
							acceptedValues
									.put(command, new ArrayList<String>());
						}
						logger.debug(
								"add value {} in the accepted values for the command {}",
								valuesMatcher.group(1), command);
						acceptedValues.get(command).add(valuesMatcher.group(1));
					}
				}
			}
		}

		public static VControlAvailableCommand getVControlAvailableCommand(
				VControlConnector vcc) {
			// If we change host, then we reload the available commands
			if (singleton != null
					&& (!singleton.host.equals(vcc.s.getInetAddress()) || singleton.port != vcc.s
							.getPort())) {
				singleton = null;
			}
			if (singleton == null) {
				singleton = new VControlAvailableCommand(vcc);
			}
			return singleton;
		}

		public boolean isAvailable(String command) {
			if (availableCommands != null) {
				return availableCommands.keySet().contains(command);
			} else {
				return false;
			}
		}

		public String getUnit(String command) {
			return commandUnit.containsKey(command) ? commandUnit.get(command)
					: "";
		}

		public List<String> getAcceptedValues(String command) {
			return acceptedValues.containsKey(command) ? acceptedValues
					.get(command) : null;
		}

		public Map<String, String> getAvailableCommands() {
			return availableCommands;
		}

	}

}
