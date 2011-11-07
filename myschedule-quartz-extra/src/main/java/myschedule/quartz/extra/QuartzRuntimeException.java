package myschedule.quartz.extra;

import org.quartz.SchedulerException;

/**
 * Use to wrap Quartz's checked exception into RuntimeException.
 * 
 * @see SchedulerTemplate
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 *
 */
public class QuartzRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private SchedulerException schedulerException;
	
	public SchedulerException getSchedulerException() {
		return schedulerException;
	}
	
	public QuartzRuntimeException(SchedulerException schedulerException) {
		this.schedulerException = schedulerException;
	}
	
	public QuartzRuntimeException() {
	}

	public QuartzRuntimeException(String message) {
		super(message);
	}

	public QuartzRuntimeException(Throwable cause) {
		super(cause);
	}

	public QuartzRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	@Override
	public String toString() {
		if (schedulerException != null) {
			return schedulerException.toString();
		}
		return super.toString();
	}
}