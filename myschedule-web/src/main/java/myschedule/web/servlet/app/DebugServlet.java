package myschedule.web.servlet.app;

import myschedule.web.servlet.ActionHandler;
import myschedule.web.servlet.ActionHandlerServlet;
import myschedule.web.servlet.ViewData;
import myschedule.web.servlet.UrlRequestActionHandler;



/**
 * A extra debug servlet for dev use only.
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 *
 */
public class DebugServlet extends ActionHandlerServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() {
		addActionHandler("", new UrlRequestActionHandler());
		addActionHandler("/test", testAction);
	}
	
	protected ActionHandler testAction = new UrlRequestActionHandler() {
		@Override
		protected void handleViewData(ViewData viewData) {
			viewData.addData("message", "ServerTime=" + new java.util.Date());
		}			
	};
}