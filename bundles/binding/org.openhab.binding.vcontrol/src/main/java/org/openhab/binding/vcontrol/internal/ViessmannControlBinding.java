/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vcontrol.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.vcontrol.ViessmannControlBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Felix CARDON
 * @since 1.7.0
 */
public class ViessmannControlBinding extends
		AbstractActiveBinding<ViessmannControlBindingProvider> {

	private static final Logger logger = LoggerFactory
			.getLogger(ViessmannControlBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is
	 * set in the activate() method and must not be accessed anymore once the
	 * deactivate() method was called or before activate() was called.
	 */
	private BundleContext bundleContext;

	/**
	 * The ip adress to connect to Viessman
	 */
	private String vcontroldIp;

	/**
	 * The port adress to connect to
	 */
	private int vcontroldPort = 3002;

	/**
	 * the refresh interval which is used to poll values from the
	 * ViessmannControl server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	private Map<String, Long> lastUpdateMap = new HashMap<String, Long>();

	/** RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code> */
	private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern
			.compile("(.*?)\\((.*)\\)");

	public ViessmannControlBinding() {
		logger.debug("call ViessmannControlBinding");
	}

	/**
	 * Called by the SCR to activate the component with its configuration read
	 * from CAS
	 * 
	 * @param bundleContext
	 *            BundleContext of the Bundle that defines this component
	 * @param configuration
	 *            Configuration properties for this component obtained from the
	 *            ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext,
			final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component
		// definition has the
		// configuration-policy set to require. If set to 'optional' then the
		// configuration may be null

		modified(configuration);
	}

	/**
	 * Called by the SCR when the configuration of a binding has been changed
	 * through the ConfigAdmin service.
	 * 
	 * @param configuration
	 *            Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
		if (configuration == null)
			return;

		String vcontroldIpString = (String) configuration.get("vcontroldIp");
		InetAddress inet;
		try {
			inet = InetAddress.getByName(vcontroldIpString);
		} catch (UnknownHostException e) {
			logger.error("Unknow Host '" + vcontroldIpString + "' : "
					+ e.getMessage());
			return;
		}
		try {
			if (!inet.isReachable(60000)) {
				logger.error("Timeout : Host " + vcontroldIpString
						+ " is unreachable.");
				return;
			}
		} catch (IOException e) {
			logger.error("Host " + vcontroldIpString + " is unreachable : "
					+ e.getMessage());
			return;
		}
		vcontroldIp = vcontroldIpString;

		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

		// to override the default port one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String vcontroldPortIntervalString = (String) configuration
				.get("vcontroldPort");
		if (StringUtils.isNotBlank(vcontroldPortIntervalString)) {
			vcontroldPort = Integer.parseInt(vcontroldPortIntervalString);
		}

		setProperlyConfigured(true);
	}

	/**
	 * Called by the SCR to deactivate the component when either the
	 * configuration is removed or mandatory references are no longer satisfied
	 * or the component has simply been stopped.
	 * 
	 * @param reason
	 *            Reason code for the deactivation:<br>
	 *            <ul>
	 *            <li>0 – Unspecified
	 *            <li>1 – The component was disabled
	 *            <li>2 – A reference became unsatisfied
	 *            <li>3 – A configuration was changed
	 *            <li>4 – A configuration was deleted
	 *            <li>5 – The component was disposed
	 *            <li>6 – The bundle was stopped
	 *            </ul>
	 */
	public void deactivate(final int reason) {
		ViessmannConnectorFactory.getConnector(
				ViessmannConnectorFactory.VCONTROL_CONNECTOR).disconnect();
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "ViessmannControl Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
		ViessmannConnector vc = ViessmannConnectorFactory
				.getConnector(ViessmannConnectorFactory.VCONTROL_CONNECTOR);
		vc.connect(vcontroldIp, vcontroldPort);
		for (ViessmannControlBindingProvider provider : providers) {
			for (String itemName : provider.getInBindingItemNames()) {
				String commandLine = provider.getCommandLine(itemName);
				if (!vc.isAvailableCommand(commandLine)) {
					logger.error("bindingConfig : '" + commandLine
							+ "' doesn't represent a valid command.");
					continue;
				}
				int refreshInterval = provider.getRefreshInterval(itemName);
				String transformation = provider.getTransformation(itemName);

				Long lastUpdateTimeStamp = lastUpdateMap.get(itemName);
				if (lastUpdateTimeStamp == null) {
					lastUpdateTimeStamp = 0L;
				}

				long age = System.currentTimeMillis() - lastUpdateTimeStamp;
				boolean needsUpdate = age >= refreshInterval;

				if (needsUpdate) {

					logger.debug("item '{}' is about to be refreshed now",
							itemName);

					commandLine = String.format(commandLine, Calendar
							.getInstance().getTime(), "", itemName);

					String response = vc.getValue(commandLine);

					if (response == null) {
						logger.error("No response received from command '{}'",
								commandLine);
						lastUpdateMap.put(itemName, System.currentTimeMillis());
						continue;
					}

					String transformedResponse = response;
					// If transformation is needed
					if (transformation.length() > 0)
						transformedResponse = transformResponse(response,
								transformation);

					Class<? extends Item> itemType = provider
							.getItemType(itemName);
					State state = createState(itemType, transformedResponse);

					if (state != null) {
						eventPublisher.postUpdate(itemName, state);
					}

					lastUpdateMap.put(itemName, System.currentTimeMillis());
				}
			}
		}
		vc.disconnect();
	}

	protected String transformResponse(String response, String transformation) {
		String transformedResponse;

		try {
			String[] parts = splitTransformationConfig(transformation);
			String transformationType = parts[0];
			String transformationFunction = parts[1];

			TransformationService transformationService = TransformationHelper
					.getTransformationService(bundleContext, transformationType);
			if (transformationService != null) {
				transformedResponse = transformationService.transform(
						transformationFunction, response);
			} else {
				transformedResponse = response;
				logger.warn(
						"couldn't transform response because transformationService of type '{}' is unavailable",
						transformationType);
			}
		} catch (TransformationException te) {
			logger.error("transformation throws exception [transformation="
					+ transformation + ", response=" + response + "]", te);

			// in case of an error we return the response without any
			// transformation
			transformedResponse = response;
		}

		logger.debug("transformed response is '{}'", transformedResponse);
		return transformedResponse;
	}

	/**
	 * Splits a transformation configuration string into its two parts - the
	 * transformation type and the function/pattern to apply.
	 * 
	 * @param transformation
	 *            the string to split
	 * @return a string array with exactly two entries for the type and the
	 *         function
	 */
	protected String[] splitTransformationConfig(String transformation) {
		Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(transformation);

		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"given transformation function '"
							+ transformation
							+ "' does not follow the expected pattern '<function>(<pattern>)'");
		}
		matcher.reset();

		matcher.find();
		String type = matcher.group(1);
		String pattern = matcher.group(2);

		return new String[] { type, pattern };
	}

	/**
	 * Returns a {@link State} which is inherited from the {@link Item}s
	 * accepted DataTypes. The call is delegated to the {@link TypeParser}. If
	 * <code>item</code> is <code>null</code> the {@link StringType} is used.
	 * 
	 * @param itemType
	 * @param transformedResponse
	 * 
	 * @return a {@link State} which type is inherited by the {@link TypeParser}
	 *         or a {@link StringType} if <code>item</code> is <code>null</code>
	 */
	private State createState(Class<? extends Item> itemType,
			String transformedResponse) {
		try {
			if (itemType.isAssignableFrom(NumberItem.class)) {
				return DecimalType.valueOf(transformedResponse);
			} else if (itemType.isAssignableFrom(ContactItem.class)) {
				return OpenClosedType.valueOf(transformedResponse);
			} else if (itemType.isAssignableFrom(SwitchItem.class)) {
				return OnOffType.valueOf(transformedResponse);
			} else if (itemType.isAssignableFrom(RollershutterItem.class)) {
				return PercentType.valueOf(transformedResponse);
			} else {
				return StringType.valueOf(transformedResponse);
			}
		} catch (Exception e) {
			logger.error("Couldn't create state of type '{}' for value '{}'",
					itemType, transformedResponse);
			return StringType.valueOf(transformedResponse);
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand({},{}) is called!", itemName,
				command);
		ViessmannControlBindingProvider provider = findFirstMatchingBindingProvider(
				itemName, command);

		if (provider == null) {
			logger.warn(
					"doesn't find matching binding provider [itemName={}, command={}]",
					itemName, command);
			return;
		}

		String commandLine = provider.getCommandLine(itemName, command);

		if (commandLine != null && !commandLine.isEmpty()) {

			commandLine = String.format(commandLine, Calendar.getInstance()
					.getTime(), command, itemName);
			ViessmannConnector vc = ViessmannConnectorFactory
					.getConnector(ViessmannConnectorFactory.VCONTROL_CONNECTOR);
			vc.connect(vcontroldIp, vcontroldPort);
			if (!vc.submit(commandLine))
				logger.error("Unable to set {} with the command : '{}'",
						itemName, commandLine);
			vc.disconnect();
		}
	}

	private ViessmannControlBindingProvider findFirstMatchingBindingProvider(
			String itemName, Command command) {

		ViessmannControlBindingProvider firstMatchingProvider = null;

		for (ViessmannControlBindingProvider provider : this.providers) {

			String commandLine = provider.getCommandLine(itemName, command);

			if (commandLine != null) {
				firstMatchingProvider = provider;
				break;
			}
		}

		return firstMatchingProvider;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName,
				newState);
	}
}
