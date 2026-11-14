package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Calcul du stock : somme des entrées - somme des sorties.
 * Le stock n'est jamais stocké : il est recalculé à partir des mouvements.
 */
@Service @RequiredArgsConstructor
public class StockService {

    private final EntreeRepository entreeRepository;
    private final SortieRepository sortieRepository;

    @Transactional(readOnly = true)
    public long stockDe(Article article) {
        return valeur(entreeRepository.totalQuantite(article))
                - valeur(sortieRepository.totalQuantite(article));
    }

    /** Stock de tous les articles en deux requêtes. Un article absent de la map a un stock nul. */
    @Transactional(readOnly = true)
    public Map<Long, Long> stocksParArticle() {
        Map<Long, Long> stocks = new HashMap<>();
        for (Object[] ligne : entreeRepository.totauxParArticle()) {
            stocks.merge((Long) ligne[0], (Long) ligne[1], Long::sum);
        }
        for (Object[] ligne : sortieRepository.totauxParArticle()) {
            stocks.merge((Long) ligne[0], -((Long) ligne[1]), Long::sum);
        }
        return stocks;
    }

    private long valeur(Long somme) {
        return somme == null ? 0L : somme;
    }
}
