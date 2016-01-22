/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vcontrol.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.vcontrol.ViessmannControlBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
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
						"Viessmann binding legacy format cannot start with '<' or '>' ");
			}

			throw new BindingConfigParseException("The binding config "
					+ bindingConfig + " can not be parse for Viessmann binding");

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

	protected ViessmannControlBindingConfig parseInBindingConfig(Item item,
			String bindingConfig, ViessmannControlBindingConfig config)
			throws BindingConfigParseException {

		Matcher matcher = IN_BINDING_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {
			throw new BindingConfigParseException("bindingConfig '"
					+ bindingConfig
					+ "' doesn't represent a valid in-binding-configuration.");
		}
		matcher.reset();

		ViessmannControlBindingConfigElement configElement;

		while (matcher.find()) {
			configElement = new ViessmannControlBindingConfigElement();
			configElement.command = matcher.group(1).replaceAll("\\\\\"", "");
			if (!matcher.group(2).isEmpty()) {
				configElement.refreshInterval = Integer.valueOf(
						matcher.group(2)).intValue();
			} else {
				configElement.refreshInterval = 0;
			}
			configElement.transformation = matcher.group(3).replaceAll(
					"\\\\\"", "\"");
			config.put(IN_BINDING_KEY, configElement);
		}

		return config;
	}

	protected ViessmannControlBindingConfig parseOutBindingConfig(Item item,
			String bindingConfig, ViessmannControlBindingConfig config)
			throws BindingConfigParseException {

		Matcher matcher = OUT_BINDING_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {
			throw new BindingConfigParseException("bindingConfig '"
					+ bindingConfig
					+ "' doesn't represent a valid in-binding-configuration.");
		}
		matcher.reset();

		ViessmannControlBindingConfigElement configElement;

		while (matcher.find()) {
			Command command = createCommandFromString(item, matcher.group(1));

			configElement = new ViessmannControlBindingConfigElement();
			configElement.command = matcher.group(2).replaceAll("\\\\\"", "");

			config.put(command, configElement);
		}

		return config;
	}

	/**
	 * Creates a {@link Command} out of the given <code>commandAsString</code>
	 * taking the special Commands "CHANGED" and "*" into account and
	 * incorporating the {@link TypeParser}.
	 * 
	 * @param item
	 * @param commandAsString
	 * 
	 * @return an appropriate Command (see {@link TypeParser} for more
	 *         information
	 * 
	 * @throws BindingConfigParseException
	 *             if the {@link TypeParser} couldn't create a command
	 *             appropriately
	 * 
	 * @see {@link TypeParser}
	 */
	private Command createCommandFromString(Item item, String commandAsString)
			throws BindingConfigParseException {

		if (CHANGED_COMMAND_KEY.equals(commandAsString)) {
			return CHANGED_COMMAND_KEY;
		} else if (WILDCARD_COMMAND_KEY.equals(commandAsString)) {
			return WILDCARD_COMMAND_KEY;
		} else {
			Command command = TypeParser.parseCommand(
					item.getAcceptedCommandTypes(), commandAsString);

			if (command == null) {
				throw new BindingConfigParseException(
						"couldn't create Command from '" + commandAsString
								+ "' ");
			}

			return command;
		}
	}

	public Class<? extends Item> getItemType(String itemName) {
		ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.itemType : null;
	}

	public String getCommandLine(String itemName, Command command) {
		try {
			ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
					.get(itemName);
			return config != null ? config.get(command).command : null;
		} catch (NullPointerException e) {
			return null;
		}
	}

	public String getCommandLine(String itemName) {
		ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
				.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config
				.get(IN_BINDING_KEY).command : null;
	}

	public int getRefreshInterval(String itemName) {
		ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
				.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config
				.get(IN_BINDING_KEY).refreshInterval : 0;
	}

	public String getTransformation(String itemName) {
		ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
				.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config
				.get(IN_BINDING_KEY).transformation : null;
	}

	public Pattern getExtractValuePattern(String itemName) {
		ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
				.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config
				.get(IN_BINDING_KEY).extractValuePattern : null;
	}

	public List<String> getInBindingItemNames() {
		List<String> inBindings = new ArrayList<String>();
		for (String itemName : bindingConfigs.keySet()) {
			ViessmannControlBindingConfig config = (ViessmannControlBindingConfig) bindingConfigs
					.get(itemName);
			if (config.containsKey(IN_BINDING_KEY)) {
				inBindings.add(itemName);
			}
		}
		return inBindings;
	}

	/**
	 * This is an internal data structure to store information from the binding
	 * config strings and use it to answer the requests to the Exec binding
	 * provider.
	 */
	static class ViessmannControlBindingConfig extends
			HashMap<Command, ViessmannControlBindingConfigElement> implements
			BindingConfig {

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
	class ViessmannControlBindingConfigElement implements BindingConfig {
		// put member fields here which holds the parsed values
		String command;
		/** RegEx to extract the value */
		Pattern extractValuePattern = null;
		int refreshInterval = 0;
		String transformation = null;

		@Override
		public String toString() {
			return "ViessmannControlBindingConfig [command=" + command
					+ ", extractValuePattern=" + extractValuePattern
					+ ", refreshInterval=" + refreshInterval
					+ ", transformation=" + transformation + "]";
		}
	}
}
