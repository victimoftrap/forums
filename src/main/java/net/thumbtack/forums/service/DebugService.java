package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.DebugDao;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service("debugService")
public class DebugService {
    private final DebugDao debugDao;

    @Autowired
    public DebugService(final DebugDao debugDao) {
        this.debugDao = debugDao;
    }

    public void clearDatabase() throws ServerException {
        debugDao.clear();
    }
}
