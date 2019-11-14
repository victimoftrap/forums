package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest extends DaoTestBase {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    void testInsertNewUser() {
        User user = new User(
                UserRole.USER,
                "shermental", "shermental@gmail.com", "passwd",
                LocalDateTime.now(),
                false
        );

        final User insertedUser = userDao.save(user);
        assertAll(
                () -> assertNotEquals(0, insertedUser),
                () -> assertNotEquals(0, user),
                () -> assertEquals(user, insertedUser)
        );
    }

    @Test
    void testInsertNewUser_nullParam_userNotCreated() {
        final User user = new User(
                UserRole.USER,
                null, "shermental@gmail.com", "passwd",
                LocalDateTime.now(),
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
                /*Timestamp.valueOf(
                        format.format(Timestamp.from(Instant.now()))
                )*/
                false
        );
        userDao.save(user);

        final User selectedUser = userDao.getById(user.getId());
        assertEquals(user, selectedUser);
    }

    @Test
    void testGetUserById_userNotExists_notFound() {
        final User selectedUser = userDao.getById(1256);
        assertNull(selectedUser);
    }

    @Test
    void testUpdateUser() {
        User user = new User(
                UserRole.USER,
                "House", "house@Princeton-Plainsboro.com", "passwd",
                LocalDateTime.now(),
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
        assertTrue(selectedUserSecondly.isAreDeleted());
    }
}