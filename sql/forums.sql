DROP DATABASE IF EXISTS forums;
CREATE DATABASE forums;
USE forums;

CREATE TABLE users(
  id            INT(11)      PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(256) NOT NULL,
  email         VARCHAR(256) NOT NULL,
  password      VARCHAR(256) NOT NULL,
  registered_at TIMESTAMP    DEFAULT NOW(),

  UNIQUE KEY username(username),
  KEY        email(email)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table with roles that user can have
# USER - regular user
# SUPERUSER - user with special rights on server
CREATE TABLE roles(
  id   INT(11) PRIMARY KEY AUTO_INCREMENT,
  role VARCHAR(256),

  UNIQUE KEY role(role)
) ENGINE = INNODB DEFAULT CHARSET = utf8;
INSERT INTO roles (role) VALUES('USER');
INSERT INTO roles (role) VALUES('SUPERUSER');

CREATE TABLE user_roles(
  user_id INT(11) NOT NULL,
  role_id INT(11) NOT NULL,

  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE users_sessions(
  user_id       INT(11)  NOT NULL,
  session_token CHAR(36) NOT NULL,

  PRIMARY KEY (user_id, session_token),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE banned_users(
  id           INT(11)   PRIMARY KEY AUTO_INCREMENT,
  user_id      INT(11)   NOT NULL,
  banned_until TIMESTAMP NOT NULL,
  ban_count    INT(3)    NOT NULL,
  permanent    BOOLEAN   NOT NULL,

  KEY user_id(user_id),
  KEY banned_until(banned_until),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that contains available forum types on server
CREATE TABLE forum_types(
  id         INT(11)      PRIMARY KEY AUTO_INCREMENT,
  forum_type VARCHAR(100) NOT NULL,

  UNIQUE KEY forum_type(forum_type)
) ENGINE = INNODB DEFAULT CHARSET = utf8;
INSERT INTO forum_types (forum_type) VALUES('MODERATED');
INSERT INTO forum_types (forum_type) VALUES('UNMODERATED');

CREATE TABLE forums(
  id         INT(11)      PRIMARY KEY AUTO_INCREMENT,
  name       VARCHAR(256) NOT NULL,
  type_id    INT(11)      NOT NULL,
  owner_id   INT(11)      NOT NULL,
  readonly   BOOLEAN      NOT NULL,
  created_at TIMESTAMP    DEFAULT NOW(),

  UNIQUE KEY name(name),
  FOREIGN KEY (type_id)  REFERENCES forum_types(id) ON DELETE CASCADE,
  FOREIGN KEY (owner_id) REFERENCES users(id)       ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that contains available priority for messages
CREATE TABLE message_priorities(
  id            INT(11)     PRIMARY KEY AUTO_INCREMENT,
  priority_name VARCHAR(50) NOT NULL,

  UNIQUE KEY priority_name(priority_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;
INSERT INTO message_priorities (priority_name) VALUES('LOW');
INSERT INTO message_priorities (priority_name) VALUES('NORMAL');
INSERT INTO message_priorities (priority_name) VALUES('HIGH');

# Table that contains possible stages of post in forum
CREATE TABLE post_states(
  id         INT(11)     PRIMARY KEY AUTO_INCREMENT,
  state_name VARCHAR(50) NOT NULL,

  UNIQUE KEY state_name(state_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;
INSERT INTO post_states (state_name) VALUES('PUBLISHED');
INSERT INTO post_states (state_name) VALUES('UNPUBLISHED');

CREATE TABLE posts(
  id          INT(11)       PRIMARY KEY AUTO_INCREMENT,
  forum_id    INT(11)       NOT NULL,
  owner_id    INT(11)       NOT NULL,
  refer_to    INT(11)       NULL,
  state_id    INT(11)       NOT NULL,
  priority_id INT(11)       NULL,
  subject     VARCHAR(256)  NULL,
  body        VARCHAR(4096) NOT NULL,
  rating      INT(2)        NOT NULL,
  created_at  TIMESTAMP     DEFAULT NOW(),
  updated_at  TIMESTAMP     DEFAULT NOW(),

  KEY subject(subject),
  KEY rating(rating),
  FOREIGN KEY (forum_id)    REFERENCES forum(id)              ON DELETE CASCADE,
  FOREIGN KEY (owner_id)    REFERENCES users(id)              ON DELETE CASCADE,
  FOREIGN KEY (state_id)    REFERENCES post_states(id)        ON DELETE CASCADE,
  FOREIGN KEY (priority_id) REFERENCES message_priorities(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that contains all created tags for posts on server
CREATE TABLE available_tags(
  id       INT(11)     PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50) NOT NULL,

  UNIQUE KEY tag_name(tag_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that connects posts with tags
CREATE TABLE post_tags(
  tag_id  INT(11) NOT NULL,
  post_id INT(11) NOT NULL,

  FOREIGN KEY (tag_id)  REFERENCES available_tags(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id)          ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;
