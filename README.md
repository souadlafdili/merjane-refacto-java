### Consignes: 
* Ignorez les migrations BDD
* Ne pas modifier les classes qui ont un commentaire: `// WARN: Should not be changed during the exercise
`
* Pour lancer les tests (depuis le sous-répertoire `api`) :
  * unitaires: `mvnw test`
  * integration: `mvnw integration-test`
  * tous: `mvnw verify`


  ## Cahier de charges


-Tous les produits ont une valeur available qui désigne le nombre d'unités disponibles en stock.

-Tous les produits ont une valeur leadTime qui désigne le nombre de jours nécessaires pour le réapprovisionnement.

-À la fin de chaque commande, notre système décrémente la valeur available pour chaque produit commandé.

Jusqu'ici, tout va bien. Mais voici où ça se corse :

-Les produits "NORMAL" ne présentent aucune particularité. Lorsqu'ils sont en rupture de stock, un délai est simplement annoncé aux clients.

-Les produits "SEASONAL" ne sont disponibles qu'à certaines périodes de l'année. Lorsqu'ils sont en rupture de stock, un délai est annoncé aux clients, mais si ce délai dépasse la saison de disponibilité, le produit est considéré comme non disponible. Quand le produit est considéré comme non disponible, les client sont notifiés de cette indisponibilité.

-Les produits "EXPIRABLE" ont une date d'expiration. Ils peuvent être vendus normalement tant qu'ils n'ont pas expiré, mais ne sont plus disponibles une fois la date d'expiration passée