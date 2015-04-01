package org.openhab.binding.vclient.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.core.items.Item;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VclientAvailableCommand {

	private static final Logger logger = LoggerFactory
			.getLogger(VclientAvailableCommand.class);

	private static VclientAvailableCommand singleton = null;
	private String host;
	private int port;
	private Map<String, VclientCommandType> availableCommand;

	/**
	 * load all commands which are form as following: openHAB_commandName:
	 * (GETTER|SETTER) : bindingCommand [; ITEM : itemClass] [; Type :
	 * typeClass]
	 * 
	 * @param host
	 * @param port
	 */
	@SuppressWarnings("unchecked")
	private VclientAvailableCommand(String host, int port) {
		this.host = host;
		this.port = port;
		this.availableCommand = new HashMap<String, VclientCommandType>();
		VclientConnector vcc = new VclientConnector(host, port);
		Pattern patternCommand = Pattern
				.compile("^(openHAB_[^:]+):\\s*(GETTER|SETTER)\\s*:\\s*([^;\\s]+)\\s*(;\\s*(ITEM)\\s*:\\s*([^;\\s]+)\\s*)?(;\\s*(TYPE)\\s*:\\s*([^;\\s]+)\\s*)?;?");
		vcc.connect();
		List<String> commandsList = vcc.getCommands();
		for (String commandLine : commandsList) {
			Matcher match = patternCommand.matcher(commandLine);
			if (match.find()) {
				VclientCommandType vct;
				if (!availableCommand.containsKey(match.group(3))) {
					vct = new VclientCommandType();
					availableCommand.put(match.group(3), vct);
				} else {
					vct = availableCommand.get(match.group(3));
				}
				if (match.group(2).equals("GETTER")) {
					vct.commandGetter = match.group(1);
				} else {
					vct.commandSetter = match.group(1);
				}
				if (match.group(5) != null) {
					if (match.group(5).equals("ITEM")) {
						try {
							vct.itemClass = (Class<? extends Item>) Class
									.forName("org.openhab.core.library.items."
											+ match.group(6));
						} catch (ClassNotFoundException e) {
							logger.error("Unable to load itemClass 'org.openhab.core.library.items."
									+ match.group(6)
									+ "' for command '"
									+ match.group(1) + "'");
						}
					} else {
						try {
							vct.typeClass = (Class<? extends Type>) Class
									.forName("org.openhab.core.library.types."
											+ match.group(6));
						} catch (ClassNotFoundException e) {
							logger.error("Unable to load typeClass 'org.openhab.core.library.types."
									+ match.group(6)
									+ "' for command '"
									+ match.group(1) + "'");
						}
					}
				}
				if (match.group(8) != null) {
					if (match.group(8).equals("TYPE")) {
						try {
							vct.typeClass = (Class<? extends Type>) Class
									.forName("org.openhab.core.library.types."
											+ match.group(9));
						} catch (ClassNotFoundException e) {
							logger.error("Unable to load typeClass 'org.openhab.core.library.types."
									+ match.group(9)
									+ "' for command '"
									+ match.group(1) + "'");
						}
					}
				}
				List<String> detailsCommand = vcc.getDetailCommand(match
						.group(1));
				Pattern unitPattern = Pattern.compile("Einheit:\\s(.*)");
				for (String detail : detailsCommand) {
					match = unitPattern.matcher(detail);
					if (match.find()) {
						String unit = match.group(1);
						if (unit == null || unit.equals("(null)")
								|| unit.equals("")) {
							unit = "";
						}
						vct.unit = unit;
					}
				}
			}
		}
		vcc.disconnect();
	}

	public static VclientAvailableCommand getVclientAvailableCommand(
			String host, int port) {
		// If we change host, then we reload the available commands
		if (singleton != null
				&& (singleton.host != host || singleton.port != port)) {
			singleton = null;
		}
		if (singleton == null) {
			singleton = new VclientAvailableCommand(host, port);
		}
		return singleton;
	}

	public static VclientAvailableCommand getVclientAvailableCommand() {
		return singleton;
	}

	public VclientCommandType getCommand(String command) {
		if (availableCommand != null) {
			return availableCommand.get(command);
		} else {
			return null;
		}
	}
}
