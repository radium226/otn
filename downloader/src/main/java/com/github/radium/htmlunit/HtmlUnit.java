package com.github.radium.htmlunit;

import com.gargoylesoftware.htmlunit.BinaryPage;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlUnit {

    final public static int SLEEP_DURATION = 1;
    
    private HtmlUnit() {
        super();
    }
    
    public static void disableLogging() {
        Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
        Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

    }
    
    public static WebClient newClient() {
        WebClient client = new WebClient();
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        return client;
    }
    
    public static HtmlAnchor findAnchorByTextContent(HtmlPage page, final String textContent) {
        return findAnchorByTextContent(page.getBody(), textContent);
    }
    
    public static HtmlAnchor findAnchorByTextContent(HtmlElement element, final String textContent) {
        return ((List<HtmlAnchor>) element.getByXPath("//a")).stream()
            .filter(new Predicate<HtmlAnchor>() {

                @Override
                public boolean test(HtmlAnchor anchor) {
                    return anchor.getTextContent().equals(textContent);
                }
                
            }).findFirst().get();
    }
    
    public static <E extends HtmlElement> E findByName(HtmlElement element, final String tagName, final String name) {
        return ((List<E>) element.getByXPath("//" + tagName)).stream()
            .filter(new Predicate<E>() {

                @Override
                public boolean test(E childElement) {
                    return childElement.getAttribute("name").equals(name);
                }
                    
            }).findFirst().get();
    }

    public static HtmlForm findFormByName(HtmlElement element, String name) {
        return findByName(element, "form", name);
    }

    public static HtmlInput findInputByName(HtmlElement element, String name) {
        return findByName(element, "input", name);
    }
    
    public static HtmlPage browsePage(WebClient client, String url) throws IOException {
        HtmlPage page = client.getPage(url);
        Uninterruptibles.sleepUninterruptibly(SLEEP_DURATION, TimeUnit.SECONDS);
        return page;
    }
    
    public static InputStream download(Page page) throws IOException {
        BinaryPage binaryPage = new BinaryPage(page.getWebResponse(), page.getEnclosingWindow());
        InputStream inputStream = binaryPage.getInputStream();
        return inputStream;
    }
    
}
