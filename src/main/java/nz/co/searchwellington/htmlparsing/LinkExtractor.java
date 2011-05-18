package nz.co.searchwellington.htmlparsing;

import java.util.Set;

public interface LinkExtractor {

    public abstract Set<String> extractLinks(String inputHTML);

}