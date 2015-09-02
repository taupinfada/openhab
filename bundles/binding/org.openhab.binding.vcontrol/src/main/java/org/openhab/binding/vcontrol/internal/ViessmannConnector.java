package org.openhab.binding.vcontrol.internal;

import java.util.List;

public interface ViessmannConnector {
	public void connect(String serverIp, int serverPort);
	public String getValue(String command);
	public List<String> getCommands();
	public List<String> getDetailCommand(String vCommand);
	public boolean setValue(String commandStr);
	public void disconnect();
}
