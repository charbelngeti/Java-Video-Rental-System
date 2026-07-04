-- 1. Create the Genres Table
CREATE TABLE Genres (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    genre TEXT NOT NULL,
    isactive BOOLEAN NOT NULL CHECK (isactive IN (0, 1)) -- 0=unlisted, 1=listed
);

-- 2. Create the Movies Table 
CREATE TABLE Movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    genre_id INTEGER NOT NULL,
    Title TEXT NOT NULL,
    isactive BOOLEAN NOT NULL CHECK (isactive IN (0, 1)), -- 0=unlisted, 1=listed
    FOREIGN KEY (genre_id) REFERENCES Genres(id)
);

-- 3. Create the Clients Table (Updated with Phone & Email to support GUI)
CREATE TABLE Clients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    Fullname TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    isactive BOOLEAN NOT NULL CHECK (isactive IN (0, 1)) -- 0=unlisted, 1=listed
);

-- 4. Create the Rentals Table
CREATE TABLE Rentals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    client_id INTEGER NOT NULL,
    movie_id INTEGER NOT NULL,
    Returned BOOLEAN NOT NULL CHECK (Returned IN (0, 1)), -- 0=borrowed, 1=returned
    FOREIGN KEY (client_id) REFERENCES Clients(id),
    FOREIGN KEY (movie_id) REFERENCES Movies(id)
);