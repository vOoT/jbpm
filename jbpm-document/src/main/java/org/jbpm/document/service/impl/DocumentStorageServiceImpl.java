/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.document.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jbpm.document.Document;
import org.jbpm.document.service.DocumentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * This a Sample Implementation of the DocumentStorageService saves the uploaded files on the File System on a folder (by default /docs)
 * and return the complete path to the file that will be stored in the form field property.
 *
 * Check that the user that is running the app has write permissions on the storage folder.
 */
public class DocumentStorageServiceImpl implements DocumentStorageService {

    private Logger log = LoggerFactory.getLogger(DocumentStorageServiceImpl.class);

    /**
     * This is the root folder where the files are going to be stored, please check that the user that is running the app has permissions to read/write inside
     */
    private String storagePath = ".docs";

    @Override
    public Document saveDocument(String docName, byte[] content) {
        String destinationPath = generateUniquePath(docName);
        File destination = new File(destinationPath);

        try {
            FileUtils.writeByteArrayToFile(destination, content);

            return new DocumentImpl(destinationPath, destination.getName(), destination.length(), new Date(destination.lastModified()));
        } catch (IOException e) {
            log.error("Error writing file {}: {}", docName, e);
        }

        return null;
    }

    @Override
    public Document getDocument(String id) {
        File file = new File(id);

        if (file.exists()) {
            return new DocumentImpl(id, file.getName(), file.length(), new Date(file.lastModified()));
        }

        return null;
    }

    @Override
    public boolean deleteDocument(String id) {
        if (StringUtils.isEmpty(id)) return true;
        return deleteDocument(getDocument(id));
    }

    @Override
    public boolean deleteDocument(Document doc) {
        if (doc != null) {
            return deleteFile(getDocumentContent(doc));
        }
        return true;
    }

    public File getDocumentContent(Document doc) {
        if (doc != null) {
            return new File(doc.getIdentifier());
        }
        return null;
    }

    protected boolean deleteFile(File file) {
        try {
            if (file != null) {
                if (file.isFile()) {
                    file.delete();
                    return deleteFile(file.getParentFile());
                } else {
                    if (!file.getName().equals(storagePath)) {
                        String[] list = file.list();
                        if (list == null || list.length == 0) {
                            file.delete();
                            return deleteFile(file.getParentFile());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error deleting file: ", e);
            return false;
        }
        return true;
    }

    /**
     * Generates a random path to store the file to avoid overwritting files with the same name
     * @param fileName The fileName that is going to be stored
     * @return A String
     */
    protected String generateUniquePath(String fileName) {
        String destinationPath = storagePath + "/";

        destinationPath += UUID.randomUUID().toString();
        destinationPath = destinationPath.replaceAll("-", "/");
        if (!destinationPath.endsWith("/")) destinationPath += "/";

        return destinationPath + "/" + fileName;
    }
}