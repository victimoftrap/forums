DROP DATABASE IF EXISTS forums;
CREATE DATABASE forums;
USE forums;

CREATE TABLE users(
  id            INT          PRIMARY KEY AUTO_INCREMENT,
  role          ENUM('USER', 'SUPERUSER'),
  username      VARCHAR(256) NOT NULL,
  email         VARCHAR(256) NOT NULL,
  password      VARCHAR(256) NOT NULL,
  registered_at TIMESTAMP    DEFAULT NOW(),
  deleted       BOOLEAN      NOT NULL,

  banned_until  TIMESTAMP    NULL,
  ban_count     INT          NOT NULL,

  UNIQUE KEY username(username),
  KEY        email(email),
  KEY        banned_until(banned_until),
  KEY        ban_count(ban_count)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE users_sessions(
  user_id       INT      NOT NULL,
  session_token CHAR(36) NOT NULL,

  UNIQUE KEY (user_id),
  UNIQUE KEY (session_token),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE forums(
  id         INT          PRIMARY KEY AUTO_INCREMENT,
  forum_type ENUM('MODERATED', 'UNMODERATED'),
  owner_id   INT          NOT NULL,

  name       VARCHAR(256) NOT NULL,
  readonly   BOOLEAN      NOT NULL,
  created_at TIMESTAMP    DEFAULT NOW(),

  UNIQUE  KEY name(name),
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE messages_tree(
  id           INT          PRIMARY KEY AUTO_INCREMENT,
  forum_id     INT          NOT NULL,
  subject      VARCHAR(256) NOT NULL,
  priority     ENUM('LOW', 'NORMAL', 'HIGH'),

  KEY priority(priority),
  KEY subject(subject),
  FOREIGN KEY (forum_id)     REFERENCES forums(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;
#ALTER TABLE messages ADD tree_id INT(11) NOT NULL;
#ALTER TABLE messages ADD CONSTRAINT fk_tree_id FOREIGN KEY (tree_id) REFERENCES messages_tree(id) ON DELETE CASCADE;

CREATE TABLE messages(
  id             INT       PRIMARY KEY AUTO_INCREMENT,
  owner_id       INT       NOT NULL,
  tree_id        INT       NOT NULL,
  root_message   INT       NOT NULL,
  parent_message INT       NULL,
  created_at     TIMESTAMP DEFAULT NOW(),
  updated_at     TIMESTAMP DEFAULT NOW(),
  
  FOREIGN KEY (owner_id)       REFERENCES users(id)         ON DELETE CASCADE,
  FOREIGN KEY (tree_id)        REFERENCES messages_tree(id) ON DELETE CASCADE,
  FOREIGN KEY (root_message)   REFERENCES messages(id)      ON DELETE CASCADE,
  FOREIGN KEY (parent_message) REFERENCES messages(id)      ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_history(
  message_id INT           NOT NULL,
  body       VARCHAR(4096) NOT NULL,
  state      ENUM('UNPUBLISHED', 'PUBLISHED'),
  created_at TIMESTAMP     DEFAULT NOW(),
  
  FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_ratings(
  message_id INT NOT NULL,
  user_id    INT NOT NULL,
  rating     INT NOT NULL,
  
  PRIMARY KEY (message_id, user_id),
  FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE available_tags(
  id       INT         PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50) NOT NULL,

  UNIQUE KEY tag_name(tag_name)
) ENGINE = INNODB DEFAULT CHARSET = utf8;

CREATE TABLE message_tags(
  tag_id     INT NOT NULL,
  message_id INT NOT NULL,

  FOREIGN KEY (tag_id)     REFERENCES available_tags(id) ON DELETE CASCADE,
  FOREIGN KEY (message_id) REFERENCES messages(id)       ON DELETE CASCADE
) ENGINE = INNODB DEFAULT CHARSET = utf8;

INSERT INTO users 
 (role, username, email, password, deleted, ban_count) 
VALUES('SUPERUSER', 'admin', 'admin@example.com', 'admin', FALSE, 0);
