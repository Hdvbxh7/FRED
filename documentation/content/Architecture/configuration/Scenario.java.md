Cette classe configure et orchestre l'exécution des scénarios de tests sur les dossiers étudiants. Elle définit les paramètres globaux comme la date limite d'évaluation, le type d'[[Explorateur.java|explorateur]] à utiliser (Git, Simple ou Scénario), et le chemin du projet à tester. Chaque instance représente un scénario de test à exécuter sur un dossier spécifique, implémentant l'interface Runnable pour permettre une exécution multi-thread.

L'objectif est que la fonction `scenario` de la classe soit modifiée par le professeur afin de produire le scénario dé

siré.

{{javadoc:configuration.Scenario}}
