package org.openhab.binding.vcontrol.internal;

import java.util.List;

public interface ViessmannConnector {
	public boolean connect(String serverIp, int serverPort);

	public String getValue(String command);

	public List<String> getCommands();

	public List<String> getDetailCommand(String vCommand);

	public boolean setValue(String command, String valueS);

	public boolean disconnect();
}
