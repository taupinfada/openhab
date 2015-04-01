/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import org.openhab.binding.vclient.VclientBindingConfig;
import org.openhab.binding.vclient.VclientBindingProvider;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Felix CARDON
 * @since 0.1
 */
public class VclientGenericBindingProvider extends
		AbstractGenericBindingProvider implements VclientBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "vclient";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		VclientCommandType commandType = VclientAvailableCommand
				.getVclientAvailableCommand().getCommand(bindingConfig);
		if (commandType == null)
			throw new BindingConfigParseException("BindingConfig '"
					+ bindingConfig + "' is unknow");

		if (!(item.getClass().equals(commandType.getItemClass()))) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', and command '" + bindingConfig
					+ "' matches only with type '"
					+ commandType.getItemClass().getSimpleName() + "'");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		VclientBindingConfig config = parseBindingConfig(bindingConfig, item);
		addBindingConfig(item, config);
	}

	private VclientBindingConfig parseBindingConfig(String bindingConfig,
			Item item) {
		return new VclientBindingConfig(VclientAvailableCommand
				.getVclientAvailableCommand().getCommand(bindingConfig), item);
	}

	@Override
	public VclientBindingConfig getConfig(String itemName) {
		VclientBindingConfig config = (VclientBindingConfig) bindingConfigs
				.get(itemName);
		return config;
	}

}
