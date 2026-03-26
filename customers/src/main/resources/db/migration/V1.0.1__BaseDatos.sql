CREATE TABLE IF NOT EXISTS person
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    gender VARCHAR
(
    50
),
    address VARCHAR
(
    255
),
    phone VARCHAR
(
    50
),
    identification_number VARCHAR
(
    255
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS customer
(
    id
    SERIAL
    PRIMARY
    KEY,
    state
    BOOLEAN
    NOT
    NULL,
    password
    VARCHAR
(
    255
) NOT NULL,
    person_id INTEGER NOT NULL,
    FOREIGN KEY
(
    person_id
) REFERENCES person
(
    id
)
    );

INSERT INTO person (id, name, address, phone, identification_number, gender)
VALUES (1, 'Jose Lema', 'Otavalo sn y principal', '098254785', '1234567890', 'MASCULINO'),
       (2, 'Marianela Montalvo', 'Amazonas y NNUU', '097548965', '1987654321', 'FEMENINO'),
       (3, 'Juan Osorio', '13 junio y Equinoccial', '098874587', '1122334455', 'MASCULINO');

INSERT INTO customer (id, state, password, person_id)
VALUES (1, TRUE, 'password123', 1),
       (2, TRUE, 'securepass456', 2),
       (3, TRUE, 'mypassword789', 3);