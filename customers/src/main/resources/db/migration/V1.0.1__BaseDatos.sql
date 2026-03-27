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

INSERT INTO person (name, address, phone, identification_number, gender)
VALUES ('Jose Lema', 'Otavalo sn y principal', '098254785', '1234567890', 'MASCULINO'),
       ( 'Marianela Montalvo', 'Amazonas y NNUU', '097548965', '1987654321', 'FEMENINO'),
       ( 'Juan Osorio', '13 junio y Equinoccial', '098874587', '1122334455', 'MASCULINO');

INSERT INTO customer (state, password, person_id)
VALUES (TRUE, 'password123', (SELECT id FROM person WHERE identification_number = '1234567890')),
       ( TRUE, 'securepass456', (SELECT id FROM person WHERE identification_number = '1987654321')),
       ( TRUE, 'mypassword789', (SELECT id FROM person WHERE identification_number = '1122334455'));