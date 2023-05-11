package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.configuration.DefaultConfiguration;
import org.jlab.btm.persistence.epics.ContextPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a service locator / factory for obtaining EPICS contexts.
 * <p>
 * This class wraps around a ContextPool and provides life cycle management.
 *
 * <p>A resource bundle named epics.properties is consulted to determine
 * the EPICS addr_list value.</p>
 *
 * <p>This class is a singleton which is created on application startup and
 * destroyed on application shutdown.</p>
 *
 * @author ryans
 */
@Startup
@Singleton
public class ContextFactory {

    private static final Logger logger = Logger.getLogger(ContextFactory.class.getName());

    private ContextPool pool = null;

    /**
     * Get an EPICS channel access context.  Make sure to return it when you're
     * done.
     *
     * @return the context.
     * @throws CAException if unable to get the context.
     */
    public CAJContext getContext() throws CAException {
        return pool.getContext();
    }

    /**
     * Return a context to the factory.  This method should always be called
     * when done with a context.
     *
     * @param context the context.
     * @throws CAException if unable to return the context.
     */
    public void returnContext(CAJContext context) throws CAException {
        pool.returnContext(context);
    }

    /**
     * Construct the context factory.
     */
    @PostConstruct
    public void construct() {
        logger.log(Level.FINEST, "Constructing ContextPoolFactory");
        DefaultConfiguration config = new DefaultConfiguration("btm");

        ResourceBundle bundle = ResourceBundle.getBundle("epics");

        config.setAttribute("addr_list", bundle.getString("addr_list"));
        config.setAttribute("auto_addr_list", "false");

        try {
            pool = new ContextPool(config);
        } catch (CAException e) {
            logger.log(Level.SEVERE, "Unable to create channel access context", e);
        }
    }

    /**
     * Destroy the context factory.
     */
    @PreDestroy
    public void destruct() {
        logger.log(Level.FINEST, "Destroying ContextPoolFactory");
        try {
            pool.destroy();
        } catch (CAException e) {
            logger.log(Level.SEVERE, "Unable to destroy channel access context", e);
        }
    }
}
