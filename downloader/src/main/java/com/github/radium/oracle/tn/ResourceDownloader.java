package com.github.radium.oracle.tn;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.io.Files;
import java.io.File;
import static com.github.radium.htmlunit.HtmlUnit.*;
import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

public class ResourceDownloader {
    
    public static interface DownloadResourceCallback {
        
        public static DownloadResourceCallback NONE = new DownloadResourceCallback() {

            @Override
            public void browsingEntryPoint(String entryPointURL) {
                
            }

            @Override
            public void signingIn(String user, String password) {
                
            }

            @Override
            public void acceptingAgreement() {
                
            }

            @Override
            public void downloadingResource(String fileName) {
                
            }
            
        };  
        
        void browsingEntryPoint(String entryPointURL);
        void signingIn(String user, String password);
        void acceptingAgreement();
        void downloadingResource(String fileName);
        
        
    }
    
    private String user; 
    private String password;
    private String entryPointURL;
    
    private ResourceDownloader(String user, String password) {
        super();
        
        this.user = user;
        this.password = password;
    }
    
    public static ResourceDownloader signIn(String user, String password) {
        return new ResourceDownloader(user, password);
    }
    
    public ResourceDownloader browse(String url) {
        this.entryPointURL = url;
        return this;
    }
    
    public void downloadResource(Resource resource, File resourceFile, DownloadResourceCallback callback) throws IOException {
        disableLogging();
        String fileName = resource.getFileName();
        String entryPointURL = resource.getEntryPointURL();
        
        WebClient client = newClient();
        
        callback.browsingEntryPoint(entryPointURL);
        HtmlPage resourceEntryPointPage = browseEntryPointPage(client, resource);
        
        callback.acceptingAgreement();
        acceptAgreement(resourceEntryPointPage);
        
        HtmlPage signInPage = clickOnResource(resourceEntryPointPage, resource);
        
        callback.signingIn(this.user, this.password);
        Page resourceDownloadPage = signIn(signInPage);
        
        callback.downloadingResource(fileName);
        try (InputStream resourceInputStream = download(resourceDownloadPage) ; FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile)) {;
            ByteStreams.copy(resourceInputStream, resourceFileOutputStream);
        } 
    }
    
    private Page signIn(HtmlPage signInPage) throws IOException {
        HtmlElement body = signInPage.getBody();
        HtmlForm loginForm = findFormByName(body, "LoginForm");
        HtmlInput userInput = findInputByName(loginForm, "ssousername");
        userInput.setValueAttribute(this.user);
        HtmlInput passwordInput = findInputByName(loginForm, "password");
        passwordInput.setValueAttribute(this.password);

        HtmlAnchor anchor = ((List<HtmlAnchor>) loginForm.getByXPath("//a")).stream()
            .filter(new Predicate<HtmlAnchor>() {

                @Override
                public boolean test(HtmlAnchor anchor) {
                    return anchor.getAttribute("class").equals("submit_btn");
                }
                    
            })
            .findFirst().get();

        Page resourcePage = anchor.click();
        return resourcePage;
    }
    
    private HtmlPage clickOnResource(HtmlPage entryPointPage, Resource resource) throws IOException {
        HtmlAnchor anchor = findAnchorByTextContent(entryPointPage, resource.getFileName());
        return anchor.click();
    }
    
    private HtmlPage browseEntryPointPage(WebClient client, Resource resource) throws IOException {
        return browsePage(client, MoreObjects.firstNonNull(this.entryPointURL, resource.getEntryPointURL()));
    }
    
    private void acceptAgreement(HtmlPage page) throws IOException {
        HtmlElement body = page.getBody();
        HtmlForm form = findFormByName(body, "agreementForm");
        HtmlInput input = findInputByName(form, "agreement");
        input.click();
    }
    
    public File downloadResource(Resource resource, DownloadResourceCallback callback) throws IOException {
        File tempParentDir = Files.createTempDir();
        String fileName = resource.getFileName();
        File resourceFile = new File(tempParentDir, fileName);
        downloadResource(resource, resourceFile, callback);
        return resourceFile;
    }
    
    public File downloadResource(Resource resource) throws IOException {
        return downloadResource(resource, ResourceDownloader.DownloadResourceCallback.NONE);
    }
    
    public void downloadResource(Resource resource, File resourceFile) throws IOException {
        downloadResource(resource, resourceFile, ResourceDownloader.DownloadResourceCallback.NONE);
    }
    
}
