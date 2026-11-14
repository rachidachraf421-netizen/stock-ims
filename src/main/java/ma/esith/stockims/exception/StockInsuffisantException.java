package ma.esith.stockims.exception;

import lombok.Getter;

@Getter
public class StockInsuffisantException extends RuntimeException {

    private final long stockDisponible;
    private final int quantiteDemandee;

    public StockInsuffisantException(long stockDisponible, int quantiteDemandee) {
        super("Sortie refusée : le stock disponible est de " + stockDisponible
                + ", la quantité demandée est de " + quantiteDemandee + ".");
        this.stockDisponible = stockDisponible;
        this.quantiteDemandee = quantiteDemandee;
    }
}
