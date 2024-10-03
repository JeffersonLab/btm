package org.jlab.btm.presentation.util;

import org.hibernate.envers.RevisionListener;
import org.jlab.btm.persistence.entity.RevisionInfo;
import org.jlab.smoothness.presentation.filter.AuditContext;

public class RevisionInfoListener implements RevisionListener {

  @Override
  public void newRevision(Object o) {
    RevisionInfo revisionInfo = (RevisionInfo) o;

    AuditContext context = AuditContext.getCurrentInstance();

    String ip = context.getIp();
    String username = context.getUsername();

    revisionInfo.setAddress(ip);
    revisionInfo.setUsername(username);
  }
}
