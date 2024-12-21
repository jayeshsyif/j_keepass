package org.j_keepass.events.reload;

import org.j_keepass.events.interfaces.ReloadAction;

public interface ReloadEvent {
    void reload(ReloadAction reloadAction);
}
