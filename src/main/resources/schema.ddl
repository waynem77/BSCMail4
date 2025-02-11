CREATE TABLE group_permission
(
    group_id      bigint NOT NULL,
    permission_id bigint NOT NULL,
    PRIMARY KEY (group_id, permission_id)
);
CREATE TABLE groupp
(
    id   bigint NOT NULL,
    name TEXT   NOT NULL UNIQUE,
    PRIMARY KEY (id)
);
CREATE TABLE groupp_seq
(
    next_val bigint
);
INSERT INTO groupp_seq
VALUES (1);
CREATE TABLE permission
(
    id   bigint NOT NULL,
    name TEXT UNIQUE,
    PRIMARY KEY (id)
);
CREATE TABLE permission_seq
(
    next_val bigint
);
INSERT INTO permission_seq
VALUES (1);
CREATE TABLE person
(
    active        BOOLEAN NOT NULL,
    id            bigint  NOT NULL,
    email_address TEXT    NOT NULL,
    name          TEXT    NOT NULL,
    phone         TEXT,
    PRIMARY KEY (id)
);
CREATE TABLE person_group
(
    group_id  bigint NOT NULL,
    person_id bigint NOT NULL
);
CREATE TABLE person_permissions
(
    permissions_id bigint NOT NULL,
    person_id      bigint NOT NULL,
    PRIMARY KEY (permissions_id, person_id)
);
CREATE TABLE person_seq
(
    next_val bigint
);
INSERT INTO person_seq
VALUES (1);
CREATE TABLE shift_template
(
    id                     bigint NOT NULL,
    required_permission_id bigint,
    name                   TEXT   NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE shift_template_seq
(
    next_val bigint
);
INSERT INTO shift_template_seq
VALUES (1);
