package io.mosip.kernel.core.auditmanager.spi;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface with function to write AuditRequest
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface AuditHandler<T> {

    /**
     * Function to write AuditRequest
     *
     * @param auditRequest The AuditRequest
     * @return true - if AuditRequest is successfully written
     */
    boolean addAudit(T auditRequest);

    /**
     * Function to write multiple AuditRequests in a batch
     *
     * @param auditRequests The list of AuditRequests
     * @return true - if AuditRequests are successfully written
     */
    boolean addAudits(List<T> auditRequests);

    /**
     * Function to update multiple AuditRequests in a batch
     *
     * @param auditRequests The list of AuditRequests to update
     * @return true - if AuditRequests are successfully updated
     */
    boolean updateAudits(List<T> auditRequests);

    /**
     * Function to delete audits older than the specified cutoff time
     *
     * @param cutoffTime The cutoff time for deleting old audits
     * @return true - if old audits are successfully deleted
     */
    boolean deleteAuditsOlderThan(LocalDateTime cutoffTime);
}