package org.mule.modules.ezpublish;

import java.util.Map;

import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.modules.ezpublish.client.EzClient;
import org.mule.modules.ezpublish.config.ConnectorConfig;
import org.mule.modules.ezpublish.exception.EzPublishConnectorException;

@Connector(name="ez-publish", friendlyName="EzPublish")
public class EzPublishConnector {

    @Config
    ConnectorConfig config;
    
    private EzClient client;

    @Start
    public void init() {
        setClient(new EzClient(config));
    }
      
    @Processor
    public Map<String, Object> getRatecardById(String id) throws EzPublishConnectorException {
        return client.getRatecardById(id);
    }
    
    @Processor
    public Map<String, Object> getRatecardItemById(String id) throws EzPublishConnectorException {
        return client.getRatecardItemById(id);
    }
    
    @Processor
    public Map<String, Object> getRawContentObjectById(String id) throws EzPublishConnectorException {
        return client.getRawContentObject(id);
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

	public EzClient getClient() {
		return client;
	}

	public void setClient(EzClient client) {
		this.client = client;
	}

}