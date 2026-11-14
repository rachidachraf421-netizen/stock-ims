package ma.esith.stockims.service;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock EntreeRepository entreeRepository;
    @Mock SortieRepository sortieRepository;
    @InjectMocks StockService service;

    private final Article article = new Article("TIS-001", "Rouleau de coton");

    @Test
    void unArticleSansMouvementAUnStockNul() {
        when(entreeRepository.totalQuantite(article)).thenReturn(null);
        when(sortieRepository.totalQuantite(article)).thenReturn(null);
        assertEquals(0, service.stockDe(article));
    }

    @Test
    void stockEgalEntreesMoinsSorties() {
        when(entreeRepository.totalQuantite(article)).thenReturn(20L);
        when(sortieRepository.totalQuantite(article)).thenReturn(7L);
        assertEquals(13, service.stockDe(article));
    }

    @Test
    void stocksParArticleCombineEntreesEtSorties() {
        when(entreeRepository.totauxParArticle()).thenReturn(List.<Object[]>of(
                new Object[]{1L, 20L}, new Object[]{2L, 50L}));
        when(sortieRepository.totauxParArticle()).thenReturn(List.<Object[]>of(
                new Object[]{1L, 7L}));

        Map<Long, Long> stocks = service.stocksParArticle();

        assertEquals(13L, stocks.get(1L));
        assertEquals(50L, stocks.get(2L));
        assertNull(stocks.get(3L));   // pas de mouvement : absent de la map, donc stock 0
    }
}
