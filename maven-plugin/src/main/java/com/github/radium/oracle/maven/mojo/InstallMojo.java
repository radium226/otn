/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.radium.oracle.maven.mojo;

import com.github.radium.oracle.Resource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;

@Mojo(name = "install", requiresProject = false, requiresDirectInvocation = true)
@Execute(goal = "download")
public class InstallMojo extends AbstractMojo {

    @Component
    private RepositorySystem repositorySystem;
    
    @Parameter(property = "resourceBaseDir", defaultValue = "./")
    private File resourceBaseDir;
    
    @Parameter(property = "resource")
    private Resource resource;
    
    @Parameter(readonly=true, defaultValue="${repositorySystemSession}")
    protected RepositorySystemSession repositorySystemSession; 
    
    public InstallMojo() {
        super();
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            String resourceFileName = resource.getFileName();
            String resourceRepositoryCoordinates = resource.getRepositoryCoordinates();
            File resourceFile = new File(resourceBaseDir, resourceFileName);
            Artifact resourceArtifact = new DefaultArtifact(resourceRepositoryCoordinates);
            resourceArtifact = resourceArtifact.setFile(resourceFile);
            InstallRequest installRequest = new InstallRequest();
            installRequest.addArtifact(resourceArtifact);
            this.repositorySystem.install(this.repositorySystemSession, installRequest);
        } catch (InstallationException e) {
            throw new MojoExecutionException("Unable to install... ", e);
        }
    }
    
}
