package edu.kit.scc.webreg.job;

import edu.kit.scc.webreg.entity.UserStatus;
import jakarta.inject.Inject;
import org.slf4j.Logger;

/**
 * Job class for deleting users.
 *
 * Required Parameters:
 *
 * target_status_since_millis: Users which remain in specified status for
 * this long (in milliseconds) are deleted.
 *
 * target_user_status: Only users with this status will be deleted. Possible
 * values: blocked, deregistered, on_hold.
 *
 * Optional Parameters:
 *
 * registries_per_exec: This value determines how many users are processed
 * at once. This can reduce load on the database and prevents too long running
 * database transaction, if a low value is chosen. Ie. only 1 registry (default
 * value), but every minute.
 *
 * last_user_update_millis: If the last user update is older than this value in
 * milliseconds (default: 14 day = 1209600000), an update is attempted. The
 * user is only deleted, if their status doesn't change.
 *
 * @author Michael Burgardt
 */
public class DeleteOutOfDateUsers extends AbstractDeleteUserData {
    @Inject
    private Logger logger;
    @Override
    public void execute() {

        if (!getJobStore().containsKey("target_status_since_millis")) {
            logger.warn("DeleteOutOfDateUsers is not configured correctly. max_acceptable_period_millis parameter is missing in JobMap");
            return;
        }
        Long maxAbsencePeriod = Long.parseLong(getJobStore().get("target_status_since_millis"));

        Long updateAfter = 14 * 24 * 60 * 60 * 1000L; // 14 day
        if (getJobStore().containsKey("last_user_update_millis")) {
            updateAfter = Long.parseLong(getJobStore().get("last_user_update_millis"));
        }

        Integer limit = 1;
        if (getJobStore().containsKey("registries_per_exec")) {
            limit = Integer.parseInt(getJobStore().get("registries_per_exec"));
        }

        switch (String.valueOf(getJobStore().getOrDefault("target_user_status", ""))) {
            case "BLOCKED":
            case "blocked":
                executeDeletion(UserStatus.BLOCKED, maxAbsencePeriod, updateAfter, limit);
                break;
            case "DEREGISTERED":
            case "deregistered":
                executeDeletion(UserStatus.DEREGISTERED, maxAbsencePeriod, updateAfter, limit);
                break;
            case "ON_HOLD":
            case "on_hold":
                executeDeletion(UserStatus.ON_HOLD, maxAbsencePeriod, updateAfter, limit);
                break;
            default:
                logger.warn("UpdateOrDeleteUserDataJob is not configured correctly. target_user_status parameter in JobMap must be one of: blocked, deregistered or on_hold");
        }
    }
}