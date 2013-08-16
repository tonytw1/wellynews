package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

public interface ModelBuilder {

	public abstract boolean isValid(HttpServletRequest request);

	public abstract ModelAndView populateContentModel(HttpServletRequest request);
	
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv);
	
	public String getViewName(ModelAndView mv);

}