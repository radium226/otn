package com.github.radium.oracle.maven.mojo;

import com.github.radium.oracle.Resource;
import com.github.radium.oracle.ResourceDownloader;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "download", requiresProject = false)
public class DownloadMojo extends AbstractMojo {

    @Parameter(property = "resourceBaseDir", defaultValue = "./")
    private File resourceBaseDir;
    
    @Parameter(property = "user")
    private String user;
    
    @Parameter(property = "password")
    private String password;
    
    @Parameter(property = "resource")
    private Resource resource;
    
    public DownloadMojo() {
        super();
    }
    
    @Override
    public void execute() throws MojoExecutionException {
        try {
            ResourceDownloader resourceDownloader = ResourceDownloader.signIn(this.user, this.password);
            File resourceFile = resourceDownloader.downloadResource(this.resource, resourceBaseDir, new ResourceDownloader.DownloadResourceCallback() {

                @Override
                public void browsingEntryPoint(String entryPointURL) {
                    getLog().info("Browsing " + entryPointURL);
                }

                @Override
                public void signingIn(String user, String password) {
                    getLog().info("Signing in with " + user);
                }

                @Override
                public void acceptingAgreement() {
                    getLog().info("Accepting agreement");
                }

                @Override
                public void downloadingResource(String fileName) {
                    getLog().info("Downloading " + fileName);
                }
            });
            getLog().info("resourceFile=" + resourceFile);
            getLog().info("user = " + user + " / password = " + password);
        } catch (IOException e) {
            throw new MojoExecutionException("Something happened while downloading the resource. ", e);
        }
    }
}
