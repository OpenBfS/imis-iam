/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.utils.KeycloakModelUtils;

import de.intevation.iam.util.DateUtils;

/**
 * Class running a MailTask at a given interval.
 */
public class MailScheduler {

    private KeycloakSessionFactory sessionFactory;

    private static MailScheduler instance;

    private static final Logger LOG = Logger.getLogger("MailScheduler");

    private final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> schedulerHandle;

    private State state;

    public enum State { STOPPED, RUNNING };

    private MailScheduler(KeycloakSession session) {
        LOG.info("Creating MailScheduler");
        this.sessionFactory = session.getKeycloakSessionFactory();
        this.state = State.STOPPED;
    }

    /**
     * Create or get the MailScheduler instance.
     * @param session Keycloak Session
     * @return Instance
     */
    public static MailScheduler instance(KeycloakSession session) {
        if (instance == null) {
            instance = new MailScheduler(session);
        }
        return instance;
    }

    /**
     * Start the scheduler.
     */
    public void start() {
        if (this.state != State.RUNNING) {
            LOG.info("Starting MailScheduler");
            this.state = State.RUNNING;
            run();
        }
    }

    /**
     * Stop the scheduler.
     */
    public void stop() {
        LOG.info("Stopping MailScheduler");
        schedulerHandle.cancel(true);
        this.state = State.STOPPED;
    }


    private void run() {
        final Runnable mailRunnable = new Runnable() {
            public void run() {
                try {
                    KeycloakSessionTask mailTask = new MailTask();
                    KeycloakModelUtils.runJobInTransaction(
                            sessionFactory, mailTask);
                } catch (Exception e) {
                    LOG.warning("Mail runnable failed:");
                    e.printStackTrace();
                }
            }
        };
        long schedulerInterval = DateUtils.getSecondsFromDurationEnv(
            "IAM_MAIL_SCHEDULER_INTERVAL");
        LOG.info(
            String.format("Running mail scheduler at %s",
            schedulerInterval));
        schedulerHandle = scheduler.scheduleAtFixedRate(
            mailRunnable, 0,
            schedulerInterval,
            TimeUnit.SECONDS);
    }
}
