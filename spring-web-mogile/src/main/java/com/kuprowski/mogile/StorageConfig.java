package com.kuprowski.mogile;

import org.springframework.beans.factory.annotation.Required;

/**
 *
 * @author tomek
 */
public class StorageConfig {

    private String addressesCsv;
    private String domain;
    private String storageClass;

    public StorageConfig() {
    }

    public String getAddressesCsv() {
        return addressesCsv;
    }

    @Required
    public void setAddressesCsv(String addressesCsv) {
        this.addressesCsv = addressesCsv;
    }

    public String getDomain() {
        return domain;
    }

    @Required
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStorageClass() {
        return storageClass;
    }

    @Required
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }
}
