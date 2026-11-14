package ma.esith.stockims;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.service.ArticleService;
import ma.esith.stockims.service.ArticleStock;
import ma.esith.stockims.service.MouvementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccueilController {

    private final ArticleService articleService;
    private final MouvementService mouvementService;

    @GetMapping("/")
    public String accueil(Model model) {
        var message = new MessageAccueil("Bienvenue dans StockIMS");
        model.addAttribute("message", message.getTexte());

        List<ArticleStock> articles = articleService.listerAvecStock();
        model.addAttribute("nbArticles", articles.size());
        model.addAttribute("stockTotal",
                articles.stream().mapToLong(ArticleStock::getStock).sum());
        model.addAttribute("nbSansStock",
                articles.stream().filter(a -> a.getStock() == 0).count());
        model.addAttribute("nbEntrees", mouvementService.compterEntrees());
        model.addAttribute("nbSorties", mouvementService.compterSorties());
        return "accueil";
    }
}
