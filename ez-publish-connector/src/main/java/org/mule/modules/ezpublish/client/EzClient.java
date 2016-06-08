package org.mule.modules.ezpublish.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.mule.modules.ezpublish.EzConstant;
import org.mule.modules.ezpublish.config.ConnectorConfig;
import org.mule.modules.ezpublish.exception.EzPublishConnectorException;
import org.mule.modules.ezpublish.model.EzContentObjectsResponse;
import org.mule.modules.ezpublish.model.EzContentTypeResponse;
import org.mule.modules.ezpublish.model.EzField;
import org.mule.modules.ezpublish.model.EzFieldDefinition;
import org.mule.modules.ezpublish.model.EzLocationsResponse;
import org.mule.modules.ezpublish.model.response.PriceZone;
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
import com.sun.jersey.client.urlconnection.HTTPSProperties;

@SuppressWarnings("unchecked")
public class EzClient {
	
	private transient final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private static final String API_URL_PATH = "/api/ezp/v2";
	private static final String ROOT_GET_PATH = "/";
	private static final String CONTENT_OBJECT_GET_PATH = "/content/objects/";
	private static final String HTTP_APPLICATION_VND_EZ_API_ROOT_JSON = "application/vnd.ez.api.Root+json";
	private static final String HTTP_APPLICATION_VND_EZ_API_CONTENT_JSON = "application/vnd.ez.api.Content+json";
	private static final String HTTP_APPLICATION_VND_EZ_API_CONTENT_TYPE_LIST_JSON = "application/vnd.ez.api.ContentTypeList+json";
	private static final String HTTP_APPLICATION_VND_EZ_API_LOCATION_JSON = "application/vnd.ez.api.Location+json";
	private static final String HTTP_ACCEPT = "Accept";
	private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.SSSSSS";
	private static final String HTTPS = "https";
	
    private static ObjectMapper jsonObjectMapper;
	private WebResource webResource;
	private SimpleDateFormat df;
	
