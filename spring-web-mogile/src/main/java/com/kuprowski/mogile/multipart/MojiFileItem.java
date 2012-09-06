package com.kuprowski.mogile.multipart;

import com.kuprowski.mogile.StorageConfig;
import fm.last.moji.MojiFile;
import fm.last.moji.spring.SpringMojiBean;
import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

/**
 * ********************************************************
 * Copyright: 2012 Tomek Kuprowski
 *
 * License: GPLv2: http://www.gnu.org/licences/gpl.html
 *
 * @author Tomek Kuprowski (tomekkuprowski at gmail dot com)
 * *******************************************************
 */
public class MojiFileItem implements FileItem {

    public static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * The name of the form field as provided by the browser.
     */
    private String fieldName;
    /**
     * The content type passed by the browser, or
     * <code>null</code> if not defined.
     */
    private String contentType;
    /**
     * Whether or not this item is a simple form field.
     */
    private boolean isFormField;
    /**
     * The original filename in the user's filesystem.
     */
    private String fileName;
    /**
     * The size of the item, in bytes. This is used to cache the size when a
     * file item is moved from its original location.
     */
    private long size = -1;
    private StorageConfig storageConfig;
    private String tempFileName;
    private transient SpringMojiBean moji = null;

    public MojiFileItem(String fieldName, String contentType,
                        boolean isFormField, String fileName, StorageConfig storageConfig) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.storageConfig = storageConfig;
        this.tempFileName = createNewTempName();
    }

    protected SpringMojiBean getMojiClient() {
        if (moji == null) {
            moji = new SpringMojiBean(storageConfig.getAddressesCsv(), storageConfig.getDomain());
            moji.setTestOnBorrow(true);
        }
        return moji;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getMojiFile().getInputStream();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public long getSize() {
        if (size >= 0) {
            return size;
        } else {
            long size = 0;
            try {
                size = getMojiFile().length();
            } catch (IOException ex) {
                Logger.getLogger(MojiFileItem.class.getName()).log(Level.SEVERE, null, ex);
            }
            return size;
        }
    }

    @Override
    public byte[] get() {
        byte[] fileData = new byte[(int) getSize()];
        InputStream fis = null;

        try {
            fis = getInputStream();
            fis.read(fileData);
        } catch (IOException e) {
            fileData = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return fileData;
    }

    @Override
    public String getString(String encoding) throws UnsupportedEncodingException {
        return new String(get(), Charset.forName(encoding));
    }

    @Override
    public String getString() {
        try {
            return getString(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MojiFileItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void write(File file) throws Exception {
        BufferedOutputStream out = null;
        InputStream in = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            in = getMojiFile().getInputStream();
            IOUtils.copy(in, out);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Removes the file contents from the temporary storage.
     */
    @Override
    protected void finalize() throws Exception {
        getMojiFile().delete();

        if (moji != null) {
            moji.close();
        }
    }

    @Override
    public void delete() {
        try {
            getMojiFile().delete();
        } catch (IOException ex) {
            Logger.getLogger(MojiFileItem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public boolean isFormField() {
        return isFormField;
    }

    @Override
    public void setFormField(boolean state) {
        this.isFormField = state;
    }

    protected MojiFile getMojiFile() {
        return getMojiClient().getFile(this.tempFileName, storageConfig.getStorageClass());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getMojiFile().getOutputStream();
    }

    private String createNewTempName() {
        // TODO use better way to find unique UUID
        return UUID.randomUUID().toString() + ".tmp";
    }

    public String getTempFileName() {
        return tempFileName;
    }
    
    
}
