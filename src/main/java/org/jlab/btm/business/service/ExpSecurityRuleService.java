package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.*;
import org.jlab.btm.persistence.entity.ExpSignature;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for verifying security rules for editing an experimenter hall timesheet.
 *
 * @author ryans
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@DeclareRoles({"btm-admin", "halead", "hblead", "hclead", "hdlead"})
public class ExpSecurityRuleService {
  private static final Logger logger = Logger.getLogger(ExpSecurityRuleService.class.getName());

  @Resource protected SessionContext context;

  @EJB ExpSignatureService signatureService;

  /**
   * Run the security rules for editing a timesheet denoted by hall and day and hour and return a
   * reason for failure or null if the checks pass.
   *
   * <p>The security rules are:
   *
   * <ul>
   *   <li>You can only anonymously edit a timesheet if your IP address is on our white list.
   *   <li>You cannot edit a timesheet that has already been signed by someone with an equal or more
   *       powerful role except for an Operability Manager, who can alway make modifications. The
   *       roles in increasing power are User, Hall Manager, Operability Manager.
   * </ul>
   *
   * @param hall the hall.
   * @param dayAndHour the day and hour.
   * @return the validation failure reason or null.
   */
  @PermitAll
  public String checkForEditDeniedReason(Hall hall, Date dayAndHour) {
    // logger.log(Level.WARNING, "checkForEditDeniedReason");

    SignatureChecker sigChecker = new SignatureChecker(hall, dayAndHour);

    boolean opSigned = sigChecker.isOperabilityManagerSigned();
    boolean hallSigned = sigChecker.isHallManagerSigned(hall);
    boolean userSigned = sigChecker.isUserSigned();
    boolean callerIsOperabilityManager = context.isCallerInRole("btm-admin");
    boolean callerIsHallManager = context.isCallerInRole(Role.getHallManagerRole(hall).getName());
    String username = context.getCallerPrincipal().getName();

    boolean anonymous = (username == null || username.equalsIgnoreCase("ANONYMOUS"));

    /*logger.log(Level.WARNING, "hall: {0}", hall);
    logger.log(Level.WARNING, "hall role: {0}", Role.getHallManagerRole(hall).getName());
    logger.log(Level.WARNING, "anonymous: {0}", anonymous);
    logger.log(Level.WARNING, "username: {0}", username);
    logger.log(Level.WARNING, "opSigned: {0}", opSigned);
    logger.log(Level.WARNING, "hallSigned: {0}", hallSigned);
    logger.log(Level.WARNING, "userSigned: {0}", userSigned);
    logger.log(Level.WARNING, "isOability: {0}", callerIsOperabilityManager);
    logger.log(Level.WARNING, "isHallManager: {0}", callerIsHallManager);*/

    if (anonymous) {
      return "You must login to modify a timesheet";
    }

    // Only allow edits if a superior or colleage hasn't already signed
    if (!callerIsOperabilityManager) {
      if (opSigned) {
        return "You cannot modify an Admin signed timesheet unless you are an Admin";
      }

      if (!callerIsHallManager) {
        if (hallSigned) {
          return "You cannot modify a Hall Manager signed timesheet unless you are a Hall Manager or Admin";
        }

        Date now = new Date();
        long differenceMillis = Math.abs(now.getTime() - dayAndHour.getTime());
        long fourtyEightHoursInMillis = 172_800_000;

        boolean after48Hours = differenceMillis > fourtyEightHoursInMillis;

        if (after48Hours && userSigned) {
          ExpSignature sig = sigChecker.getUserSignature();

          if (!username.equals(sig.getSignedBy())) {
            return "After 48 hours you cannot modify a User signed timesheet unless you are the signer, Hall Manager, or Admin";
          }
        }
      }
    }

    return null;
  }

  /**
   * Throws a security exception if editing the timesheet denoted by hall and day and hour is
   * allowed based on the current user security situation.
   *
   * @param hall the hall.
   * @param dayAndHour the day and hour.
   * @throws EJBAccessException if security checks fail.
   */
  @PermitAll
  public void editCheck(Hall hall, Date dayAndHour) throws EJBAccessException {
    logger.log(Level.FINEST, "editCheck");
    String reason = checkForEditDeniedReason(hall, dayAndHour);
    if (reason != null) {
      throw new EJBAccessException(reason);
    }
  }

  /**
   * Returns a boolean indicating whether editing the timesheet denoted by hall and day and hour is
   * allowed based on the current user security situation.
   *
   * @param hall the hall.
   * @param dayAndHour the day and hour.
   * @return true if editing is allowed.
   */
  @PermitAll
  public boolean isEditAllowed(Hall hall, Date dayAndHour) {
    boolean allowed = false;

    String reason = checkForEditDeniedReason(hall, dayAndHour);

    if (reason == null) {
      allowed = true;
    }

    return allowed;
  }

  class SignatureChecker {

    private List<ExpSignature> signatures;

    SignatureChecker(Hall hall, Date dayAndHour) {
      signatures = signatureService.find(hall, dayAndHour);
    }

    /**
     * Determines whether the timesheet has been signed by a user.
     *
     * @return true if there is a user signature.
     */
    public boolean isUserSigned() {
      boolean userSigned = false;

      if (signatures != null) {
        for (ExpSignature signature : signatures) {
          if (signature.getSignedRole() == Role.USER) {
            userSigned = true;
            break;
          }
        }
      }

      return userSigned;
    }

    /**
     * Determines whether the timesheet has been signed by a hall manager for a specified hall.
     *
     * @param hall the hall.
     * @return true if there is a hall manager signature.
     */
    public boolean isHallManagerSigned(Hall hall) {
      boolean opSigned = false;

      if (signatures != null) {
        for (ExpSignature signature : signatures) {
          if (signature.getSignedRole() == Role.getHallManagerRole(hall)) {
            opSigned = true;
            break;
          }
        }
      }

      return opSigned;
    }

    /**
     * Determines whether the timesheet has been signed by an operability manager.
     *
     * @return true if there is an operability manager signature.
     */
    public boolean isOperabilityManagerSigned() {
      boolean opSigned = false;

      if (signatures != null) {
        for (ExpSignature signature : signatures) {
          if (signature.getSignedRole() == Role.OPERABILITY_MANAGER) {
            opSigned = true;
            break;
          }
        }
      }

      return opSigned;
    }

    /**
     * Return the user signature or null if it doesn't exist.
     *
     * @return the user signature or null.
     */
    public ExpSignature getUserSignature() {
      ExpSignature sig = null;

      if (signatures != null) {
        for (ExpSignature signature : signatures) {
          if (signature.getSignedRole() == Role.USER) {
            sig = signature;
            break;
          }
        }
      }

      return sig;
    }
  }
}
