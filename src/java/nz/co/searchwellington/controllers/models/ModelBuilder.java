package nz.co.searchwellington.controllers.models;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

public interface ModelBuilder {

	public abstract boolean isValid(HttpServletRequest request);

	public abstract ModelAndView populateContentModel(
			HttpServletRequest request, boolean showBroken) throws IOException,
			CorruptIndexException, FeedException;

}