DROP DATABASE IF EXISTS forums;
CREATE DATABASE forums;
USE forums;

CREATE TABLE users(
  id            INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  role          ENUM('USER', 'SUPERUSER'),
  username      VARCHAR(256)     NOT NULL,
  email         VARCHAR(256)     NOT NULL,
  password      VARCHAR(256)     NOT NULL,
  registered_at TIMESTAMP        DEFAULT NOW(),
  deleted       BOOLEAN          NOT NULL,

  banned_until  TIMESTAMP        NULL,
  ban_count     INT(3)           NOT NULL,

  UNIQUE KEY username(username),
  KEY        email(email),
  KEY        banned_until(banned_until),
  KEY        ban_count(ban_count)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE users_sessions(
  user_id       INT(11) UNSIGNED NOT NULL,
  session_token CHAR(36)         NOT NULL,

  PRIMARY KEY (user_id, session_token),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE forums(
  id         INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  forum_type ENUM('MODERATED', 'UNMODERATED'),
  owner_id   INT(11) UNSIGNED NOT NULL,

  name       VARCHAR(256)     NOT NULL,
  readonly   BOOLEAN          NOT NULL,
  created_at TIMESTAMP        DEFAULT NOW(),

  UNIQUE  KEY name(name),
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE messages(
  id             INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  owner_id       INT(11) UNSIGNED NOT NULL,
  parent_message INT(11) UNSIGNED NULL,
  created_at     TIMESTAMP DEFAULT NOW(),
  updated_at     TIMESTAMP DEFAULT NOW(),
  
  FOREIGN KEY (owner_id)       REFERENCES users(id)    ON DELETE CASCADE,
  FOREIGN KEY (parent_message) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE messages_tree(
  id           INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  forum_id     INT(11) UNSIGNED NOT NULL,
  root_message INT(11) UNSIGNED NOT NULL,
  subject      VARCHAR(256)     NOT NULL,
  priority     ENUM('LOW', 'NORMAL', 'HIGH'),

  KEY priority(priority),
  KEY subject(subject),
  FOREIGN KEY (forum_id)     REFERENCES forums(id)   ON DELETE CASCADE,
  FOREIGN KEY (root_message) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_history(
  message_id INT(11) UNSIGNED NOT NULL,
  body       VARCHAR(4096)    NOT NULL,
  state      ENUM('UNPUBLISHED', 'PUBLISHED'),
  created_at TIMESTAMP DEFAULT NOW(),
  
  FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_ratings(
  message_id INT(11) UNSIGNED NOT NULL,
  user_id    INT(11) UNSIGNED NOT NULL,
  rating     INT(2)           NOT NULL,
  
  PRIMARY KEY (message_id, user_id),
  FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE available_tags(
  id       INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50)      NOT NULL,

  UNIQUE KEY tag_name(tag_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_tags(
  tag_id     INT(11) UNSIGNED NOT NULL,
  message_id INT(11) UNSIGNED NOT NULL,

  FOREIGN KEY (tag_id)     REFERENCES available_tags(id) ON DELETE CASCADE,
  FOREIGN KEY (message_id) REFERENCES messages(id)       ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;
