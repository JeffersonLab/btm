package org.jlab.btm.business.service;

import jakarta.ejb.SessionContext;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Set;

/**
 * The logged-in user and their roles, as WildFly's SessionContext would report them to a service,
 * for tests that run services outside WildFly. Starts as an anonymous caller with no roles.
 */
class TestCaller {
  private String username = "anonymous";
  private Set<String> roles = Set.of();

  /** Becomes the named user, with the given roles. */
  void is(String username, String... roles) {
    this.username = username;
    this.roles = Set.of(roles);
  }

  /** A SessionContext that reports this caller; other methods are unsupported. */
  SessionContext context() {
    return (SessionContext)
        Proxy.newProxyInstance(
            SessionContext.class.getClassLoader(),
            new Class<?>[] {SessionContext.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "getCallerPrincipal" -> (Principal) () -> username;
                  case "isCallerInRole" -> roles.contains((String) args[0]);
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }
}
