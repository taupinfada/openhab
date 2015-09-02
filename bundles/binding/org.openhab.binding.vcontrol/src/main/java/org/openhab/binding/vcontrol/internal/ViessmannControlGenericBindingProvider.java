/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vcontrol.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.vcontrol.ViessmannControlBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Felix CARDON
 * @since 1.7.0
 */
public class ViessmannControlGenericBindingProvider extends
		AbstractGenericBindingProvider implements
		ViessmannControlBindingProvider {

	/**
	 * Artificial command for the exec-in configuration (which has no command
	 * part by definition). Because we use this artificial command we can reuse
	 * the {@link ExecBindingConfig} for both in- and out-configuration.
	 */
	protected static final Command IN_BINDING_KEY = StringType
			.valueOf("IN_BINDING");

	/**
	 * Artificial command to identify that state changes should be taken into
	 * account
	 */
	protected static final Command CHANGED_COMMAND_KEY = StringType
			.valueOf("CHANGED");

	protected static final Command WILDCARD_COMMAND_KEY = StringType
			.valueOf("*");

	/** {@link Pattern} which matches a binding configuration part */
	private static final Pattern BASE_CONFIG_PATTERN = Pattern
			.compile("(<|>)\\[(.*?)\\](\\s|$)");

	/** {@link Pattern} which matches an In-Binding */
	private static final Pattern IN_BINDING_PATTERN = Pattern
			.compile("(.*?)?:(?!//)(\\d*):(.*)");

	/** {@link Pattern} which matches an Out-Binding */
	private static final Pattern OUT_BINDING_PATTERN = Pattern
			.compile("(.*?):(.*)");

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "viessmanncontrol";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		// if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
		// throw new BindingConfigParseException("item '" + item.getName()
		// + "' is of type '" + item.getClass().getSimpleName()
		// +
		// "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		// }
		// we accept all types of items
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		ViessmannControlBindingConfig config = new ViessmannControlBindingConfig();
		config.itemType = item.getClass();

		Matcher matcher = BASE_CONFIG_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {

			if (bindingConfig.startsWith("<") || bindingConfig.startsWith(">")) {
				throw new BindingConfigParseException(
						"Exec binding legacy format cannot start with '<' or '>' ");
			}

			// backward compatibility for old format
			parseLegacyOutBindingConfig(item, bindingConfig, config);

		} else {

			matcher.reset();

			while (matcher.find()) {
				String direction = matcher.group(1);
				String bindingConfigPart = matcher.group(2);

				if (direction.equals("<")) {
					config = parseInBindingConfig(item, bindingConfigPart,
							config);
				} else if (direction.equals(">")) {
					config = parseOutBindingConfig(item, bindingConfigPart,
							config);
				} else {
					throw new BindingConfigParseException(
							"Unknown command given! Configuration must start with '<' or '>' ");
				}
			}
		}

		addBindingConfig(item, config);
	}

	@Override
	public Class<? extends Item> getItemType(String itemName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandLine(String itemName, Command command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandLine(String itemName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRefreshInterval(String itemName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTransformation(String itemName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getInBindingItemNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This is an internal data structure to store information from the binding
	 * config strings and use it to answer the requests to the Exec binding
	 * provider.
	 */
	static class ExecBindingConfig extends
			HashMap<Command, ExecBindingConfigElement> implements BindingConfig {

		/** generated serialVersion UID */
		private static final long serialVersionUID = 6164971643530954095L;
		Class<? extends Item> itemType;
	}

	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author Felix CARDON
	 * @since 1.7.0
	 */
	class ViessmannControlBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
		String command;
		int refreshInterval = 0;
		String transformation = null;

		@Override
		public String toString() {
			return "ExecBindingConfigElement [command=" + command
					+ ", refreshInterval=" + refreshInterval
					+ ", transformation=" + transformation + "]";
		}
	}

	static class ViessmannControlAvailableCommand {

		private static ViessmannControlAvailableCommand singleton = null;
		private String host;
		private int port;
		private Map<String, ViessmannControlCommand> availableCommand;

		private ViessmannControlAvailableCommand(String host, int port) {
			this.host = host;
			this.port = port;
			this.availableCommand = new HashMap<String, ViessmannControlCommand>();
			ViessmannConnector vcc = ViessmannConnectorFactory
					.getConnector(ViessmannConnectorFactory.VCONTROL_CONNECTOR);
			vcc.connect(host, port);
			List<String> commandsList = vcc.getCommands();
			vcc.disconnect();
		}

		public static ViessmannControlAvailableCommand getViessmannControlAvailableCommand(
				String host, int port) {
			// If we change host, then we reload the available commands
			if (singleton != null
					&& (singleton.host != host || singleton.port != port)) {
				singleton = null;
			}
			if (singleton == null) {
				singleton = new ViessmannControlAvailableCommand(host, port);
			}
			return singleton;
		}

		public static ViessmannControlAvailableCommand getViessmannControlAvailableCommand() {
			return singleton;
		}

		public ViessmannControlCommand getCommand(String command) {
			if (availableCommand != null) {
				return availableCommand.get(command);
			} else {
				return null;
			}
		}
	}

	class ViessmannControlCommand implements Command {

		@Override
		public String format(String pattern) {
			return null;
		}

	}

}
