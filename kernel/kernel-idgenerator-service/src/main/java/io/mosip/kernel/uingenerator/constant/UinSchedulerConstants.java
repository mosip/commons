package io.mosip.kernel.uingenerator.constant;

/**
 * Ceylon Chime scheduler keys for transferring assigned UINs from {@code uin} to {@code uin_assigned}.
 */
public class UinSchedulerConstants {

	/**
	 * Prevents instantiation of this constants type.
	 */
	private UinSchedulerConstants() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Ceylon Chime module coordinate.
	 */
	public static final String CEYLON_SCHEDULER = "ceylon:herd.schedule.chime/0.2.0";
	/**
	 * Event-bus address for the UIN transfer timer.
	 */
	public static final String TIMER_EVENT = "scheduler:uin_transfer";
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
	 * Property key for scheduler type ({@code kernel.uin.transfer-scheduler-type}).
	 */
	public static final String TYPE_VALUE = "kernel.uin.transfer-scheduler-type";
	/**
	 * Property key for seconds ({@code kernel.uin.transfer-scheduler-seconds}).
	 */
	public static final String SECONDS_VALUE = "kernel.uin.transfer-scheduler-seconds";
	/**
	 * Property key for minutes ({@code kernel.uin.transfer-scheduler-minutes}).
	 */
	public static final String MINUTES_VALUE = "kernel.uin.transfer-scheduler-minutes";
	/**
	 * Property key for hours ({@code kernel.uin.transfer-scheduler-hours}).
	 */
	public static final String HOURS_VALUE = "kernel.uin.transfer-scheduler-hours";
	/**
	 * Property key for days of month ({@code kernel.uin.transfer-scheduler-days_of_month}).
	 */
	public static final String DAY_OF_MONTH_VALUE = "kernel.uin.transfer-scheduler-days_of_month";
	/**
	 * Property key for months ({@code kernel.uin.transfer-scheduler-months}).
	 */
	public static final String MONTHS_VALUE = "kernel.uin.transfer-scheduler-months";
	/**
	 * Property key for days of week ({@code kernel.uin.transfer-scheduler-days_of_week}).
	 */
	public static final String DAYS_OF_WEEK_VALUE = "kernel.uin.transfer-scheduler-days_of_week";
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
	public static final String NAME_VALUE = "scheduler:uin_transfer";
	/**
	 * JSON key for timer description.
	 */
	public static final String DESCRIPTION = "description";

}
