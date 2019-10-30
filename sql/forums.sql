DROP DATABASE IF EXISTS forums;
CREATE DATABASE forums;
USE forums;

# Table that contains user data
CREATE TABLE users(
  id            INT(11)      PRIMARY KEY AUTO_INCREMENT,
  role          ENUM('USER', 'SUPERUSER'),
  username      VARCHAR(256) NOT NULL,
  email         VARCHAR(256) NOT NULL,
  password      VARCHAR(256) NOT NULL,
  registered_at TIMESTAMP    DEFAULT NOW(),

  banned_until  TIMESTAMP    NULL,
  ban_count     INT(3)       NOT NULL,
  permanent     BOOLEAN      NOT NULL,

  UNIQUE KEY username(username),
  KEY        email(email),
  KEY        banned_until(banned_until),
  KEY        ban_count(ban_count)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE users_sessions(
  user_id       INT(11)  NOT NULL,
  session_token CHAR(36) NOT NULL,

  PRIMARY KEY (user_id, session_token),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE forums(
  id         INT(11)      PRIMARY KEY AUTO_INCREMENT,
  forum_type ENUM('MODERATED', 'UNMODERATED'),
  owner_id   INT(11)      NOT NULL,

  name       VARCHAR(256) NOT NULL,
  readonly   BOOLEAN      NOT NULL,
  created_at TIMESTAMP    DEFAULT NOW(),

  UNIQUE KEY name(name),
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE forum_messages(
  id         INT(11)       PRIMARY KEY AUTO_INCREMENT,
  forum_id   INT(11)       NOT NULL,
  owner_id   INT(11)       NOT NULL,

  refer_to   INT(11)       NULL,
  state      ENUM('UNPUBLISHED', 'PUBLISHED'),
  priority   ENUM('LOW', 'NORMAL', 'HIGH'),
  subject  VARCHAR(256)  NULL,
  body       VARCHAR(4096) NOT NULL,
  rating     INT(2)        NOT NULL,
  created_at TIMESTAMP     DEFAULT NOW(),
  updated_at TIMESTAMP     DEFAULT NOW(),

  KEY subject(subject),
  KEY rating(rating),
  FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
  FOREIGN KEY (owner_id) REFERENCES users(id)  ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that contains all created tags for posts on server
CREATE TABLE available_tags(
  id       INT(11)     PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50) NOT NULL,

  UNIQUE KEY tag_name(tag_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

# Table that connects posts with tags
CREATE TABLE message_tags(
  tag_id     INT(11) NOT NULL,
  message_id INT(11) NOT NULL,

  FOREIGN KEY (tag_id)     REFERENCES available_tags(id) ON DELETE CASCADE,
  FOREIGN KEY (message_id) REFERENCES forum_messages(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;
