package ma.esith.stockims.service;

import ma.esith.stockims.exception.ArticleIntrouvableException;
import ma.esith.stockims.exception.StockInsuffisantException;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.model.Sortie;
import ma.esith.stockims.repository.ArticleRepository;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Teste la règle « une sortie ne peut pas dépasser le stock » sans base de données. */
@ExtendWith(MockitoExtension.class)
class MouvementServiceTest {

    @Mock ArticleRepository articleRepository;
    @Mock EntreeRepository entreeRepository;
    @Mock SortieRepository sortieRepository;
    @Mock StockService stockService;
    @InjectMocks MouvementService service;

    private final Article article = new Article("TIS-001", "Rouleau de coton");
    private final LocalDate date = LocalDate.of(2026, 10, 30);

    @Test
    void refuseUneSortieDe15QuandLeStockEstDe13() {
        when(articleRepository.findByIdPourMiseAJour(1L)).thenReturn(Optional.of(article));
        when(stockService.stockDe(article)).thenReturn(13L);

        StockInsuffisantException erreur = assertThrows(StockInsuffisantException.class,
                () -> service.enregistrerSortie(1L, 15, date));

        assertEquals(13, erreur.getStockDisponible());
        assertEquals(15, erreur.getQuantiteDemandee());
        verify(sortieRepository, never()).save(any());
    }

    @Test
    void accepteUneSortieEgaleAuStockDisponible() {
        when(articleRepository.findByIdPourMiseAJour(1L)).thenReturn(Optional.of(article));
        when(stockService.stockDe(article)).thenReturn(13L);
        when(sortieRepository.save(any(Sortie.class))).thenAnswer(i -> i.getArgument(0));

        Sortie sortie = service.enregistrerSortie(1L, 13, date);

        assertEquals(13, sortie.getQuantite());
        verify(sortieRepository).save(any(Sortie.class));
    }

    @Test
    void refuseUneQuantiteNulleMemeSiLeStockLePermet() {
        when(articleRepository.findByIdPourMiseAJour(1L)).thenReturn(Optional.of(article));
        when(stockService.stockDe(article)).thenReturn(13L);

        assertThrows(IllegalArgumentException.class, () -> service.enregistrerSortie(1L, 0, date));
        verify(sortieRepository, never()).save(any());
    }

    @Test
    void refuseUneEntreePourUnArticleInconnu() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ArticleIntrouvableException.class, () -> service.enregistrerEntree(99L, 5, date));
        verify(entreeRepository, never()).save(any());
    }
}
