package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.exception.ArticleIntrouvableException;
import ma.esith.stockims.exception.StockInsuffisantException;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.model.Entree;
import ma.esith.stockims.model.MouvementStock;
import ma.esith.stockims.model.Sortie;
import ma.esith.stockims.repository.ArticleRepository;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor
public class MouvementService {

    private final ArticleRepository articleRepository;
    private final EntreeRepository entreeRepository;
    private final SortieRepository sortieRepository;
    private final StockService stockService;

    @Transactional
    public Entree enregistrerEntree(Long articleId, int quantite, LocalDate date) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleIntrouvableException(articleId));
        return entreeRepository.save(new Entree(article, quantite, date));
    }

    /** Enregistre une sortie, sauf si elle dépasse le stock disponible. */
    @Transactional
    public Sortie enregistrerSortie(Long articleId, int quantite, LocalDate date) {
        Article article = articleRepository.findByIdPourMiseAJour(articleId)
                .orElseThrow(() -> new ArticleIntrouvableException(articleId));
        long disponible = stockService.stockDe(article);
        if (quantite > disponible) {
            throw new StockInsuffisantException(disponible, quantite);
        }
        return sortieRepository.save(new Sortie(article, quantite, date));
    }

    /** Historique d'un article : entrées et sorties mélangées, plus récent d'abord. */
    @Transactional(readOnly = true)
    public List<MouvementStock> historique(Article article) {
        List<MouvementStock> mouvements = new ArrayList<>();
        mouvements.addAll(entreeRepository.findByArticle(article));
        mouvements.addAll(sortieRepository.findByArticle(article));
        return trier(mouvements);
    }

    /** Historique de tous les articles, plus récent d'abord. */
    @Transactional(readOnly = true)
    public List<MouvementStock> tousLesMouvements() {
        List<MouvementStock> mouvements = new ArrayList<>();
        mouvements.addAll(entreeRepository.findAll());
        mouvements.addAll(sortieRepository.findAll());
        return trier(mouvements);
    }

    public long compterEntrees() {
        return entreeRepository.count();
    }

    public long compterSorties() {
        return sortieRepository.count();
    }

    private List<MouvementStock> trier(List<MouvementStock> mouvements) {
        mouvements.sort(Comparator.comparing(MouvementStock::getDateMouvement).reversed());
        return mouvements;
    }
}
