/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vclient.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents all valid commands which could be processed by this binding
 * 
 * @author Félix CARDON
 * @since 0.1.0
 */
public enum VclientCommandType {

	// System info
	// getDevice: identifiant de la régulation
	DEVICE {
		{
			command = "getDevice";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\.*)$");
		}
	},
	// getSystemTime: Date system de la chaudière
	TIME {
		{
			command = "getSystemTime";
			itemClass = DateTimeItem.class;
			typeClass = DateTimeType.class;
			pattern = Pattern.compile("^vctrld>(\\.*)$");
		}
	},

	// Temperature status
	// getTempExt: Température extérieure en degrés Celsius
	TEMP_EXT {
		{
			command = "getTempExt";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempExtMoy: Température extérieure en degrés Celsius
	TEMP_EXT_MOY {
		{
			command = "getTemptExtMoy";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempDepCC2: Température de départ du circuit de chauffage 2
	TEMP_DEP_CC2 {
		{
			command = "getTempDepCC2";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempIntCC2: Température intérieure pour le circuit de chauffage 2
	TEMP_INT_CC2 {
		{
			command = "getTempIntCC2";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempECS: Température eau chaude sanitaire
	TEMP_ECS {
		{
			command = "getTempECS";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// setTempECS: Température eau chaude sanitaire
	SET_TEMP_ECS {
		{
			command = "setTempECS";
			itemClass = StringItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempDechargeECS: Température de décharge de l'eau chaude sanitaire
	TEMP_DEC_ECS {
		{
			command = "getTempDechargeECS";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempFume: Température des gaz d'échappement en degrés Celsius
	TEMP_SMOCKE {
		{
			command = "getTempFume";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},
	// getTempChaudiere: Température de la chaudière en degrès Celsius
	TEMP_BOILER {
		{
			command = "getTempChaudiere";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern
					.compile("^vctrld>(\\-?\\d*\\.\\d*) Grad Celsius$");
		}
	},

	// Status
	// getPompeStatutCC2: Statut de la pompe de circulation du circuit de
	STATE_PUMP_CC2 {
		{
			command = "getPompeStatutCC2";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// getModeCC2: Mode de fonctionnement du circuit de chauffage 2
	STATE_MODE_CC2 {
		{
			command = "getModeCC2";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// setModeCC2: Mode de fonctionnement du circuit de chauffage 2
	SET_STATE_MODE_CC2 {
		{
			command = "setModeCC2";
			itemClass = StringItem.class;
		}
	},
	// getEcoModeCC2: Statut du mode économique du circuit de chauffage 2
	STATE_MODE_ECO_CC2 {
		{
			command = "getEcoModeCC2";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// setEcoModeCC2: Statut du mode économique du circuit de chauffage 2
	SET_STATE_MODE_ECO_CC2 {
		{
			command = "setEcoModeCC2";
			itemClass = SwitchItem.class;
		}
	},
	// getRecModeCC2: Statut du mode réception du circuit de chauffage 2
	STATE_MODE_REC_CC2 {
		{
			command = "getRecModeCC2";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// setRecModeCC2: Statut du mode réception du circuit de chauffage 2
	SET_STATE_MODE_REC_CC2 {
		{
			command = "setRecModeCC2";
			itemClass = SwitchItem.class;
		}
	},
	// getStatutPompeECS: Statut de la pompe d'alimentation de l'eau chaude
	// sanitaire
	STATE_PUMP_ECS {
		{
			command = "getStatutPompeECS";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// getStatutCircuECS: Statut de la pompe de circulation de l'eau chaude
	// sanitaire
	STATE_PUMP_CIR {
		{
			command = "getStatutCircuECS";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// getActChaudiere: Temps d'activation de la chaudière
	TIME_ON {
		{
			command = "getActChaudiere";
			itemClass = StringItem.class;
			typeClass = StringType.class;
			pattern = Pattern.compile("^vctrld>(\\w*)$");
		}
	},
	// getDemarrageChaudiere: Démarrage de la chaudière
	STATE_ON {
		{
			command = "getDemarrageChaudiere";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^vctrld>(\\d*\\.\\d*)$");
		}
	},
	// getBruleur1Heure: Temps d'activation du bruleur 1
	TIME_BURNER_1 {
		{
			command = "getBruleur1Heure";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^vctrld>(\\-?\\d*\\.\\d*) Heures$");
		}
	},
	// getBruleur2Heure: Temps d'activation du bruleur 2
	TIME_BURNER_2 {
		{
			command = "getBruleur2Heure";
			itemClass = NumberItem.class;
			typeClass = DecimalType.class;
			pattern = Pattern.compile("^vctrld>(\\-?\\d*\\.\\d*) Heures$");
		}
	};

	/**
	 * Represents the heatpump command as it will be used in *.items
	 * configuration
	 */
	String command;
	Class<? extends Item> itemClass;
	Class<? extends Type> typeClass;
	Pattern pattern;
	private static final Logger logger = LoggerFactory
			.getLogger(VclientCommandType.class);

	public String getCommand() {
		return command;
	}

	public Class<? extends Item> getItemClass() {
		return itemClass;
	}

	public Class<? extends Type> getTypeClass() {
		return typeClass;
	}

	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * 
	 * @param bindingConfig
	 *            command string e.g. state, temperature_solar_storage,..
	 * @param itemClass
	 *            class to validate
	 * @return true if item class can bound to heatpumpCommand
	 */
	public static boolean validateBinding(VclientCommandType bindingConfig,
			Class<? extends Item> itemClass) {
		boolean ret = false;
		for (VclientCommandType c : VclientCommandType.values()) {
			if (c.getCommand().equals(bindingConfig.getCommand())
					&& c.getItemClass().equals(itemClass)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public static VclientCommandType fromString(String vclientCommand) {

		if ("".equals(vclientCommand)) {
			return null;
		}
		for (VclientCommandType c : VclientCommandType.values()) {

			if (c.getCommand().equals(vclientCommand)) {
				return c;
			}
		}

		throw new IllegalArgumentException("cannot find vclientCommand for '"
				+ vclientCommand + "'");

	}

	public State getType(String value) {
		Class<?>[] types = new Class<?>[] { String.class };
		Constructor<?> ct;
		State st = null;

		try {
			ct = typeClass.getConstructor(types);
			st = (State) ct.newInstance(new Object[] { value });
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			logger.error("unable to instanciate a type.", e);
		}
		return st;
	}
}
