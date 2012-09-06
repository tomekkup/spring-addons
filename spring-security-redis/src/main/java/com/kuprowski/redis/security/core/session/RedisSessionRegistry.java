package com.kuprowski.redis.security.core.session;

import java.security.Principal;
import java.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.util.Assert;

/**
 * ********************************************************
 * Copyright: 2012 Tomek Kuprowski
 *
 * License: GPLv2: http://www.gnu.org/licences/gpl.html
 *
 * @author Tomek Kuprowski (tomekkuprowski at gmail dot com)
 * *******************************************************
 */
/**
 * Implementation of <code>SessionRegistry</code> which stores a <code>SessionInformation</code> in Redis
 * 
 */
public class RedisSessionRegistry extends SessionRegistryImpl implements InitializingBean {

    private RedisConnectionFactory connectionFactory;
    protected RedisTemplate<String, SessionInformation> sessionIdsTemplate = new RedisTemplate<String, SessionInformation>();
    protected RedisTemplate<String, String> principalsTemplate = new RedisTemplate<String, String>();

    @Required
    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Object> getAllPrincipals() {
        Set<String> all = principalsTemplate.opsForSet().members(buildPrincipalsAllKey());
        if (all == null) {
            return new ArrayList<Object>();
        }
        return new ArrayList<Object>(all);
    }

    protected Set<String> getSessionsUsedByPrincipal(Object principal) {
        return principalsTemplate.opsForSet().members(buildPrincipalKey(principal));
    }

    @Override
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
        final Set<String> sessionsUsedByPrincipal = getSessionsUsedByPrincipal(principal);

        if (sessionsUsedByPrincipal == null) {
            return Collections.emptyList();
        }

        List<SessionInformation> list = new ArrayList<SessionInformation>(sessionsUsedByPrincipal.size());

        for (String sessionId : sessionsUsedByPrincipal) {
            SessionInformation sessionInformation = getSessionInformation(sessionId);

            if (sessionInformation == null) {
                continue;
            }

            if (includeExpiredSessions || !sessionInformation.isExpired()) {
                list.add(sessionInformation);
            }
        }

        return list;
    }

    @Override
    public SessionInformation getSessionInformation(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");
        return sessionIdsTemplate.opsForValue().get(buildSessionIdsKey(sessionId));
    }

    @Override
    public void refreshLastRequest(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");

        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            info.refreshLastRequest();
            sessionIdsTemplate.opsForValue().set(buildSessionIdsKey(sessionId), info);
        }
    }

    @Override
    public void registerNewSession(String sessionId, Object principal) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");
        Assert.notNull(principal, "Principal required as per interface contract");

        if (logger.isDebugEnabled()) {
            logger.debug("Registering session " + sessionId + ", for principal " + principal);
        }

        if (getSessionInformation(sessionId) != null) {
            removeSessionInformation(sessionId);
        }

        sessionIdsTemplate.opsForValue().set(buildSessionIdsKey(sessionId), new SessionInformation(principal, sessionId, new Date()));
        principalsTemplate.opsForSet().add(buildPrincipalKey(principal), sessionId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        sessionIdsTemplate.setConnectionFactory(connectionFactory);
        sessionIdsTemplate.afterPropertiesSet();
        principalsTemplate.setConnectionFactory(connectionFactory);
        principalsTemplate.afterPropertiesSet();
    }

    protected final String buildSessionIdsKey(String sessionId) {
        return RedisSessionRegistry.class.getName() + "_" + sessionId;
    }

    protected final String buildPrincipalsAllKey() {
        return RedisSessionRegistry.class.getName() + "_ALL_LIST";
    }

    protected final String buildPrincipalKey(Object principal) {
        return RedisSessionRegistry.class.getName() + "_" + ((Principal) principal).getName();
    }

    @Override
    public void removeSessionInformation(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");

        SessionInformation info = getSessionInformation(sessionId);

        if (info == null) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.debug("Removing session " + sessionId + " from set of registered sessions");
        }

        sessionIdsTemplate.delete(buildSessionIdsKey(sessionId));

        Set<String> sessionsUsedByPrincipal = getSessionsUsedByPrincipal(info.getPrincipal());

        if (sessionsUsedByPrincipal == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Removing session " + sessionId + " from principal's set of registered sessions");
        }

        sessionsUsedByPrincipal.remove(sessionId);
        principalsTemplate.opsForSet().remove(buildPrincipalKey(info.getPrincipal()), sessionId);

        if (sessionsUsedByPrincipal.isEmpty()) {
            // No need to keep object in principals Map anymore
            if (logger.isDebugEnabled()) {
                logger.debug("Removing principal " + info.getPrincipal() + " from registry");
            }
            //TODO REMOVE
            //principalsTemplate.Ops.remove(info.getPrincipal());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Sessions used by '" + info.getPrincipal() + "' : " + sessionsUsedByPrincipal);
        }
    }
}
