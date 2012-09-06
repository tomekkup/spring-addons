package com.kuprowski.mogile.multipart;

import com.kuprowski.mogile.StorageConfig;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * ********************************************************
 * Copyright: 2012 Tomek Kuprowski
 *
 * License: GPLv2: http://www.gnu.org/licences/gpl.html
 *
 * @author Tomek Kuprowski (tomekkuprowski at gmail dot com)
 * *******************************************************
 */
public class MojiFileItemFactory implements FileItemFactory {

    private StorageConfig storageConfig;

    @Required
    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }
    
    public MojiFileItemFactory() {
        
    }
    
    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new MojiFileItem(fieldName, contentType, isFormField, fileName, storageConfig);
    }
}
