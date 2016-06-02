package org.mule.modules.ezpublish.config;

import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.Configurable;

@Configuration(friendlyName = "Configuration")
public class ConnectorConfig {

	@Configurable
	@Default("https")
    private String protocol;
	
	@Configurable
	@Default("ezpub.p.aws.economist.com")
    private String host;
		
	@Configurable
    private String username;
	
	@Configurable
    private String password;

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