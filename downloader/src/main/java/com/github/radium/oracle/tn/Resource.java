package com.github.radium.oracle.tn;

public enum Resource {
    
    OJDBC6("http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html", "ojdbc6.jar", "com.oracle:ojdbc6:12.1.0.2"),
    OJDBC7("http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html", "ojdbc7.jar", "com.oracle:ojdbc7:12.1.0.2"),
    UCP("http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html", "ucp.jar", "com.oracle:ucp:12.1.0.2");
    
    final private String entryPointURL;
    final private String fileName; 
    final private String coordinates;
    
    private Resource(String entryPointURL, String fileName, String coordinates) {
        this.entryPointURL = entryPointURL;
        this.fileName = fileName;
        this.coordinates = coordinates;
    }
    
    public String getEntryPointURL() {
        return entryPointURL;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getCoordinates() {
        return coordinates;
    }

}
