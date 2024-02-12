CREATE TABLE accounts(
   account_id serial PRIMARY KEY,
   name  VARCHAR (150),
   email VARCHAR (100) UNIQUE NOT NULL,
   birthdate DATE,
   last_payment_date TIMESTAMP,
   created_on TIMESTAMP DEFAULT NOW()
);

CREATE TABLE payments(
   payment_id VARCHAR (100) PRIMARY KEY,
   account_id INTEGER REFERENCES accounts(account_id) ON DELETE RESTRICT,
   payment_type  VARCHAR (150) NOT NULL,
   credit_card VARCHAR (100),
   amount NUMERIC NOT NULL,
   created_on TIMESTAMP DEFAULT NOW()
);
