package nz.co.searchwellington.views;

import org.apache.velocity.spring.VelocityEngineUtils;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VelocityView extends VelocityEngineUtils implements View {
    @Override
    public void render(Map<String, ?> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

    }
}
