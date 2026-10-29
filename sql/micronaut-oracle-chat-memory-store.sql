-- OracleChatMemoryStore does not create its own table.
-- Oracle AI Database 26ai supports CREATE TABLE IF NOT EXISTS, so this DDL is idempotent.
-- CONTENT is a CLOB because a serialized MessageWindow can exceed VARCHAR2 limits.
CREATE TABLE IF NOT EXISTS CHAT_MEMORY (
    MEMORY_ID VARCHAR2(128 CHAR) NOT NULL,
    CONTENT   CLOB NOT NULL,
    CONSTRAINT CHAT_MEMORY_PK PRIMARY KEY (MEMORY_ID)
);
