CREATE TABLE IF NOT EXISTS account
(
    id
    SERIAL
    PRIMARY
    KEY,
    numero
    VARCHAR
(
    255
) NOT NULL,
    type VARCHAR
(
    255
) NOT NULL,
    balance NUMERIC
(
    15,
    2
) NOT NULL,
    state BOOLEAN NOT NULL,
    person_identification_number VARCHAR
(
    255
) NOT NULL);

CREATE TABLE IF NOT EXISTS movimiento
(
    id
    SERIAL
    PRIMARY
    KEY,
    date
    TIMESTAMP
(
    6
) NOT NULL,
    type VARCHAR
(
    64
) NOT NULL,
    value NUMERIC
(
    38,
    2
) NOT NULL,
    initial_balance NUMERIC
(
    38,
    2
) NOT NULL,
    available_balance NUMERIC
(
    38,
    2
) NOT NULL,
    state BOOLEAN NOT NULL,
    key_movement VARCHAR
(
    255
) NOT NULL,
    account_id INTEGER NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES account
(
    id
)
    );

INSERT INTO account (number, type, balance, state, person_identification_number)
VALUES ('478758', 'Ahorros', 2000.00, TRUE, '1234567890'),
       ('225487', 'Corriente', 100.00, TRUE, '1987654321'),
       ('495878', 'Ahorros', 0.00, TRUE, '1122334455'),
       ('496825', 'Ahorros', 540.00, TRUE, '1987654321'),
       ('585545', 'Corriente', 1000.00, TRUE, '1234567890');

INSERT INTO movimiento (date, type, value, initial_balance, available_balance, state, account_id, key_movement)
VALUES ('2024-01-10 10:00:00', 'Débito', 500.00, 2000.00, 1500.00, TRUE,
        (SELECT id FROM account where number = '478758'), 'TXN1001'),
       ('2024-01-12 14:30:00', 'Crédito', 600.00, 100.00, 700.00, TRUE,
        (SELECT id FROM account where number = '225487'), 'TXN1002'),
       ('2024-01-15 09:15:00', 'Crédito', 150.00, 0.00, 150.00, TRUE, (SELECT id FROM account where number = '495878'),
        'TXN1003'),
       ('2024-01-18 16:45:00', 'Débito', 540.00, 540.00, 0.00, TRUE, (SELECT id FROM account where number = '496825'),
        'TXN1004');