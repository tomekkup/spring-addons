package com.kuprowski.mogile.multipart;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * ********************************************************
 * Copyright: 2012 Tomek Kuprowski
 *
 * License: GPLv2: http://www.gnu.org/licences/gpl.html
 *
 * @author Tomek Kuprowski (tomekkuprowski at gmail dot com)
 * *******************************************************
 */
public class MojiMultipartResolver extends CommonsMultipartResolver {

    private MojiFileItemFactory mojiFileItemFactory;
    
    public MojiMultipartResolver() {
        super();
    }

    @Required
    public void setMojiFileItemFactory(MojiFileItemFactory mojiFileItemFactory) {
        this.mojiFileItemFactory = mojiFileItemFactory;
    }

    @Override
    protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
        return new FileUpload(mojiFileItemFactory);
    }
}
