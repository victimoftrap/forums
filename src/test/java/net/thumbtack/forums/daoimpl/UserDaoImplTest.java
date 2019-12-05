package net.thumbtack.forums.daoimpl;

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
    void testSaveUser() {
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
    void testSaveUserAndHisSession() {
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

        assertThrows(RuntimeException.class,
                () -> userDao.save(user)
        );
    }

    @Test
    void testGetUserById() {
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
    void testGetUserById_userNotExists_shouldNotFindUser() {
        final User selectedUser = userDao.getById(1256);
        assertNull(selectedUser);
    }

    @Test
    void testGetUserThatMayBeDeletedById_userDeleted_shouldFindUser() {
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
    void testGetUserById_userDeleted_shouldNotFindUser() {
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
    void testGetUserByName() {
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
    void testGetUserByName_userNotExists_shouldNotFindUser() {
        final User selectedUser = userDao.getByName("catchMeIfYouCan");
        assertNull(selectedUser);
    }

    @Test
    void testGetUserThatMayBeDeletedByName_userDeleted_shouldFindUser() {
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
    void testGetUserByName_userDeleted_shouldNotFindUser() {
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
    void testGetAllUsers() {
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
        assertIterableEquals(Arrays.asList(user1, user2, user3), users);
    }

    @Test
    void testGetAllUsers_someUsersDeleted_shouldReturnOnlyEnabledUsers() {
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
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    void testGetAllUsers_someUsersDeleted_shouldReturnAllUsers() {
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
        assertEquals(expectedEnabledUsers, enabledUsers1);
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
        assertEquals(expectedEnabledUsers, enabledUsers2);
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
    void testGetAllUsers_noUsersExists_shouldReturnEmptyList() {
        List<User> selectedUsers = userDao.getAll();
        assertEquals(Collections.EMPTY_LIST, selectedUsers);

        selectedUsers = userDao.getAll(true);
        assertEquals(Collections.EMPTY_LIST, selectedUsers);

        selectedUsers = userDao.getAll(false);
        assertEquals(Collections.EMPTY_LIST, selectedUsers);
    }

    @Test
    void testGetAllUsersWithSessions() {
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
        assertEquals(expectedUsersWithSessions, actualUsersWithSessions);
    }

    @Test
    void testUpdateUser() {
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
    void testDeleteUserById() {
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