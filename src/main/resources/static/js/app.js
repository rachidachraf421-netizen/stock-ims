/* StockIMS - interactions
   Le site fonctionne sans JavaScript : les validations sont faites côté serveur.
   Ce script ajoute seulement du confort :
   1. menu replié sur petit écran ;
   2. filtre de recherche dans les tableaux ;
   3. stock disponible affiché à l'écran de saisie d'un mouvement ;
   4. confirmation avant d'enregistrer une sortie. */
(function () {
    'use strict';

    document.documentElement.classList.add('js');

    function normaliser(texte) {
        return (texte || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }

    /* 1. Menu sur petit écran */
    var boutonMenu = document.querySelector('.menu-bouton');
    var navigation = document.getElementById('navigation');
    if (boutonMenu && navigation) {
        boutonMenu.addEventListener('click', function () {
            var ouvert = navigation.classList.toggle('ouverte');
            boutonMenu.setAttribute('aria-expanded', String(ouvert));
        });
    }

    /* 2. Filtre de recherche (insensible à la casse et aux accents) */
    var champRecherche = document.querySelector('[data-filtre]');
    if (champRecherche) {
        var tableau = document.querySelector(champRecherche.getAttribute('data-filtre'));
        var messageVide = document.getElementById(champRecherche.getAttribute('data-vide'));
        var compteur = document.getElementById(champRecherche.getAttribute('data-compteur'));
        var lignes = tableau ? Array.prototype.slice.call(tableau.querySelectorAll('tbody tr')) : [];
        var textes = lignes.map(function (ligne) { return normaliser(ligne.textContent); });

        var filtrer = function () {
            var terme = normaliser(champRecherche.value.trim());
            var visibles = 0;
            lignes.forEach(function (ligne, i) {
                var visible = terme === '' || textes[i].indexOf(terme) !== -1;
                ligne.hidden = !visible;
                if (visible) { visibles++; }
            });
            if (messageVide) { messageVide.hidden = visibles !== 0; }
            if (compteur) {
                compteur.textContent = terme === ''
                    ? lignes.length + ' au total'
                    : visibles + ' sur ' + lignes.length + ' affiché' + (visibles > 1 ? 's' : '');
            }
        };
        champRecherche.addEventListener('input', filtrer);
        filtrer();
    }

    /* 3 et 4. Formulaire d'entrée ou de sortie */
    var formulaire = document.querySelector('form[data-type]');
    if (formulaire) {
        var type = formulaire.getAttribute('data-type');
        var liste = formulaire.querySelector('select');
        var champQuantite = formulaire.querySelector('input[type="number"]');
        var indicateur = document.getElementById('indicateur-stock');

        var optionChoisie = function () {
            var option = liste.options[liste.selectedIndex];
            return option && option.value !== '' ? option : null;
        };
        var lireQuantite = function () {
            var saisie = champQuantite.value.trim();
            return /^[0-9]+$/.test(saisie) && parseInt(saisie, 10) > 0 ? parseInt(saisie, 10) : null;
        };

        var majIndicateur = function () {
            if (!indicateur) { return; }
            var option = optionChoisie();
            indicateur.classList.remove('depassement');
            if (!option) { indicateur.textContent = ''; return; }
            var stock = parseInt(option.getAttribute('data-stock'), 10);
            var quantite = lireQuantite();
            var texte = 'Stock disponible : ' + stock;
            if (type === 'sortie' && quantite !== null && quantite > stock) {
                texte += '. La quantité saisie dépasse le stock : la sortie sera refusée.';
                indicateur.classList.add('depassement');
            }
            indicateur.textContent = texte;
        };

        liste.addEventListener('change', majIndicateur);
        champQuantite.addEventListener('input', majIndicateur);
        majIndicateur();

        formulaire.addEventListener('submit', function (evenement) {
            if (type !== 'sortie') { return; }
            var option = optionChoisie();
            var quantite = lireQuantite();
            // Champ vide ou invalide : on laisse le serveur afficher les erreurs.
            if (!option || quantite === null) { return; }
            var question = 'Confirmer la sortie de ' + quantite + ' unité(s) pour '
                + option.textContent.trim() + ' ?';
            if (!window.confirm(question)) { evenement.preventDefault(); }
        });
    }
})();
