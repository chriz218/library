INSERT INTO APP_USER
(ID, APP_USER_ROLE, ENABLED, FIRST_NAME, LAST_NAME, LOCKED, MEMBERSHIP_LEVEL, PASSWORD, USERNAME)
VALUES
('a25df130-aaed-4ba8-9aee-a7062d662da0', 'LIBRARIAN', true, 'Tony', 'Stark', false, 0, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'ironman'),
('a25df130-aaed-4ba8-9aee-a7062d662da1', 'MEMBER', true, 'Bruce', 'Wayne', false, 3, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'batman'),
('a25df130-aaed-4ba8-9aee-a7062d662da2', 'MEMBER', true, 'Clark', 'Kent', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'superman'),
('a25df130-aaed-4ba8-9aee-a7062d662da3', 'MEMBER', true, 'Wally', 'West', false, 2, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'flash'),
('a25df130-aaed-4ba8-9aee-a7062d662da4', 'MEMBER', true, 'Diana', 'Prince', false, 2, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'wonderwoman'),
('a25df130-aaed-4ba8-9aee-a7062d662da5', 'LIBRARIAN', true, 'Pepper', 'Potts', false, 0, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'rescue'),
('a25df130-aaed-4ba8-9aee-a7062d662da6', 'LIBRARIAN', true, 'Peter', 'Parker', false, 0, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'spiderman'),
('a25df130-aaed-4ba8-9aee-a7062d662da7', 'MEMBER', true, 'Steve', 'Rogers', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'captainamerica'),
('a25df130-aaed-4ba8-9aee-a7062d662da8', 'MEMBER', true, 'Carol', 'Danvers', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'captainmarvel'),
('a25df130-aaed-4ba8-9aee-a7062d662da9', 'MEMBER', true, 'Billy', 'Batson', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'shazam'),
('a25df130-aaed-4ba8-9aee-a7062d662d10', 'MEMBER', true, 'Thor', 'Odinson', false, 3, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'thor'),
('a25df130-aaed-4ba8-9aee-a7062d662d11', 'MEMBER', true, 'Peter', 'Quill', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'starlord'),
('a25df130-aaed-4ba8-9aee-a7062d662d12', 'MEMBER', true, 'Rocket', 'Raccoon', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'rocket'),
('a25df130-aaed-4ba8-9aee-a7062d662d13', 'MEMBER', true, 'James', 'Barnes', false, 1, '$2a$10$1IoutiCXVPAp9kYcrHRt1Om5M3c8ydl6lmZRLo9R0WPK/t8n11pDe', 'wintersoldier');

INSERT INTO BOOK
(ID, AUTHOR, BOOK_STATUS, ISBN, NUMBER_OF_PAGES, TITLE, BORROWER)
VALUES
('f6b00e38-9451-4e8f-bfd2-1258105a6ed1', 'John Green', 'BORROWED', '978-0-141-35367-8', 316, 'The Fault in Our Stars', 'a25df130-aaed-4ba8-9aee-a7062d662da1'),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed2', 'John Green', 'BORROWED', '978-0-141-34609-0', 213, 'An Abundance of Katherines', 'a25df130-aaed-4ba8-9aee-a7062d662da1'),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed3', 'J.R.R. Tolkien', 'BORROWED', '978-0-345-33968-3', 306, 'The Hobbit', 'a25df130-aaed-4ba8-9aee-a7062d662da2'),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed4', 'Tan Twan Eng', 'AVAILABLE', '978-1-905802-62-3', 350, 'The Garden of Evening Mists', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed5', 'Tom Wright', 'AVAILABLE', '978-0-316-45347-9', 379, 'The Billion Dollar Whale', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed6', 'Bram Stoker', 'DISCONTINUED', '1-85326-086-X', 379, 'Dracula', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed7', 'J.R.R. Tolkien', 'DISCONTINUED', '0-00-712970-X', 379, 'LOTR The Fellowship of the Ring', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed8', 'J.R.R. Tolkien', 'DISCONTINUED', '0-00-712969-X', 379, 'LOTR The Two Towers', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6ed9', 'J.R.R. Tolkien', 'DISCONTINUED', '0-00-712986-X', 379, 'LOTR The Return of the King', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e10', 'James Dashner', 'AVAILABLE', '978-0-385-73794-4', 379, 'The Maze Runner', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e11', 'James Dashner', 'AVAILABLE', '978-0-385-73875-0', 379, 'The Scorch Trials', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e12', 'James Dashner', 'AVAILABLE', '978-0-385-73877-4', 379, 'The Death Cure', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e13', 'J.K Rowling', 'BORROWED', '0545162071', 400, 'Harry Potter and the Philosophers Stone', 'a25df130-aaed-4ba8-9aee-a7062d662da9'),
('f6b00e38-9451-4e8f-bfd2-1258105a6e14', 'J.K Rowling', 'AVAILABLE', '0545162072', 400, 'Harry Potter and the Chamber of Secrets', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e15', 'J.K Rowling', 'AVAILABLE', '0545162073', 400, 'Harry Potter and the Prisoner of Azkaban', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e16', 'J.K Rowling', 'AVAILABLE', '0545162074', 400, 'Harry Potter and the Goblet of Fire', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e17', 'J.K Rowling', 'AVAILABLE', '0545162075', 400, 'Harry Potter and the Order of the Phoenix', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e18', 'J.K Rowling', 'AVAILABLE', '0545162076', 400, 'Harry Potter and the Half Blood Prince', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e19', 'J.K Rowling', 'AVAILABLE', '0545162077', 400, 'Harry Potter and the Deathly Hallows', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e20', 'Cornelia Funke', 'DISCONTINUED', '9780807219508', 300, 'Inkheart', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e21', 'Cornelia Funke', 'DISCONTINUED', '9780807219509', 300, 'Inkspell', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e22', 'Cornelia Funke', 'DISCONTINUED', '9780807219510', 300, 'Inkdeath', null),
('f6b00e38-9451-4e8f-bfd2-1258105a6e23', 'Philip Pullman', 'BORROWED', '978-0-7879-8237-9', 300, 'Northern Lights', 'a25df130-aaed-4ba8-9aee-a7062d662d10'),
('f6b00e38-9451-4e8f-bfd2-1258105a6e24', 'Philip Pullman', 'BORROWED', '978-0-7879-8238-9', 300, 'The Subtle Knife', 'a25df130-aaed-4ba8-9aee-a7062d662d10'),
('f6b00e38-9451-4e8f-bfd2-1258105a6e25', 'Philip Pullman', 'BORROWED', '978-0-7879-8239-9', 300, 'The Amber Spyglass', 'a25df130-aaed-4ba8-9aee-a7062d662d10');