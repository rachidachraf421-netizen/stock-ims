package ma.esith.stockims.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    @Test
    void creeUnArticleEtRetireLesEspaces() {
        Article article = new Article("  TIS-001 ", " Rouleau de coton  ");
        assertEquals("TIS-001", article.getReference());
        assertEquals("Rouleau de coton", article.getDesignation());
    }

    @Test
    void refuseUneReferenceVideOuTropLongue() {
        assertThrows(IllegalArgumentException.class, () -> new Article("  ", "Coton"));
        assertThrows(IllegalArgumentException.class, () -> new Article(null, "Coton"));
        assertThrows(IllegalArgumentException.class, () -> new Article("A".repeat(21), "Coton"));
    }

    @Test
    void refuseUneDesignationVideOuTropLongue() {
        assertThrows(IllegalArgumentException.class, () -> new Article("TIS-001", ""));
        assertThrows(IllegalArgumentException.class, () -> new Article("TIS-001", "D".repeat(81)));
    }
}
