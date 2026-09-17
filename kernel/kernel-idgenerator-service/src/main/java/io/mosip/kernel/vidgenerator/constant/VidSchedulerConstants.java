package io.mosip.kernel.vidgenerator.constant;

/**
 * Ceylon Chime scheduler keys for VID expiry and release jobs.
 */
public class VidSchedulerConstants {

	/**
	 * Prevents instantiation of this constants type.
	 */
	private VidSchedulerConstants() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Ceylon Chime module coordinate.
	 */
	public static final String CEYLON_SCHEDULER = "ceylon:herd.schedule.chime/0.2.0";
	/**
	 * Event-bus address for the VID revoke timer.
	 */
	public static final String TIMER_EVENT = "scheduler:vid_revoker";
	/**
	 * JSON key for scheduler type.
	 */
	public static final String TYPE = "type";
	/**
	 * JSON key for seconds.
	 */
	public static final String SECONDS = "seconds";
	/**
	 * JSON key for minutes.
	 */
	public static final String MINUTES = "minutes";
	/**
	 * JSON key for hours.
	 */
	public static final String HOURS = "hours";
	/**
	 * JSON key for day-of-month.
	 */
	public static final String DAY_OF_MONTH = "days of month";
	/**
	 * JSON key for months.
	 */
	public static final String MONTHS = "months";
	/**
	 * JSON key for days of week.
	 */
	public static final String DAYS_OF_WEEK = "days of week";
	/**
	 * Property key for scheduler type ({@code kernel.vid.revoke-scheduler-type}).
	 */
	public static final String TYPE_VALUE = "kernel.vid.revoke-scheduler-type";
	/**
	 * Property key for seconds ({@code kernel.vid.revoke-scheduler-seconds}).
	 */
	public static final String SECONDS_VALUE = "kernel.vid.revoke-scheduler-seconds";
	/**
	 * Property key for minutes ({@code kernel.vid.revoke-scheduler-minutes}).
	 */
	public static final String MINUTES_VALUE = "kernel.vid.revoke-scheduler-minutes";
	/**
	 * Property key for hours ({@code kernel.vid.revoke-scheduler-hours}).
	 */
	public static final String HOURS_VALUE = "kernel.vid.revoke-scheduler-hours";
	/**
	 * Property key for days of month ({@code kernel.vid.revoke-scheduler-days_of_month}).
	 */
	public static final String DAY_OF_MONTH_VALUE = "kernel.vid.revoke-scheduler-days_of_month";
	/**
	 * Property key for months ({@code kernel.vid.revoke-scheduler-months}).
	 */
	public static final String MONTHS_VALUE = "kernel.vid.revoke-scheduler-months";
	/**
	 * Property key for days of week ({@code kernel.vid.revoke-scheduler-days_of_week}).
	 */
	public static final String DAYS_OF_WEEK_VALUE = "kernel.vid.revoke-scheduler-days_of_week";
	/**
	 * Chime address.
	 */
	public static final String CHIME = "chime";
	/**
	 * JSON key for the Chime operation.
	 */
	public static final String OPERATION = "operation";
	/**
	 * Chime create operation.
	 */
	public static final String OPERATION_VALUE = "create";
	/**
	 * JSON key for the timer name.
	 */
	public static final String NAME = "name";
	/**
	 * Default timer name.
	 */
	public static final String NAME_VALUE = "scheduler:vid_revoker";
	/**
	 * JSON key for timer description.
	 */
	public static final String DESCRIPTION = "description";

}
