package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.DebugDao;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DebugService {
    private final DebugDao debugDao;

    @Autowired
    public DebugService(final DebugDao debugDao) {
        this.debugDao = debugDao;
    }

    public void clearDatabase() {
        debugDao.clear();
    }
}
