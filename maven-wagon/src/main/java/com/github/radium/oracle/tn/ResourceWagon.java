package com.github.radium.oracle.tn;

import com.github.radium.htmlunit.HtmlUnit;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import com.github.radium.maven.Coordinates;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.io.Files;
import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = Wagon.class, hint = "otn")
public class ResourceWagon extends AbstractWagon {

    final private static Logger LOGGER = LoggerFactory.getLogger(ResourceWagon.class);
    
    public ResourceWagon() {
        super();

    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        
    }

    @Override
    protected void closeConnection() throws ConnectionException {
        
    }

    private Resource findResourceByCoordinates(final Coordinates coordinates) {
        return Iterables.getFirst(Iterables.filter(Arrays.asList(Resource.values()), new Predicate<Resource>() {

            @Override
            public boolean apply(Resource resource) {
                return coordinates.equals(Coordinates.fromText(resource.getCoordinates()));
            }

        }), null);
    }

    public void getJAR(Coordinates coordinates, File localResourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = findResourceByCoordinates(coordinates);
        if (resource == null) {
            throw new ResourceDoesNotExistException("The resource does not exists");
        }

        try {
            String url = "http" + getRepository().getUrl().substring("otn".length());
            String userName = getAuthenticationInfo().getUserName();
            String password = getAuthenticationInfo().getPassword();
            HtmlUnit.disableLogging();
            ResourceDownloader.signIn(userName, password).browse(url).downloadResource(resource, localResourceFile, new ResourceDownloader.DownloadResourceCallback() {

                @Override
                public void browsingEntryPoint(String entryPointURL) {
                    LOGGER.info("Browsing " + entryPointURL);
                }

                @Override
                public void signingIn(String user, String password) {
                    LOGGER.info("Signing in with " + user);
                }

                @Override
                public void acceptingAgreement() {
                    LOGGER.info("Accepting agrement ");
                }

                @Override
                public void downloadingResource(String fileName) {
                    LOGGER.info("Downloading resource " + fileName);
                }
            });
        } catch (IOException e) {
            e.printStackTrace(System.err);
            throw new TransferFailedException("The transfert failed", e);
        }
    }

    public void getPOM(Coordinates coordinates, File localResourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        if(findResourceByCoordinates(coordinates) == null) {
            throw new ResourceDoesNotExistException("Sorry for " + coordinates);
        }
        
        String pom = getPOM(coordinates);
        try {
            Files.write(pom, localResourceFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new TransferFailedException("Unable to write POM", e);
        }
    }
    
    public String getPOM(Coordinates coordinates) {
        return new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n")
                .append("<modelVersion>4.0.0</modelVersion>\n")
                .append("<groupId>").append(coordinates.getGroupID()).append("</groupId>\n")
                .append("<artifactId>").append(coordinates.getArtifactID()).append("</artifactId>\n")
                .append("<version>").append(coordinates.getVersion()).append("</version>\n")
                .append("</project>\n")
            .toString();
    }

    public void getPOMHash(Coordinates coordinates, File localResourceFile, HashFunction hashFunction) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        String pom = getPOM(coordinates);
        String hash = hashFunction.hashString(pom, Charsets.UTF_8).toString();
        try {
            Files.write(hash, localResourceFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new TransferFailedException("Unable to write POM", e);
        }
    }
    
    @Override
    public void get(String resourceName, File localResourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Coordinates coordinates = Coordinates.of(resourceName);

        if (resourceName.endsWith(".jar")) {
            getJAR(coordinates, localResourceFile);
        } else if (resourceName.endsWith(".pom")) {
            getPOM(coordinates.withPackaging("jar"), localResourceFile);
        } else if (resourceName.endsWith(".pom.sha1")) {
            throw new ResourceDoesNotExistException("Sorry !");
            //getPOMHash(coordinates, localResourceFile, Hashing.sha1());
        } else if (resourceName.endsWith(".pom.md5")) {
            throw new ResourceDoesNotExistException("Sorry !");
            //getPOMHash(coordinates, localResourceFile, Hashing.md5());
        } else {
            throw new UnsupportedOperationException("Nope. Nope Nope Nope. ");
        }
    }
    
    

    @Override
    public boolean getIfNewer(String string, File file, long l) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return false;
    }

    @Override
    public void put(File file, String string) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        throw new ResourceDoesNotExistException("Sorry :(");
    }

}
