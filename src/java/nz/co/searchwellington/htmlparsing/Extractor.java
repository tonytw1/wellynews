package nz.co.searchwellington.htmlparsing;

import java.util.List;

public interface Extractor {

    public abstract List<String> extractLinks(String inputHTML);

}