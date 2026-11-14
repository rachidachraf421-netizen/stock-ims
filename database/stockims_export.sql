-- ============================================================================
-- StockIMS - export SQL : structure et données de test
-- MySQL 8.4, jeu de caractères utf8mb4
--
-- Utilisation (facultative : l'application crée elle-même les tables
-- grâce à spring.jpa.hibernate.ddl-auto=update) :
--   mysql -u root -p < database/stockims_export.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS stock_ims CHARACTER SET utf8mb4;
USE stock_ims;

-- ---------------------------------------------------------------------------
-- Structure (identique à celle générée par Hibernate pour les entités)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    reference   VARCHAR(20)  NOT NULL,
    designation VARCHAR(80)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_article_reference UNIQUE (reference)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS entree (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    article_id     BIGINT  NOT NULL,
    quantite       INT     NOT NULL,
    date_mouvement DATE    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_entree_article FOREIGN KEY (article_id) REFERENCES article (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sortie (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    article_id     BIGINT  NOT NULL,
    quantite       INT     NOT NULL,
    date_mouvement DATE    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sortie_article FOREIGN KEY (article_id) REFERENCES article (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Données de test (à n'importer que sur une base vide)
-- ---------------------------------------------------------------------------
INSERT INTO article (id, reference, designation) VALUES
    (1, 'TIS-001', 'Rouleau de coton'),
    (2, 'TIS-002', 'Rouleau de lin'),
    (3, 'FIL-001', 'Bobine de fil'),
    (4, 'BOU-001', 'Boîte de boutons');

-- TIS-001 : scénario du sujet. Stock initial 0, entrée de 20, sortie de 7 : stock 13.
-- (Une sortie de 15 est refusée par l'application ; elle n'apparaît donc pas ici.)
INSERT INTO entree (article_id, quantite, date_mouvement) VALUES
    (1, 20, '2026-10-26'),
    (2, 50, '2026-10-26'),
    (3, 120, '2026-10-27'),
    (3, 30, '2026-10-29');

INSERT INTO sortie (article_id, quantite, date_mouvement) VALUES
    (1, 7, '2026-10-28'),
    (3, 45, '2026-10-30');

-- BOU-001 n'a aucun mouvement : son stock est nul.

ALTER TABLE article AUTO_INCREMENT = 5;

-- ---------------------------------------------------------------------------
-- Vérification : stock = somme des entrées - somme des sorties
-- Résultat attendu : BOU-001 = 0, FIL-001 = 105, TIS-001 = 13, TIS-002 = 50
-- ---------------------------------------------------------------------------
SELECT a.reference,
       a.designation,
       COALESCE((SELECT SUM(e.quantite) FROM entree e WHERE e.article_id = a.id), 0)
     - COALESCE((SELECT SUM(s.quantite) FROM sortie s WHERE s.article_id = a.id), 0) AS stock
FROM article a
ORDER BY a.reference;
