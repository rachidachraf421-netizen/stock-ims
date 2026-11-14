package ma.esith.stockims.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MouvementStockTest {

    private final Article article = new Article("TIS-001", "Rouleau de coton");
    private final LocalDate date = LocalDate.of(2026, 10, 30);

    @Test
    void uneEntreeAUnEffetPositifEtUneSortieUnEffetNegatif() {
        assertEquals(20, new Entree(article, 20, date).quantiteSignee());
        assertEquals(-7, new Sortie(article, 7, date).quantiteSignee());
    }

    @Test
    void laQuantiteEnregistreeResteToujoursPositive() {
        assertEquals(7, new Sortie(article, 7, date).getQuantite());
    }

    @Test
    void refuseUneQuantiteNulleOuNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Entree(article, 0, date));
        assertThrows(IllegalArgumentException.class, () -> new Sortie(article, -3, date));
    }

    @Test
    void refuseUnArticleOuUneDateAbsents() {
        assertThrows(IllegalArgumentException.class, () -> new Entree(null, 5, date));
        assertThrows(IllegalArgumentException.class, () -> new Entree(article, 5, null));
    }

    @Test
    void lePolymorphismeDonneLeStockDuScenario() {
        List<MouvementStock> mouvements = List.of(
                new Entree(article, 20, date),
                new Sortie(article, 7, date));
        int stock = 0;
        for (MouvementStock m : mouvements) {
            stock += m.quantiteSignee();
        }
        assertEquals(13, stock);
    }
}
