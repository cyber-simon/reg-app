package edu.kit.scc.webreg.job;

import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.UserService;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for deleting non-active users.
 *
 * @author Michael Burgardt
 */
public abstract class AbstractDeleteUserData extends AbstractExecutableJob {

    public static Logger logger = LoggerFactory.getLogger(AbstractDeleteUserData.class);

    /**
     * Attempts to delete all user data for users with given status.
     *
     * @param targetStatus Process users with this status only.
     * @param lastUpdate [ms] If a user has targetStatus for longer
     * than this, their user data is deleted. Set to -1 to prevent any and all
     * deletions.
     * @param lastUserUpdate [ms] If a user was last updated longer than this ago,
     * attempt to update them before performing the deletion.
     * @param limit This value determines how many registries are processed at
     * once.
     */
    public void executeDeletion (UserStatus targetStatus, Long lastUpdate, Long lastUserUpdate, Integer limit) {
        String auditName = targetStatus + "-rem-job";

        try {
            InitialContext ic = new InitialContext();
            UserService userService = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");
            UserDeleteService userDeleteService = (UserDeleteService) ic.lookup("global/bwreg/bwreg-service/UserDeleteServiceImpl!edu.kit.scc.webreg.service.UserDeleteService");

            List<UserEntity> userList;
            logger.info("Initialising deletion of {} users", targetStatus);
            userList = userService.findByStatusAndTimeSince(targetStatus, lastUpdate, limit);

            for (UserEntity user : userList) {
                try {
                    logger.debug("Attempting to delete user {}", user.getEppn());

                    if (user instanceof SamlUserEntity) {
                        SamlUserEntity samlUser = (SamlUserEntity) user;
                        IdentityEntity identity = samlUser.getIdentity();

                        Long sinceLastChange = System.currentTimeMillis() - samlUser.getLastStatusChange().getTime();
                        Long sinceLastUpdate = System.currentTimeMillis() - samlUser.getLastUpdate().getTime();
                        if (sinceLastUpdate > lastUserUpdate) {
                            logger.info("User {} lastUpdate is older than {}ms, trying to update", user.getEppn(), lastUserUpdate);
                            try {
                                userService.updateUserFromIdp(samlUser, auditName);
                                logger.info("Update performed");
                            } catch (UserUpdateException e) {
                                logger.info("Exception while querying IDP: {}\nUpdate fails since {}ms ago", e.getMessage(), sinceLastUpdate);
                                if (e.getCause() != null) {
                                    logger.info("Cause is: {}", e.getCause().getMessage());
                                    if (e.getCause().getCause() != null) {
                                        logger.info("Inner cause is: {}", e.getCause().getCause().getMessage());
                                    }
                                }
                                // resume without deregistering user
                                throw new RegisterException("IDP failed");
                            }
                        }

                        if (targetStatus.equals(samlUser.getUserStatus())) {
                            logger.info("User {} had status {} for {}ms, attempting deletion", samlUser.getEppn(), targetStatus, sinceLastChange);
                            userDeleteService.deleteUserData(identity, "identity-" + identity.getId());
                            logger.info("Deletion successful");
                        } else {
                            logger.info("User status changed. User deletion postponed for another {}ms", lastUpdate - sinceLastChange);
                        }
                    }
                } catch (RegisterException e) {
                    logger.info("Due update failed, postponing deletion", e);
                }
            }
        } catch (NamingException e) {
            logger.warn("Could not delete users: {}", e);
        }
    }
}