package nz.co.searchwellington.views;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.web.servlet.View;

public class JsonView implements View {

	private static final String CALLBACK = "callback";
	private static final Pattern VALID_CALLBACK_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
	
	private final JsonSerializer jsonSerializer;
	private final EtagGenerator etagGenerator;
	private Integer maxAge;

	public JsonView(JsonSerializer jsonSerializer, EtagGenerator etagGenerator) {
		this.jsonSerializer = jsonSerializer;
		this.etagGenerator = etagGenerator;
	}
	
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType());
    	response.setHeader("Cache-Control", "max-age=" + (maxAge != null ? maxAge : 0));
    	
		final String callback = request.getParameter(CALLBACK);
		if (callback != null) {
			if (!isValidCallbackName((String) model.get(CALLBACK))) {
				response.setStatus(HttpStatus.SC_BAD_REQUEST);
				return;
			}
		}
		
		final String json = jsonSerializer.serialize(model);
		response.setHeader("Etag", etagGenerator.makeEtagFor(json));
		
		writeWrappedInCallback(response, json, callback);
		
		response.getWriter().flush();
	}
	
	private boolean isValidCallbackName(String callback) {
		return VALID_CALLBACK_PATTERN.matcher(callback).matches();
	}

	private void writeWrappedInCallback(HttpServletResponse response, final String json, final String callback) throws IOException {
		if (callback != null) {
			response.getWriter().write(callback + "(");			
		}
		response.getWriter().write(json);		
		if (callback != null) {
			response.getWriter().write(");");			
		}
	}
	
}
