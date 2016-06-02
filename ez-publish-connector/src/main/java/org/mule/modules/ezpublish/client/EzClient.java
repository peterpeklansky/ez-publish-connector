package org.mule.modules.ezpublish.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.mule.modules.ezpublish.EzConstant;
import org.mule.modules.ezpublish.config.ConnectorConfig;
import org.mule.modules.ezpublish.exception.EzPublishConnectorException;
import org.mule.modules.ezpublish.model.EzContentObjectsResponse;
import org.mule.modules.ezpublish.model.EzContentTypeResponse;
import org.mule.modules.ezpublish.model.EzField;
import org.mule.modules.ezpublish.model.EzFieldDefinition;
import org.mule.modules.ezpublish.model.EzLocationsResponse;
import org.mule.modules.ezpublish.model.response.Product;
import org.mule.modules.ezpublish.model.response.ProductTerm;
import org.mule.modules.ezpublish.model.response.Ratecard;
import org.mule.modules.ezpublish.model.response.RatecardItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;

@SuppressWarnings("unchecked")
public class EzClient {
	
	private transient final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private static final String API_URL_PATH = "/api/ezp/v2";
	private static final String CONTENT_OBJECT_GET_PATH = "/content/objects/";
	private static final String HTTP_APPLICATION_VND_EZ_API_CONTENT_JSON = "application/vnd.ez.api.Content+json";
	private static final String HTTP_APPLICATION_VND_EZ_API_CONTENT_TYPE_LIST_JSON = "application/vnd.ez.api.ContentTypeList+json";
	private static final String HTTP_APPLICATION_VND_EZ_API_LOCATION_JSON = "application/vnd.ez.api.Location+json";
	private static final String HTTP_ACCEPT = "Accept";
	private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.SSSSSS";
	
    private static ObjectMapper jsonObjectMapper;
	private WebResource webResource;
	private SimpleDateFormat df;
	
	public EzClient(ConnectorConfig connectorConfig) {
		df = new SimpleDateFormat(DATE_FORMAT);
        ClientConfig clientConfig = new DefaultClientConfig();
			
		clientConfig.getClasses().add(JacksonJaxbJsonProvider.class);
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		jsonObjectMapper = new ObjectMapper();
		jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
		Client client = Client.create(clientConfig);
		// authentication
		client.addFilter(new HTTPBasicAuthFilter(connectorConfig.getUsername(), connectorConfig.getPassword()));
		this.webResource = client.resource(connectorConfig.getProtocol() + "://"+ connectorConfig.getHost() + API_URL_PATH);
    }
	
