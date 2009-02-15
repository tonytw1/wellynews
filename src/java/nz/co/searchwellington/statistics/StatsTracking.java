package nz.co.searchwellington.statistics;

import org.springframework.web.servlet.ModelAndView;

public class StatsTracking {
    
    // TODO move to a toolbox item?
    @SuppressWarnings("unchecked")
	public static void setRecordPageImpression(ModelAndView mv, String statsTrackingCode) {
		final boolean statsTrackingIsConfigured = statsTrackingCode != null && !statsTrackingCode.equals("");
		if (statsTrackingIsConfigured) {
			boolean okToRecordPageImpression = mv.getModel().get("login_prompt") == null && mv.getModel().get("logged_in_user") == null;
			if (okToRecordPageImpression) {
				mv.getModel().put("record_page_impression", 1);
				mv.getModel().put("stats_tracking_code", statsTrackingCode);
			}
		}
	}
    
    
}