	public EzClient(ConnectorConfig connectorConfig) {
		df = new SimpleDateFormat(DATE_FORMAT);
        ClientConfig clientConfig = new DefaultClientConfig();
        
        // in case of HTTPS create a trust manager that does not validate certificate chains 
        // TODO: get rid of this if possible, by using valid certificate
        if (connectorConfig.getProtocol().equalsIgnoreCase(HTTPS)) {
	        try {
		        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
		            public X509Certificate[] getAcceptedIssuers(){return null;}
		            public void checkClientTrusted(X509Certificate[] certs, String authType){}
		            public void checkServerTrusted(X509Certificate[] certs, String authType){}
		        }};
		        SSLContext ctx = SSLContext.getInstance("SSL");
		        ctx.init(null, trustAllCerts, new SecureRandom());
		        clientConfig.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				}, ctx));
	        } catch (KeyManagementException | NoSuchAlgorithmException e) {
	        	LOGGER.error(e.getMessage(), e);
	        }
        }
			
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
						ratecard.setName(getFieldValue(field, String.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
						ratecard.setCode(getFieldValue(field, String.class));
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
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
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
		// first get data for RatecardItem itself
		EzContentObjectsResponse rateCardItemObject = getObjectFromEz(id);
		
		try {
			if (rateCardItemObject != null) {
				RatecardItem ratecardItem = new RatecardItem();
				ratecardItem.setId(String.valueOf(rateCardItemObject.getContent().getId()));
				List<EzField> ezFields = rateCardItemObject.getContent().getCurrentVersion().getVersion().getFields().getField();
				int followUpTermId = -1;
				int priceZoneId = -1;
				int productTermId = -1;
				int productTermTermId = -1;
				ArrayList<Integer> featureIds = new ArrayList<Integer>();
				for (EzField field : ezFields) {
					if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
						ratecardItem.setName(getFieldValue(field, String.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
						ratecardItem.setCode(getFieldValue(field, String.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.INITIAL_RATE)) {
						ratecardItem.setInitialRate(getBigDecimalFieldValue(field, Integer.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.AUTO_RENEW)) {
						ratecardItem.setAutoRenew(getFieldValue(field, Boolean.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.FOLLOWUP_RATE)) {
						ratecardItem.setFollowUpRate(getBigDecimalFieldValue(field, Integer.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.FOLLOWUP_TERM)) {
						followUpTermId = getIntegerFieldMapValue(field, Map.class, EzConstant.DESTINATION_CONTENT_ID);
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.PRICE_ZONE)) {
						priceZoneId = getIntegerFieldMapValue(field, Map.class, EzConstant.DESTINATION_CONTENT_ID);
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.PRODUCT_TERM)) {
						productTermId = getIntegerFieldMapValue(field, Map.class, EzConstant.DESTINATION_CONTENT_ID);
					}						
				}
				
				// get data for Ratecard (we need header fields)
				// the only reference from RatacardItem back to Ratecard is from "MainLocation path"
				String rateCardItemMainLocationHref = rateCardItemObject.getContent().getMainLocation().getHref();
				// filter only "parent" (Ratecard node_id) from path
				String ratecardItemLocationPath = rateCardItemMainLocationHref.replace(API_URL_PATH, "");
				ratecardItemLocationPath = ratecardItemLocationPath.substring(0, ratecardItemLocationPath.lastIndexOf("/"));
				EzLocationsResponse ratecardEzLocation = getLocationFromEz(ratecardItemLocationPath);
				int ratecardId = ratecardEzLocation.getLocation().getConetntInfo().getContent().getId();
				EzContentObjectsResponse ratecardObject = getObjectFromEz(String.valueOf(ratecardId));
				if (ratecardObject != null) {
					List<EzField> ezFieldsRatecard = ratecardObject.getContent().getCurrentVersion().getVersion().getFields().getField();
					for (EzField field : ezFieldsRatecard) {
						if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
							ratecardItem.setRateCardHeaderName(getFieldValue(field, String.class));
						} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
							ratecardItem.setRateCardHeaderCode(getFieldValue(field, String.class));
						}
					}
				}
				
				// get data for FollowupTerm
				int followUpTermUnitFieldKey = -1;
				if (followUpTermId != -1) {
					EzContentObjectsResponse followUpTermObject = getObjectFromEz(String.valueOf(followUpTermId));
					if (followUpTermObject != null) {
						List<EzField> ezFieldsFollowUpTerm = followUpTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
						for (EzField field : ezFieldsFollowUpTerm) {
							if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.QUANTITY)) {
								Integer intFieldValue = getFieldValue(field, Integer.class);
								if (intFieldValue != null) {
									ratecardItem.setFollowUpTermQuantity(String.valueOf(intFieldValue));
								}
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
								followUpTermUnitFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
							}
						}
						if (followUpTermUnitFieldKey != -1) {
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
					}
				}
				
				// get data for PriceZone
				if (priceZoneId != -1) {
					EzContentObjectsResponse priceZoneObject = getObjectFromEz(String.valueOf(priceZoneId));
					int priceZoneCurrencyFieldKey = -1;
					int priceZoneDispatchFieldKey = -1;
					int priceZoneRegionFieldKey = -1;
					if (priceZoneObject != null) {
						List<EzField> ezFieldsPriceZone = priceZoneObject.getContent().getCurrentVersion().getVersion().getFields().getField();
						for (EzField field : ezFieldsPriceZone) {
							if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
								ratecardItem.setPriceZoneName(getFieldValue(field, String.class));
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CURRENCY)) {
								priceZoneCurrencyFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.DISPATCH)) {
								priceZoneDispatchFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.REGION)) {
								priceZoneRegionFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
							} 
						}
						String contentTypePath = priceZoneObject.getContent().getContentType().getHref();
						String path = contentTypePath.replace(API_URL_PATH, "");
						// get field definitions and find "currency", "dispatch" and "region"
						EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
						if (contentTypeObject != null) {
							List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
							for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
								if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.CURRENCY) && priceZoneCurrencyFieldKey != -1) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									ratecardItem.setPriceZoneCurrency(options.get(priceZoneCurrencyFieldKey));
								} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.DISPATCH) && priceZoneDispatchFieldKey != -1) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									ratecardItem.setPriceZoneDispatch(options.get(priceZoneDispatchFieldKey));
								} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.REGION) && priceZoneRegionFieldKey != -1) {
									Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
									ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
									ratecardItem.setPriceZoneRegion(options.get(priceZoneRegionFieldKey));
								}
							}
						}
					}
				}
				
				// get data for ProductTerm
				if (productTermId != -1) {
					EzContentObjectsResponse productTermObject = getObjectFromEz(String.valueOf(productTermId));
					if (productTermObject != null) {
						ProductTerm productTerm = new ProductTerm();
						productTerm.setId(String.valueOf(productTermId));
						List<EzField> ezFieldsProductTerm = productTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
						for (EzField field : ezFieldsProductTerm) {
							if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
								productTerm.setName(getFieldValue(field, String.class));
							} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
								productTerm.setCode(getFieldValue(field, String.class));
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
								productTermTermId = getIntegerFieldMapValue(field, Map.class, EzConstant.DESTINATION_CONTENT_ID);
							}	
						}
						// get data for Term
						if (productTermTermId != -1) {
							int productTermTermUnitFieldKey = -1;
							EzContentObjectsResponse productTermTermObject = getObjectFromEz(String.valueOf(productTermTermId));
							if (productTermTermObject != null) {
								List<EzField> ezFieldsProductTermTerm = productTermTermObject.getContent().getCurrentVersion().getVersion().getFields().getField();
								for (EzField field : ezFieldsProductTermTerm) {
									if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.QUANTITY)) {
										Integer intFieldValue = getFieldValue(field, Integer.class);
										if (intFieldValue != null) {
											productTerm.setInitialTermQuantity(String.valueOf(intFieldValue));
										}
									} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.UNIT)) {
										productTermTermUnitFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
									}
								}
								if (productTermTermUnitFieldKey != -1) {
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
						int productTaxProfileFieldKey = -1;
						int productTypeFieldKey = -1;
						ArrayList<Integer> productOldCodeFieldKeys = new ArrayList<>();
						if (productObject != null) {
							Product product = new Product();
							product.setId(String.valueOf(productId));
							List<EzField> ezFieldsProduct = productObject.getContent().getCurrentVersion().getVersion().getFields().getField();
							for (EzField field : ezFieldsProduct) {
								if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
									product.setName(getFieldValue(field, String.class));
								} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CODE)) {
									product.setCode(getFieldValue(field, String.class));
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
									featureIds = getIntegerListFieldMapValue(field, Map.class, EzConstant.DESTINATION_CONTENT_IDS);
								} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.TAX_PROFILE)) {
									productTaxProfileFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
								} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.TYPE)) {
									productTypeFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
								} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.LEGACY_CODES)) {
									productOldCodeFieldKeys = getMultiKeyForFieldDefinition(field); // multi selection allowed
								} 
							}
							String contentTypePath = productObject.getContent().getContentType().getHref();
							String path = contentTypePath.replace(API_URL_PATH, "");
							// get field definitions and find "tax profile", "legacy codes" and "product type"
							EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
							if (contentTypeObject != null) {
								List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
								for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
									if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.TAX_PROFILE) && productTaxProfileFieldKey != -1) {
										Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
										ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
										product.setTaxProfile(options.get(productTaxProfileFieldKey));
									} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.TYPE) && productTypeFieldKey != -1) {
										Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
										ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
										product.setType(options.get(productTypeFieldKey));
									} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.LEGACY_CODES)) {
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
											featureList.add(getFieldValue(featureField, String.class));
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
				}
				
				// build final map
				map = convertJsonPojoToMap(ratecardItem);
			}
		} catch (IOException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
		}
		
		return map;
	}
	
	/**
	 * Method for retrieving PriceZone data from EzPublish.
	 * 
	 * @param id
	 * @return
	 * @throws EzPublishConnectorException
	 */
	public Map<String, Object> getPriceZoneById(String id) throws EzPublishConnectorException {
		Map<String, Object> map = new HashMap<String, Object>();
		EzContentObjectsResponse priceZoneObject = getObjectFromEz(id);
		
		try {
			if (priceZoneObject != null) {
				PriceZone priceZone = new PriceZone();
				int priceZoneCurrencyFieldKey = -1;
				int priceZoneDispatchFieldKey = -1;
				int priceZoneRegionFieldKey = -1;
				priceZone.setId(String.valueOf(priceZoneObject.getContent().getId()));
				List<EzField> ezFieldsPriceZone = priceZoneObject.getContent().getCurrentVersion().getVersion().getFields().getField();
				for (EzField field : ezFieldsPriceZone) {
					if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.NAME)) {
						priceZone.setName(getFieldValue(field, String.class));
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.CURRENCY)) {
						priceZoneCurrencyFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.DISPATCH)) {
						priceZoneDispatchFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
					} else if (field.getFieldDefinitionIdentifier().equalsIgnoreCase(EzConstant.REGION)) {
						priceZoneRegionFieldKey = getSingleKeyForFieldDefinition(field); // only 1 selection allowed
					} 
				}
				String contentTypePath = priceZoneObject.getContent().getContentType().getHref();
				String path = contentTypePath.replace(API_URL_PATH, "");
				// get field definitions and find "currency", "dispatch" and "region"
				EzContentTypeResponse contentTypeObject = getContentTypeFromEz(path);
				if (contentTypeObject != null) {
					List<EzFieldDefinition> ezFieldDefinitions = contentTypeObject.getContentType().getFieldDefinitions().getFieldDefinition();
					for (EzFieldDefinition fieldDefinition: ezFieldDefinitions) {
						if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.CURRENCY) && priceZoneCurrencyFieldKey != -1) {
							Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
							ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
							priceZone.setCurrency(options.get(priceZoneCurrencyFieldKey));
						} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.DISPATCH) && priceZoneDispatchFieldKey != -1) {
							Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
							ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
							priceZone.setDispatch(options.get(priceZoneDispatchFieldKey));
						} else if (fieldDefinition.getIdentifier().equalsIgnoreCase(EzConstant.REGION) && priceZoneRegionFieldKey != -1) {
							Map<String, Object> fieldMap = (Map<String, Object>)fieldDefinition.getFieldSettings();
							ArrayList<String> options = (ArrayList<String>)fieldMap.get(EzConstant.OPTIONS);
							priceZone.setRegion(options.get(priceZoneRegionFieldKey));
						}
					}
				}				
				
				map = convertJsonPojoToMap(priceZone);
			}
		} catch (IOException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
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
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
		}
		
		return map;
	}
	/**
	 * Method used for testing the connection. It retrieves root resources.
	 * 
	 * @return
	 * @throws EzPublishConnectorException
	 */
	public String testConnection() throws EzPublishConnectorException {
		String path = ROOT_GET_PATH;		
		Builder request = webResource.path(path).header(HTTP_ACCEPT, HTTP_APPLICATION_VND_EZ_API_ROOT_JSON);
		ClientResponse clientResponse = request.get(ClientResponse.class);
		validateResponse(clientResponse);
		String response = clientResponse.getEntity(String.class);
		return response;
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
		String response = clientResponse.getEntity(String.class);
		EzContentObjectsResponse responseObj = null;
		
		try {
			responseObj = jsonObjectMapper.readValue(response, EzContentObjectsResponse.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
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
		String response = clientResponse.getEntity(String.class);
		EzLocationsResponse responseObj = null;
		
		try {
			responseObj = jsonObjectMapper.readValue(response, EzLocationsResponse.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
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
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED);
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
		LOGGER.info(EzConstant.EZPUBLISH_API + " - " + clientResponse.toString());
		
		if(responseStatus == 500 ){
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API + " - internal server error");
		} else if(responseStatus != 200){
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API + " - failed with status: " + responseStatus);
		}
	}
	
	/**
	 * Helper method to get value of the field and cast it to specific type.
	 * 
	 * @param field
	 * @param responseClass
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private <T> T getFieldValue(EzField field, Class<T> responseClass) throws EzPublishConnectorException {
		T result = null;
		try {
			Object fieldValue = field.getFieldValue();
			if (fieldValue != null) {
				result = (T)fieldValue;
			}
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED + ": " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * Helper method to get value of the field and "transform" it to BigDecimal.
	 * 
	 * @param field
	 * @param responseClass
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private <T> BigDecimal getBigDecimalFieldValue(EzField field, Class<T> responseClass) throws EzPublishConnectorException {
		T value = getFieldValue(field, responseClass);
		if (value != null) {
			if (value instanceof Integer) {
				return new BigDecimal((Integer)value);
			} else if (value instanceof Long) {
				return new BigDecimal((Long)value);
			} else return null;
		} else return null;
	}
	
	/**
	 * Helper method to get value of the field as Map and return item with given fieldKey.
	 * 
	 * @param field
	 * @param responseClass
	 * @param fieldKey
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private <T> Integer getIntegerFieldMapValue(EzField field, Class<T> responseClass, String fieldKey) throws EzPublishConnectorException {
		Integer result = -1;
		Map<String, Object> fieldMap = getFieldValue(field, Map.class);
		try {
			if (fieldMap != null) {
				Object value = fieldMap.get(fieldKey);
				if (value != null) {
					result = (Integer)value;
				}
			}
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED + ": " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * Helper method to get value of the field as Map and return item with given fieldKey.
	 * 
	 * @param field
	 * @param responseClass
	 * @param fieldKey
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private <T> ArrayList<Integer> getIntegerListFieldMapValue(EzField field, Class<T> responseClass, String fieldKey) throws EzPublishConnectorException {
		ArrayList<Integer> result = new ArrayList<>();
		Map<String, Object> fieldMap = getFieldValue(field, Map.class);
		try {
			if (fieldMap != null) {
				Object value = fieldMap.get(fieldKey);
				if (value != null) {
					result = (ArrayList<Integer>)value;
				}
			}
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED + ": " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * Helper method to get field definition key.
	 * 
	 * @param field
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private int getSingleKeyForFieldDefinition(EzField field) throws EzPublishConnectorException {
		int result = -1;
		List list = getFieldValue(field, List.class);
		try {
			ArrayList<Integer> newList = (ArrayList<Integer>)list;
			if (newList != null && newList.size() > 0) {
				return newList.get(0);
			}
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED + ": " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * Helper method to get field definition keys.
	 * 
	 * @param field
	 * @return
	 * @throws EzPublishConnectorException
	 */
	private ArrayList<Integer> getMultiKeyForFieldDefinition(EzField field) throws EzPublishConnectorException {
		ArrayList<Integer> result = new ArrayList<>();
		List list = getFieldValue(field, List.class);
		try {
			ArrayList<Integer> newList = (ArrayList<Integer>)list;
			if (newList != null && newList.size() > 0) {
				return newList;
			}
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EzPublishConnectorException(EzConstant.EZPUBLISH_API_RESPONSE_PROCESSING_FAILED + ": " + e.getMessage());
		}
		return result;
	}

}