	/**
	 * Method for retrieving Ratecard data from EzPublish.
	 * 
	 * @param id
	 * @return
	 * @throws EzPublishConnectorException
	 */
	public Map<String, Object> getRatecardById(String id) throws EzPublishConnectorException {
		Map<String, Object> map = new HashMap<String, Object>();
		EzContentObjectsResponse response = getObjectFromEz(id);
		
		try {
			if (response != null) {
				Ratecard ratecard = new Ratecard();
				ratecard.setId(String.valueOf(response.getContent().getId()));
				List<EzField> ezFields = response.getContent().getCurrentVersion().getVersion().getFields().getField();
				for (EzField field : ezFields) {
					if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
						ratecard.setName((String)field.getFieldValue());
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
						ratecard.setCode((String)field.getFieldValue());
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.START_DATE)) {
						Object fieldValue = field.getFieldValue();
						if (fieldValue instanceof String) {
							ratecard.setStartDate((String)fieldValue);
						} else if (fieldValue instanceof Map) {
							Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
							Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
							if (timestamp != null) {
								Date startDate = new Date((int)timestamp*1000l);
								ratecard.setStartDate(df.format(startDate));
							}
						}
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.END_DATE)) {
						Object fieldValue = field.getFieldValue();
						if (fieldValue instanceof String) {
							ratecard.setEndDate((String)fieldValue);
						} else if (fieldValue instanceof Map) {
							Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
							Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
							if (timestamp != null) {
								Date endDate = new Date((int)timestamp*1000l);
								ratecard.setEndDate(df.format(endDate));
							}
						}
					}					
				}
				map = convertJsonPojoToMap(ratecard);
			}
		} catch (IOException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return map;
	}
	
	/**
	 * Method to retrieve RatecardItem data from EzPublish, including all referenced objects like ProductTerm, Product ...
	 * 
	 * @param id
	 * @return
	 * @throws EzPublishConnectorException
	 */
	public Map<String, Object> getRatecardItemById(String id) throws EzPublishConnectorException {
		Map<String, Object> map = new HashMap<String, Object>();
		// first get data for RatecardItem
		EzContentObjectsResponse response = getObjectFromEz(id);
		
		try {
			if (response != null) {
				RatecardItem ratecardItem = new RatecardItem();
				ratecardItem.setId(String.valueOf(response.getContent().getId()));
				List<EzField> ezFields = response.getContent().getCurrentVersion().getVersion().getFields().getField();
				int followUpTermId = 0;
				int priceZoneId = 0;
				int productTermId = 0;
				int productTermTermId = 0;
				ArrayList<Integer> featureIds = new ArrayList<Integer>();
				for (EzField field : ezFields) {
					if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
						ratecardItem.setName((String)field.getFieldValue());
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
						ratecardItem.setCode((String)field.getFieldValue());
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.INITIAL_RATE)) {
						ratecardItem.setInitialRate(new BigDecimal((Integer)field.getFieldValue()));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.AUTO_RENEW)) {
						ratecardItem.setAutoRenew((boolean)field.getFieldValue());
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.FOLLOWUP_RATE)) {
						ratecardItem.setFollowUpRate(new BigDecimal((Integer)field.getFieldValue()));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.FOLLOWUP_TERM)) {
						Map<String, Object> fieldMap = (Map<String, Object>)field.getFieldValue();
						followUpTermId = (Integer)fieldMap.get(EzConstant.DESTINATION_CONTENT_ID);
					}	else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.PRICE_ZONE)) {
						Map<String, Object> fieldMap = (Map<String, Object>)field.getFieldValue();
						priceZoneId = (Integer)fieldMap.get(EzConstant.DESTINATION_CONTENT_ID);
					}	else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.PRODUCT_TERM)) {
						Map<String, Object> fieldMap = (Map<String, Object>)field.getFieldValue();
						productTermId = (Integer)fieldMap.get(EzConstant.DESTINATION_CONTENT_ID);
					}						
				}
				// get data for FollowupTerm
				int followUpTermUnitFieldKey = 0;
				EzContentObjectsResponse followUpTermObject = getObjectFromEz(String.valueOf(followUpTermId));
				if (followUpTermObject != null) {
					List<EzField> ezFieldsFollowUpTerm = followUpTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
					for (EzField field : ezFieldsFollowUpTerm) {
						if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.QUANTITY)) {
							ratecardItem.setFollowUpTermQuantity(String.valueOf((Integer)field.getFieldValue()));
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
							ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
							followUpTermUnitFieldKey = listFieldValue.get(0);  // only 1 selection allowed
						}
					}
					String contentTypePath = followUpTermObject.getContent().getContentType().getHref();
					String path = contentTypePath.replace(API_URL_PATH, "");
					// get field definitions and find term "unit"
					EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
					if (contentTypeObject != null) {
						List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
						for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
							if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
								ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
								ratecardItem.setFollowUpTermUnit(options.get(followUpTermUnitFieldKey));
								break;
							}
						}
					}
				}
				
				// get data for PriceZone
				EzContentObjectsResponse priceZoneObject = getObjectFromEz(String.valueOf(priceZoneId));
				int priceZoneCurrencyFieldKey = 0;
				int priceZoneDispatchFieldKey = 0;
				int priceZoneRegionFieldKey = 0;
				if (priceZoneObject != null) {
					List<EzField> ezFieldsPriceZone = priceZoneObject.getContent().getCurrentVersion().getVersion().getFields().getField();
					for (EzField field : ezFieldsPriceZone) {
						if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
							ratecardItem.setPriceZoneName((String)field.getFieldValue());
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CURRENCY)) {
							ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
							priceZoneCurrencyFieldKey = listFieldValue.get(0);  // only 1 selection allowed
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.DISPATCH)) {
							ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
							priceZoneDispatchFieldKey = listFieldValue.get(0);  // only 1 selection allowed
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.REGION)) {
							ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
							priceZoneRegionFieldKey = listFieldValue.get(0);  // only 1 selection allowed
						} 
					}
					String contentTypePath = priceZoneObject.getContent().getContentType().getHref();
					String path = contentTypePath.replace(API_URL_PATH, "");
					// get field definitions and find "currency", "dispatch" and "region"
					EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
					if (contentTypeObject != null) {
						List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
						for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
							if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.CURRENCY)) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
								ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
								ratecardItem.setPriceZoneCurrency(options.get(priceZoneCurrencyFieldKey));
							} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.DISPATCH)) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
								ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
								ratecardItem.setPriceZoneDispatch(options.get(priceZoneDispatchFieldKey));
							} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.REGION)) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
								ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
								ratecardItem.setPriceZoneRegion(options.get(priceZoneRegionFieldKey));
							}
						}
					}
				}
				
				// get data for ProductTerm
				EzContentObjectsResponse productTermObject = getObjectFromEz(String.valueOf(productTermId));
				if (productTermObject != null) {
					ProductTerm productTerm = new ProductTerm();
					productTerm.setId(String.valueOf(productTermId));
					List<EzField> ezFieldsProductTerm = productTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
					for (EzField field : ezFieldsProductTerm) {
						if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
							productTerm.setName((String)field.getFieldValue());
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
							productTerm.setCode((String)field.getFieldValue());
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.EFFECTIVE_FROM_DATE)) {
							Object fieldValue = field.getFieldValue();
							if (fieldValue instanceof String) {
								productTerm.setEffectiveFromDate((String)fieldValue);
							} else if (fieldValue instanceof Map) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
								Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
								if (timestamp != null) {
									Date startDate = new Date((int)timestamp*1000l);
									productTerm.setEffectiveFromDate(df.format(startDate));
								}
							}
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.EFFECTIVE_TO_DATE)) {
							Object fieldValue = field.getFieldValue();
							if (fieldValue instanceof String) {
								productTerm.setEffectiveToDate((String)fieldValue);
							} else if (fieldValue instanceof Map) {
								Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
								Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
								if (timestamp != null) {
									Date endDate = new Date((int)timestamp*1000l);
									productTerm.setEffectiveToDate(df.format(endDate));
								}
							}
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.TERM)) {
							Map<String, Object> fieldMap = (Map<String, Object>)field.getFieldValue();
							productTermTermId = (Integer)fieldMap.get(EzConstant.DESTINATION_CONTENT_ID);
						}	
					}
					// get data for Term
					int productTermTermUnitFieldKey = 0;
					EzContentObjectsResponse productTermTermObject = getObjectFromEz(String.valueOf(productTermTermId));
					if (productTermTermObject != null) {
						List<EzField> ezFieldsProductTermTerm = productTermTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
						for (EzField field : ezFieldsProductTermTerm) {
							if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.QUANTITY)) {
								productTerm.setInitialTermQuantity(String.valueOf((Integer)field.getFieldValue()));
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
								ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
								productTermTermUnitFieldKey = listFieldValue.get(0);  // only 1 selection allowed
							}
						}
						String contentTypePath = productTermTermObject.getContent().getContentType().getHref();
						String path = contentTypePath.replace(API_URL_PATH, "");
						// get field definitions and find product term "unit"
						EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
						if (contentTypeObject != null) {
							List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
							for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
								if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									productTerm.setInitialTermUnit(options.get(productTermTermUnitFieldKey));
									break;
								}
							}
						}
					}
					
					// get data for Product
					// the only reference from ProductTerm back to Product is from "MainLocation path"
					String mainLocationHref = productTermObject.getContent().getMainLocation().getHref();
					// filter only "parent" (Product node_id) from path
					String locationPath = mainLocationHref.replace(API_URL_PATH, "");
					locationPath = locationPath.substring(0, locationPath.lastIndexOf("/"));
					EzLocationsResponse ezLocation = getLocationFromEz(locationPath);
					int productId = ezLocation.getLocation().getConetntInfo().getContent().getId();
					EzContentObjectsResponse productObject = getObjectFromEz(String.valueOf(productId));
					int productTaxProfileFieldKey = 0;
					int productTypeFieldKey = 0;
					ArrayList<Integer> productOldCodeFieldKeys = new ArrayList<>();
					if (productObject != null) {
						Product product = new Product();
						product.setId(String.valueOf(productId));
						List<EzField> ezFieldsProduct = productObject.getContent().getCurrentVersion().getVersion().getFields().getField();
						for (EzField field : ezFieldsProduct) {
							if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
								product.setName((String)field.getFieldValue());
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
								product.setCode((String)field.getFieldValue());
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.START_DATE)) {
								Object fieldValue = field.getFieldValue();
								if (fieldValue instanceof String) {
									product.setStartDate((String)fieldValue);
								} else if (fieldValue instanceof Map) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
									Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
									if (timestamp != null) {
										Date startDate = new Date((int)timestamp*1000l);
										product.setStartDate(df.format(startDate));
									}
								}
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.END_DATE)) {
								Object fieldValue = field.getFieldValue();
								if (fieldValue instanceof String) {
									product.setEndDate((String)fieldValue);
								} else if (fieldValue instanceof Map) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldValue;
									Object timestamp = fieldMap.get(EzConstant.TIMESTAMP);
									if (timestamp != null) {
										Date endDate = new Date((int)timestamp*1000l);
										product.setEndDate(df.format(endDate));
									}
								}
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.FEATURES)) {
								Map<String, Object> fieldMap = (Map<String, Object>)field.getFieldValue();
								featureIds = (ArrayList<Integer>)fieldMap.get(EzConstant.DESTINATION_CONTENT_IDS);
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.TAX_PROFILE)) {
								ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
								productTaxProfileFieldKey = listFieldValue.get(0);  // only 1 selection allowed
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.TYPE)) {
								ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
								productTypeFieldKey = listFieldValue.get(0);  // only 1 selection allowed
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.OLD_CODE)) {
								ArrayList<Integer> listFieldValue = (ArrayList<Integer>)field.getFieldValue();
								productOldCodeFieldKeys = listFieldValue;  // multi selection allowed
							} 
						}
						String contentTypePath = productObject.getContent().getContentType().getHref();
						String path = contentTypePath.replace(API_URL_PATH, "");
						// get field definitions and find "currency", "dispatch" and "region"
						EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
						if (contentTypeObject != null) {
							List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
							for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
								if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.TAX_PROFILE)) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									product.setTaxProfile(options.get(productTaxProfileFieldKey));
								} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.TYPE)) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									product.setType(options.get(productTypeFieldKey));
								} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.OLD_CODE)) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									List<String> legacyCodes = new ArrayList<String>();
									for (Integer key : productOldCodeFieldKeys) {
										legacyCodes.add(options.get(key));
									}
									product.setLegacyCodes(legacyCodes); 
								}
							}
						}
						
						// get data for each Feature
						ArrayList<String> featureList = new ArrayList<>();
						for (Integer featureId : featureIds) {
							EzContentObjectsResponse featureObject = getObjectFromEz(featureId.toString());
							if (featureObject != null) {
								List<EzField> ezFieldsFeature = featureObject.getContent().getCurrentVersion().getVersion().getFields().getField();
								for (EzField featureField : ezFieldsFeature) {
									if (featureField.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
										featureList.add((String)featureField.getFieldValue());
										break;
									}
								}
							}
						}
						product.setFeatures(featureList);
						productTerm.setProduct(product);
					}
					
					ratecardItem.setProductTerm(productTerm);
				}
				
				// build final map
				map = convertJsonPojoToMap(ratecardItem);
			}
		} catch (IOException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return map;
	}
	
	/**
	 * Method to retrieve "raw" data from EzPublish for object with given id.
	 * 
	 * @param id
	 * @return
	 * @throws EzPublishConnectorException
	 */
	public Map<String, Object> getRawContentObject(String id) throws EzPublishConnectorException {
		Map<String, Object> map = new HashMap<String, Object>();
		String path = CONTENT_OBJECT_GET_PATH + id;		
		Builder request = webResource.path(path).header(HTTP_ACCEPT, HTTP_APPLICATION_VND_EZ_API_CONTENT_JSON);
		ClientResponse clientResponse = request.get(ClientResponse.class);
		validateResponse(clientResponse);
		String response = clientResponse.getEntity(String.class);
		
		try {
			EzContentObjectsResponse pojo = jsonObjectMapper.readValue(response, EzContentObjectsResponse.class);
			map = convertJsonPojoToMap(pojo);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return map;
	}
	
	/**
	 * Helper method to make a call to EzPublish Standard API "content/objects" endpoint.
	 * 
	 * @param id
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private EzContentObjectsResponse getObjectFromEz(String id) throws EzPublishConnectorException {
		String path = CONTENT_OBJECT_GET_PATH + id;		
		Builder request = webResource.path(path).header(HTTP_ACCEPT, HTTP_APPLICATION_VND_EZ_API_CONTENT_JSON);
		ClientResponse clientResponse = request.get(ClientResponse.class);
		validateResponse(clientResponse);
		//EzContentObjectsResponse response = clientResponse.getEntity(EzContentObjectsResponse.class);
		String response = clientResponse.getEntity(String.class);
		EzContentObjectsResponse responseObj = null;
		
		try {
			responseObj = jsonObjectMapper.readValue(response, EzContentObjectsResponse.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return responseObj;
	}
	
	/**
	 * Helper method to make a call to EzPublish Standard API "content/locations" endpoint.
	 * 
	 * @param path
	 * @throws EzPublishConnectorException
	 */
	private EzLocationsResponse getLocationFromEz(String path) throws EzPublishConnectorException {
		Builder request = webResource.path(path).header(HTTP_ACCEPT, HTTP_APPLICATION_VND_EZ_API_LOCATION_JSON);
		ClientResponse clientResponse = request.get(ClientResponse.class);
		validateResponse(clientResponse);
		//EzLocationsResponse response = clientResponse.getEntity(EzLocationsResponse.class);
		String response = clientResponse.getEntity(String.class);
		EzLocationsResponse responseObj = null;
		
		try {
			responseObj = jsonObjectMapper.readValue(response, EzLocationsResponse.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return responseObj;
	}
	
	/**
	 * Helper method to make a call to EzPublish Standard API "content/types" endpoint.
	 * 
	 * @param path
	 * @throws EzPublishConnectorException
	 */
	private EzContentTypeResponse getContentTypeFromEz(String path) throws EzPublishConnectorException {
		Builder request = webResource.path(path).header(HTTP_ACCEPT, HTTP_APPLICATION_VND_EZ_API_CONTENT_TYPE_LIST_JSON);
		ClientResponse clientResponse = request.get(ClientResponse.class);
		validateResponse(clientResponse);
		String response = clientResponse.getEntity(String.class);
		EzContentTypeResponse responseObj = null;
		
		try {
			responseObj = jsonObjectMapper.readValue(response, EzContentTypeResponse.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException("EzPublish - response processing failed");
		}
		
		return responseObj;
	}
	
	/**
	 * Helper method to transform POJO to key/value Map.
	 * 
	 * @param pojo
	 * @return
	 * @throws IOException
	 */
	private Map<String, Object> convertJsonPojoToMap(Object pojo) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		// first write it as Json String
		String jsonString = jsonObjectMapper.writeValueAsString(pojo);
		// then convert from String to Map
		map = jsonObjectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
		
		return map;
	}
	
	/**
	 * Validates response based on HTTP status.
	 * 
	 * @param clientResponse
	 * @throws EzPublishConnectorException
	 */
	private void validateResponse(ClientResponse clientResponse) throws EzPublishConnectorException {
		int responseStatus = clientResponse.getStatus();
		LOGGER.info(String.format("Getting response with status %s", responseStatus));
		
		if(responseStatus == 500 ){
			throw new EzPublishConnectorException("EzPublish - internal server error");
		} else if(responseStatus != 200){
			throw new EzPublishConnectorException("EzPublish - failed with status: " + responseStatus);
		}
	}

}
