package net.thumbtack.forums.daoimpl;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.UserRoles;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest extends DaoTestBase {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    void testInsertNewUser() {
        User user = new User(
                UserRoles.USER, "shermental",
                "shermental@gmail.com", "passwd",
                Timestamp.from(Instant.now())
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
                UserRoles.USER, null,
                "shermental@gmail.com", "passwd",
                Timestamp.from(Instant.now())
        );

        assertThrows(RuntimeException.class,
                () -> userDao.save(user)
        );
    }

    @Test
    void testGetUserById() {
        User user = new User(
                UserRoles.USER, "shermental",
                "shermental@gmail.com", "passwd",
                Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                /*Timestamp.valueOf(
                        format.format(Timestamp.from(Instant.now()))
                )*/
        );
        userDao.save(user);

        final User selectedUser = userDao.findById(user.getId());

        assertEquals(user, selectedUser);
    }

    @Test
    void testGetUserById_userNotExists_notFound() {
        final User selectedUser = userDao.findById(1256);
        assertNull(selectedUser);
    }

    @Test
    void testUpdateUser() {
        User user = new User(
                UserRoles.USER, "House",
                "house@Princeton-Plainsboro.com", "passwd",
                Timestamp.from(Instant.now())
        );
        userDao.save(user);

        user.setRole(UserRoles.SUPERUSER);
        user.setEmail("house@gmail.com");
        user.setPassword("newpasswd");
        userDao.update(user);

        final User selectedUser = userDao.findById(user.getId());

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
                UserRoles.USER, "shermental",
                "shermental@gmail.com", "passwd",
                Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        );
        userDao.save(user);
        final User selectedUserFirstly = userDao.findById(user.getId());

        assertEquals(user, selectedUserFirstly);

        userDao.deleteById(user.getId());
        final User selectedUserSecondly = userDao.findById(user.getId());

        assertNull(selectedUserSecondly);
    }
}