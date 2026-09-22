package io.mosip.kernel.core.pdfgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Axis-aligned box used as the visible PDF signature rectangle.
 * <p>
 * Contract: coordinates are PDF user-space points. {@code llx}/{@code lly} is
 * the lower-left corner; {@code urx}/{@code ury} is the upper-right. Does not
 * perform I/O.
 * </p>
 *
 * @author Urvil Joshi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rectangle {

	/**
	 * Lower-left X in PDF user-space points.
	 */
	private float llx;
	/**
	 * Lower-left Y in PDF user-space points.
	 */
	private float lly;
	/**
	 * Upper-right X in PDF user-space points.
	 */
	private float urx;
	/**
	 * Upper-right Y in PDF user-space points.
	 */
	private float ury;

}
