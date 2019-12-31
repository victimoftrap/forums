package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest extends DaoTestEnvironment {
    @Test
    void testSaveUser() throws ServerException {
        final User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );

        final User savedUser = userDao.save(user);
        assertAll(
                () -> assertNotEquals(0, savedUser.getId()),
                () -> assertNotEquals(0, user.getId()),
                () -> assertEquals(user, savedUser)
        );
    }

    @Test
    void testSaveUserAndHisSession() throws ServerException {
        User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        UserSession session = new UserSession(
                user, UUID.randomUUID().toString()
        );

        UserSession savedSession = userDao.save(user, session);
        assertAll(
                () -> assertNotEquals(0, user.getId()),
                () -> assertEquals(session, savedSession)
        );
    }

    @Test
    void testSaveUser_usernameAreNull_shouldNotCreateUser() {
        final User user = new User(
                UserRole.USER,
                null, "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );

        assertThrows(ServerException.class,
                () -> userDao.save(user)
        );
    }

    @Test
    void testGetUserById() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final User selectedUser = userDao.getById(user.getId());
        assertEquals(user, selectedUser);
    }

    @Test
    void testGetUserById_userNotExists_shouldNotFindUser() throws ServerException {
        final User selectedUser = userDao.getById(1256);
        assertNull(selectedUser);
    }

    @Test
    void testGetUserThatMayBeDeletedById_userDeleted_shouldFindUser() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);
        userDao.deactivateById(user.getId());

        final User selectedFirstly = userDao.getById(user.getId());
        final User selectedSecondly = userDao.getById(user.getId(), true);
        assertNotNull(selectedFirstly);
        assertNotNull(selectedSecondly);
        assertEquals(selectedFirstly, selectedSecondly);
    }

    @Test
    void testGetUserById_userDeleted_shouldNotFindUser() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);
        userDao.deactivateById(user.getId());

        final User selectedUser = userDao.getById(user.getId(), false);
        assertNull(selectedUser);
    }

    @Test
    void testGetUserByName() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final User selectedUser = userDao.getByName(user.getUsername());
        assertEquals(user, selectedUser);
    }

    @Test
    void testGetUserByName_userNotExists_shouldNotFindUser() throws ServerException {
        final User selectedUser = userDao.getByName("catchMeIfYouCan");
        assertNull(selectedUser);
    }

    @Test
    void testGetUserThatMayBeDeletedByName_userDeleted_shouldFindUser() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);
        userDao.deactivateById(user.getId());

        final User selectedFirstly = userDao.getByName(user.getUsername());
        final User selectedSecondly = userDao.getByName(user.getUsername(), true);
        assertNotNull(selectedFirstly);
        assertNotNull(selectedSecondly);
        assertEquals(selectedFirstly, selectedSecondly);
    }

    @Test
    void testGetUserByName_userDeleted_shouldNotFindUser() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);
        userDao.deactivateById(user.getId());

        final User selectedUser = userDao.getByName(user.getUsername(), false);
        assertNull(selectedUser);
    }

    @Test
    void testGetAllUsers() throws ServerException {
        User admin = new User(
                1, UserRole.SUPERUSER, "admin",
                "admin@example.com", "admin_strong_pass",
                null, false
        );

        User user1 = new User(
                UserRole.SUPERUSER, "jolygolf",
                "jolygolf@gmail.com", "pryadko",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);

        User user2 = new User(
                UserRole.USER, "millenniumeye23",
                "sokolov@gmail.com", "millenniumeye23",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);

        User user3 = new User(
                UserRole.USER, "link",
                "tokach@gmail.com", "igorlink",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user3);

        final List<User> users = userDao.getAll();
        assertEquals(4, users.size());

        User actualAdmin = users.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());
        assertIterableEquals(Arrays.asList(user1, user2, user3), users.subList(1, users.size()));
    }

    @Test
    void testGetAllUsers_someUsersDeleted_shouldReturnOnlyEnabledUsers() throws ServerException {
        User admin = new User(
                1, UserRole.SUPERUSER, "admin",
                "admin@example.com", "admin_strong_pass",
                null, false
        );

        User user1 = new User(
                UserRole.SUPERUSER, "jolygolf",
                "jolygolf@gmail.com", "pryadko",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);

        User user2 = new User(
                UserRole.USER, "millenniumeye23",
                "sokolov@gmail.com", "millenniumeye23",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);

        User user3 = new User(
                UserRole.USER, "link",
                "tokach@gmail.com", "igorlink",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user3);

        userDao.deactivateById(user2.getId());
        final List<User> expectedUsers = Arrays.asList(user1, user3);
        final List<User> actualUsers = userDao.getAll(false);
        assertEquals(3, actualUsers.size());

        User actualAdmin = actualUsers.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());
        assertIterableEquals(expectedUsers, actualUsers.subList(1, actualUsers.size()));
    }

    @Test
    void testGetAllUsers_someUsersDeleted_shouldReturnAllUsers() throws ServerException {
        User admin = new User(
                1, UserRole.SUPERUSER, "admin",
                "admin@example.com", "admin_strong_pass",
                null, false
        );

        User user1 = new User(
                UserRole.SUPERUSER, "jolygolf",
                "jolygolf@gmail.com", "pryadko",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user1);

        User user2 = new User(
                UserRole.USER, "millenniumeye23",
                "sokolov@gmail.com", "millenniumeye23",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);

        User user3 = new User(
                UserRole.USER, "link",
                "tokach@gmail.com", "igorlink",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user3);
        userDao.deactivateById(user2.getId());
        final List<User> expectedEnabledUsers = Arrays.asList(user1, user3);
        final List<User> expectedDeletedUsers = Collections.singletonList(user2);

        final List<User> selectedFirstly = userDao.getAll();
        final List<User> enabledUsers1 = selectedFirstly
                .stream()
                .filter(u -> !u.isDeleted())
                .collect(Collectors.toList());
        final List<User> deletedUsers1 = selectedFirstly
                .stream()
                .filter(User::isDeleted)
                .collect(Collectors.toList());

        User actualAdmin = enabledUsers1.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());

        assertEquals(expectedEnabledUsers, enabledUsers1.subList(1, 3));
        assertAll(
                () -> assertEquals(expectedDeletedUsers.get(0).getId(), deletedUsers1.get(0).getId()),
                () -> assertEquals(expectedDeletedUsers.get(0).getUsername(), deletedUsers1.get(0).getUsername()),
                () -> assertEquals(expectedDeletedUsers.get(0).getPassword(), deletedUsers1.get(0).getPassword()),
                () -> assertEquals(expectedDeletedUsers.get(0).getRole(), deletedUsers1.get(0).getRole()),
                () -> assertEquals(expectedDeletedUsers.get(0).getRegisteredAt(), deletedUsers1.get(0).getRegisteredAt()),
                () -> assertEquals(expectedDeletedUsers.get(0).getBannedUntil(), deletedUsers1.get(0).getBannedUntil()),
                () -> assertEquals(expectedDeletedUsers.get(0).getBanCount(), deletedUsers1.get(0).getBanCount()),
                () -> assertNotEquals(expectedDeletedUsers.get(0).isDeleted(), deletedUsers1.get(0).isDeleted())
        );

        final List<User> selectedSecondly = userDao.getAll(true);
        final List<User> enabledUsers2 = selectedSecondly
                .stream()
                .filter(u -> !u.isDeleted())
                .collect(Collectors.toList());
        final List<User> deletedUsers2 = selectedSecondly
                .stream()
                .filter(User::isDeleted)
                .collect(Collectors.toList());

        actualAdmin = enabledUsers2.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());

        assertEquals(expectedEnabledUsers, enabledUsers2.subList(1, 3));
        assertAll(
                () -> assertEquals(expectedDeletedUsers.get(0).getId(), deletedUsers2.get(0).getId()),
                () -> assertEquals(expectedDeletedUsers.get(0).getUsername(), deletedUsers2.get(0).getUsername()),
                () -> assertEquals(expectedDeletedUsers.get(0).getPassword(), deletedUsers2.get(0).getPassword()),
                () -> assertEquals(expectedDeletedUsers.get(0).getRole(), deletedUsers2.get(0).getRole()),
                () -> assertEquals(expectedDeletedUsers.get(0).getRegisteredAt(), deletedUsers2.get(0).getRegisteredAt()),
                () -> assertEquals(expectedDeletedUsers.get(0).getBannedUntil(), deletedUsers2.get(0).getBannedUntil()),
                () -> assertEquals(expectedDeletedUsers.get(0).getBanCount(), deletedUsers2.get(0).getBanCount()),
                () -> assertNotEquals(expectedDeletedUsers.get(0).isDeleted(), deletedUsers2.get(0).isDeleted())
        );
    }

    @Test
    void testGetAllUsers_noUsersExists_shouldReturnListWithAdmin() throws ServerException {
        User admin = new User(
                UserRole.SUPERUSER, "admin",
                "admin@example.com", "admin_strong_pass",
                null, false
        );
        List<User> expectedUsers = Collections.singletonList(admin);

        List<User> selectedUsers = userDao.getAll();
        assertEquals(expectedUsers.size(), selectedUsers.size());

        User actualAdmin = selectedUsers.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());

        selectedUsers = userDao.getAll(true);
        assertEquals(expectedUsers.size(), selectedUsers.size());

        actualAdmin = selectedUsers.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());

        selectedUsers = userDao.getAll(false);
        assertEquals(expectedUsers.size(), selectedUsers.size());

        actualAdmin = selectedUsers.get(0);
        assertEquals(admin.getRole(), actualAdmin.getRole());
        assertEquals(admin.getEmail(), actualAdmin.getEmail());
        assertEquals(admin.getPassword(), actualAdmin.getPassword());
        assertEquals(admin.isDeleted(), actualAdmin.isDeleted());
    }

    @Test
    void testGetAllUsersWithSessions() throws ServerException {
        User admin = new User(
                1, UserRole.SUPERUSER, "admin",
                "admin@example.com", "admin_strong_pass",
                null, false
        );
        UserSession adminSession = new UserSession(admin, UUID.randomUUID().toString());
        sessionDao.upsertSession(adminSession);

        User user1 = new User(
                UserRole.SUPERUSER, "user0",
                "user0@gmail.com", "user0",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        UserSession user1Session = new UserSession(user1, UUID.randomUUID().toString());
        userDao.save(user1, user1Session);

        User user2 = new User(
                UserRole.USER, "user1",
                "user1@gmail.com", "user1",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user2);
        // doesn't have session

        User user3 = new User(
                UserRole.USER, "user2",
                "user2@gmail.com", "user2",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        UserSession user3Session = new UserSession(user3, UUID.randomUUID().toString());
        userDao.save(user3, user3Session);

        final List<UserSession> expectedUsersWithSessions = Arrays.asList(
                user1Session,
                new UserSession(user2, null),
                user3Session
        );
        final List<UserSession> actualUsersWithSessions = userDao.getAllWithSessions();
        UserSession actualAdminSession = actualUsersWithSessions.get(0);
        assertEquals(adminSession.getToken(), actualAdminSession.getToken());
        assertEquals(admin.getRole(), actualAdminSession.getUser().getRole());
        assertEquals(admin.getEmail(), actualAdminSession.getUser().getEmail());
        assertEquals(admin.getPassword(), actualAdminSession.getUser().getPassword());
        assertEquals(admin.isDeleted(), actualAdminSession.getUser().isDeleted());
        assertEquals(expectedUsersWithSessions, actualUsersWithSessions.subList(1, actualUsersWithSessions.size()));
    }

    @Test
    void testUpdateUser() throws ServerException {
        User user = new User(
                UserRole.USER,
                "House", "house@Princeton-Plainsboro.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        user.setRole(UserRole.SUPERUSER);
        user.setEmail("house@gmail.com");
        user.setPassword("newpasswd");
        userDao.update(user);

        final User selectedUser = userDao.getById(user.getId());
        assertAll(
                () -> assertEquals(user.getId(), selectedUser.getId()),
                () -> assertEquals(user.getRole(), selectedUser.getRole()),
                () -> assertEquals(user.getEmail(), selectedUser.getEmail()),
                () -> assertEquals(user.getPassword(), selectedUser.getPassword())
        );
    }

    @Test
    void testDeleteUserById() throws ServerException {
        User user = new User(
                UserRole.USER, "shermental",
                "shermental@gmail.com", "passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                false
        );
        userDao.save(user);

        final User selectedUserFirstly = userDao.getById(user.getId());
        assertEquals(user, selectedUserFirstly);

        userDao.deactivateById(user.getId());

        User selectedUserSecondly = userDao.getById(user.getId(), false);
        assertNull(selectedUserSecondly);

        selectedUserSecondly = userDao.getById(user.getId());
        assertTrue(selectedUserSecondly.isDeleted());
    }
}