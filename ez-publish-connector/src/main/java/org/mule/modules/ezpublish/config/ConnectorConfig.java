package org.mule.modules.ezpublish.config;

import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.TestConnectivity;
import org.mule.modules.ezpublish.client.EzClient;
import org.mule.modules.ezpublish.exception.EzPublishConnectorException;

@Configuration(friendlyName = "Configuration")
public class ConnectorConfig {

	@Configurable
	@Default("https")
	@Placement(group = "Connection", order = 1)
    private String protocol;
	
	@Configurable
	@Default("ezpub.p.aws.economist.com")
	@Placement(group = "Connection", order = 2)
    private String host;
		
	@Configurable
	@Placement(group = "Authentication", order = 1)
    private String username;
	
	@Configurable
	@Placement(group = "Authentication", order = 2)
	@Password
    private String password;
	
	@TestConnectivity
	public void testConnection() throws ConnectionException {
	  EzClient ezClient = new EzClient(this);
	  try {
		ezClient.testConnection();
	  } catch (EzPublishConnectorException e) {
		  throw new ConnectionException(ConnectionExceptionCode.CANNOT_REACH, null, e.getMessage());
	  }
	  // if we manage to get here, it means that the connection was 
	  // successful, hence, no need to return a boolean
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}