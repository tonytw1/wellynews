package nz.co.searchwellington.feeds.rss.modules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

public class WellynewsModuleGenerator implements ModuleGenerator {
	
	 private static final Namespace NAMESPACE = Namespace.getNamespace("wellynews", WellynewsRssModule.URI);
	  private static final Set NAMESPACES;
	  static {
	    Set<Namespace> namespaces = new HashSet<Namespace>();
	    namespaces.add(NAMESPACE);
	    NAMESPACES = Collections.unmodifiableSet(namespaces);
	  }

	  public String getNamespaceUri() {
	    return WellynewsRssModule.URI;
	  }

	  public Set getNamespaces() {
	    return NAMESPACES;
	  }

	  public void generate(Module module, Element element) {
		  WellynewsRssModule myModule = (WellynewsRssModule) module;
		  if (myModule.getCommented() != null) {
			  Element myElement = new Element("commented", NAMESPACE);
			  myElement.setText(myModule.getCommented());
			  element.addContent(myElement);
		  }
	  }
	  
}
