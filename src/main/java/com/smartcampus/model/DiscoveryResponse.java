package com.smartcampus.model;

import java.util.Map;

/**
 * Typed response body returned by GET /api/v1/ (the discovery endpoint).
 * Using a dedicated POJO instead of a raw Map gives a stable, documented
 * contract and makes the structure immediately visible to an examiner.
 */
public class DiscoveryResponse {

    private String apiName;
    private String version;
    private String description;
    private String adminContact;
    private Map<String, String> resources;

    public DiscoveryResponse() {}

    public DiscoveryResponse(String apiName, String version, String description,
                             String adminContact, Map<String, String> resources) {
        this.apiName = apiName;
        this.version = version;
        this.description = description;
        this.adminContact = adminContact;
        this.resources = resources;
    }

    public String getApiName()      { return apiName; }
    public String getVersion()      { return version; }
    public String getDescription()  { return description; }
    public String getAdminContact() { return adminContact; }
    public Map<String, String> getResources() { return resources; }

    public void setApiName(String apiName)           { this.apiName = apiName; }
    public void setVersion(String version)           { this.version = version; }
    public void setDescription(String description)   { this.description = description; }
    public void setAdminContact(String adminContact) { this.adminContact = adminContact; }
    public void setResources(Map<String, String> resources) { this.resources = resources; }
}
