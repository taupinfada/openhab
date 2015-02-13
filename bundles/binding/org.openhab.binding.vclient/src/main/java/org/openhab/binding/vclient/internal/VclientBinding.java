/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.vclient.VclientBindingConfig;
import org.openhab.binding.vclient.VclientBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Felix CARDON
 * @since 0.1
 */
public class VclientBinding extends
		AbstractActiveBinding<VclientBindingProvider> {

	private static final Logger logger = LoggerFactory
			.getLogger(VclientBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is
	 * set in the activate() method and must not be accessed anymore once the
	 * deactivate() method was called or before activate() was called.
	 */
	@SuppressWarnings("unused")
	private BundleContext bundleContext;

	/**
	 * the refresh interval which is used to poll values from the vclient server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	/**
	 * The ip address to connect to vcontrold
	 */
	private String vcontroldIp;

	/**
	 * The port address to connect to vcontrold (optional, defaults to 3002)
	 */
	private int vcontroldPort = 3002;

	public VclientBinding() {
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

		// read further config parameters here ...

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
		return "vclient Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
		VclientConnector vcc = new VclientConnector(vcontroldIp, vcontroldPort);
		vcc.connect();
		for (VclientBindingProvider provider : providers) {
			Collection<String> items = provider.getItemNames();

			for (String itemName : items) {
				VclientBindingConfig bindingConfig = provider
						.getConfig(itemName);
				if (bindingConfig != null) {
					eventPublisher.postUpdate(bindingConfig.item.getName(),
							vcc.getValue(bindingConfig.commandType));
				} else {
					logger.error("Item '" + itemName + "' inconnu");
				}
			}

		}
		vcc.disconnect();
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
