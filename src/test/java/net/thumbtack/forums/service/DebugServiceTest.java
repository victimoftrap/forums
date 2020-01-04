package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.DebugDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DebugServiceTest {
    private DebugDao mockDebugDao;
    private DebugService debugService;

    @BeforeEach
    void initMocks() {
        mockDebugDao = mock(DebugDao.class);
        debugService = new DebugService(mockDebugDao);
    }

    @Test
    void testClearDatabase() throws ServerException {
        doNothing()
                .when(mockDebugDao)
                .clear();

        debugService.clearDatabase();
        verify(mockDebugDao)
                .clear();
    }

    @Test
    void testClearDatabase_errorInDatabase_shouldThrowException() throws ServerException {
        doThrow(new ServerException(ErrorCode.DATABASE_ERROR))
                .when(mockDebugDao)
                .clear();
        try {
            debugService.clearDatabase();
        } catch (ServerException se) {
            assertEquals(ErrorCode.DATABASE_ERROR, se.getErrorCode());
        }
        verify(mockDebugDao)
                .clear();
    }
}