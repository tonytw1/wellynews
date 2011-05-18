package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

public interface ModelBuilder {

	public abstract boolean isValid(HttpServletRequest request);

	public abstract ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken);
	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv);
	
	public String getViewName(ModelAndView mv);

}