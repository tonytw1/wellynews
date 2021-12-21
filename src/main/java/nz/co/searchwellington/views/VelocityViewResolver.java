package nz.co.searchwellington.views;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

public class VelocityViewResolver implements ViewResolver {

    @Override
    public View resolveViewName(String viewname, Locale locale) throws Exception {
        return new VelocityView();
    }
}
