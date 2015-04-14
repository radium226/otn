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
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Component(role = Wagon.class, hint = "otn")
public class ResourceWagon extends AbstractWagon {

    public ResourceWagon() {
        super();

    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        System.out.println(this.getRepository());
    }

    @Override
    protected void closeConnection() throws ConnectionException {
        System.out.println("closeConnection");
    }

    private Resource findResourceByCoordinates(final Coordinates coordinates) {
        return Iterables.getFirst(Iterables.filter(Arrays.asList(Resource.values()), new Predicate<Resource>() {

            @Override
            public boolean apply(Resource resource) {
                System.out.println(resource);
                return coordinates.equals(Coordinates.fromText(resource.getCoordinates()));
            }

        }), null);
    }

    public void getJAR(Coordinates coordinates, File localResourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = findResourceByCoordinates(coordinates);
        System.out.println(" ===> " + resource);
        if (resource == null) {
            throw new ResourceDoesNotExistException("The resource does not exists");
        }

        try {
            String url = "http" + getRepository().getUrl().substring("otn".length());
            System.out.println(" ----> url = " + url);
            
            String userName = getAuthenticationInfo().getUserName();
            String password = getAuthenticationInfo().getPassword();

            System.out.println("user = " + userName);
            System.out.println("password = " + password);
            HtmlUnit.disableLogging();
            ResourceDownloader.signIn(userName, password).browse(url).downloadResource(resource, localResourceFile);
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
        System.out.println("pom = " + pom);
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
        System.out.println(hashFunction + " = " + hash);
        try {
            Files.write(hash, localResourceFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new TransferFailedException("Unable to write POM", e);
        }
    }
    
    @Override
    public void get(String resourceName, File localResourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println(" ---------> resourceName=" + resourceName);
        Coordinates coordinates = Coordinates.of(resourceName);

        if (resourceName.endsWith(".jar")) {
            getJAR(coordinates, localResourceFile);
        } else if (resourceName.endsWith(".pom")) {
            getPOM(coordinates, localResourceFile);
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
        System.out.println("getIfNewer");
        return false;
    }

    @Override
    public void put(File file, String string) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println("put");
        throw new ResourceDoesNotExistException("Sorry :(");
    }

}
