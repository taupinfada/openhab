package org.openhab.binding.vcontrol.internal;

public class ViessmannConnectorFactory {

	public static final int VCONTROL_CONNECTOR = 1;

	public static ViessmannConnector getConnector(int type) {
		switch (type) {
		case VCONTROL_CONNECTOR:
		default:
			return VControlConnector.getVcontrolConnector();
		}
	}
}
