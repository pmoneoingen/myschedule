package myschedule.web.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Process all Http requests using a map of action paths to some callback handlers. Subclass may setup this handler 
 * mappings, and concentrate on each action into smaller piece of code, instead of worry about the inner working of 
 * Http request processing.
 * 
 * <p>
 * The default action path will be parse from the Http request's URI after the 'servletPath' section. This action path
 * is use to lookup pre-registered handler instance to handle the request. A typical usage is that a subclass is a 
 * Servlet, and it wil be map to a URL in web.xml (eg: '/demo-servlet' -> DemoServlet). Then this subclass may setup 
 * handler mappings in the init() method with action name as in '/mywebapp/demo-servlet/<action>' pattern. 
 * For example:
 * <pre>
 * public DemoServlet extends ActionHandlerServlet {
 *   @Override
 *   public void init() {
 *	   addActionHandler("", new ViewDataActionHandler()); // Eg: maps to http://localhost/mywebapp/demo-servlet
 *	   addActionHandler("/test", testAction);             // Eg: maps to http://localhost/mywebapp/demo-servlet/test
 *   }
 *   protected ActionHandler testAction = new ViewDataActionHandler() {
 *	   @Override
 *	   protected void handleViewData(ViewData viewData) {
 *	     viewData.addData("message", "ServerTime=" + new java.util.Date());
 *	   }			
 *	 };
 * }
 * </pre>
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 *
 */
public abstract class ActionHandlerServlet extends AbstractControllerServlet {
	private static final long serialVersionUID = 1L;
	private Map<String, ActionHandler> actionHandlerMappings = new HashMap<String, ActionHandler>();
	private Map<String, ActionFilter> actionFilterMappings = new HashMap<String, ActionFilter>();
		
	/** Allow subclass to add URL action path to a handler. This should be called in init() method of subclass. */
	protected void addActionHandler(String actionPath, ActionHandler handler) {
		// Ensure action path is consistent.
		if (actionPath.endsWith("/")) { 
			throw new IllegalArgumentException("Action path should not ends with '/'.");
		}
		
		// We will try our best to print most useful mapping path, but it will depend where subclass
		// is adding the handler. For example, if they add it in init(), then all these are good, but 
		// if it's added in other methods, then servlet context will not be available.
		if (logger.isInfoEnabled()) {
			String fullActionPath = "";
			if (getServletContext() != null) {
				String ctxName = getServletContext().getContextPath();
				// Note: The Servlet API doesn't allow use to get the 'servletPath' at this stage, only in request.
				//       because multiple 'servletPath' could be mapped to <ServletName>. So the best we can do is
				//       the <ServletName> here.
				String servletName = getServletConfig().getServletName();
				fullActionPath += ctxName + "/<" + servletName + ">";
			} else {
				// If not context is available, then at least let user know the general form.
				fullActionPath += "/<YourWebappContext>/<YourServletNameMapping>";
			}
			fullActionPath += actionPath;
			logger.info("Path '{}' is mapped to action handler: {}", fullActionPath, handler);
		}
		actionHandlerMappings.put(actionPath, handler);
	}
	
	protected void addActionFilter(String actionPath, ActionFilter filter) {
		logger.info("Adding filter on action path starting with: {}", actionPath);
		actionFilterMappings.put(actionPath, filter);
	}
		
	@Override
	protected ViewData process(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String actionPath = getActionPath(req);
		// Ensure action path is consistent.
		if (actionPath.endsWith("/")) {
			actionPath = actionPath.substring(0, actionPath.length() -1);
		}
		logger.debug("Action path: {}", actionPath);
		
		ActionHandler handler = findActionHandler(actionPath, req);
		logger.debug("Action handler: {}", handler);
		if (handler == null) {
			String actionServletPath = req.getServletPath()  + actionPath;
			throw new RuntimeException("Unable to find action handler for path: " + actionServletPath);
		}
		
		ActionFilter filter = findActionFilter(actionPath, req);
		if (filter != null) {
			ViewData viewData = filter.beforeAction(actionPath, req, resp);
			if (viewData != null) {
				logger.debug("Filter has stopped the before action path: {}.", actionPath);
				return viewData;
			}
		}
		
		ViewData viewData = handler.handleAction(actionPath, req, resp);
		logger.trace("Handler result: {}", viewData);
		
		if (filter != null) {
			filter.afterAction(viewData, actionPath, req, resp);
		}
		return viewData;
	}
	
	/** 
	 * We assume all filter actionPath are added and match by the front of the actionPath (String.startsWith). It's not
	 * the same as the action handler where it match by exact name!
	 * 
	 * @param actionPath
	 * @param req
	 * @return
	 */
	protected ActionFilter findActionFilter(String actionPath, HttpServletRequest req) {
		Set<String> filterNames = actionFilterMappings.keySet();
		String matchedName = null;
		for (String name : filterNames) {
			if (actionPath.startsWith(name)) {
				matchedName = name;
				break;
			}
		}
		
		ActionFilter result = null;
		if (matchedName != null) {
			result = actionFilterMappings.get(matchedName);
		}
		return result;
	}

	/** Extract action Path from request URI after the servletPath portion. */
	protected String getActionPath(HttpServletRequest req) {
		String contextPath = req.getContextPath();
		String servletPath = req.getServletPath();
		String reqUri = req.getRequestURI();
		return reqUri.substring(contextPath.length() + servletPath.length());
	}

	protected ActionHandler findActionHandler(String actionPath, HttpServletRequest req) {
		return actionHandlerMappings.get(actionPath);
	}
}