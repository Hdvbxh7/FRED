**F.R.E.D** (**F**ramework de **R**eview d'**E**xercices en **D**irect) est un outil d'évaluation automatique de projets étudiants en Java. Il permet de tester et noter automatiquement le code soumis par les étudiants en exécutant différents types de vérifications.

## Fonctionnement global

FRED récupère les projets étudiants (depuis Git, un dossier local, etc.), applique une série de tests configurables, puis génère des rapports au format [TAP](https://testanything.org/) (Test Anything Protocol).

Le processus se déroule en trois étapes :

1. **Exploration** : Un [[Explorateur.java|explorateur]] découvre les projets à évaluer (depuis des dépôts Git, un répertoire local, ou selon un scénario prédéfini)
2. **Évaluation** : Des [[Evaluateur.java|évaluateurs]] exécutent différents types de tests sur chaque projet selon un [[Scénario.java|scénario]] configuré par le professeur
3. **Agrégation** : L'[[Aggregateur.java|agrégateur]] compile tous les résultats dans un rapport final

![[Architecture.excalidraw]]

## Types d'évaluations disponibles

FRED propose les catégories d'évaluations suivantes, chacune avec des évaluateurs spécifiques :

- **Compilation** : Vérifie que le code compile sans erreurs ni avertissements
- **Style de code** : Analyse le respect des conventions de codage avec [Checkstyle](https://checkstyle.org/)
- **Tests unitaires** : Exécute les tests unitaires dans les fichiers élèves, comme avec [JUnit](https://junit.org/) par exemple 
- **Tests boîte noire** : Compare les sorties du programme avec des résultats attendus
- **Gestion mémoire** : Détecte les fuites mémoire avec [Valgrind](https://valgrind.org/)

Pour voir plus en détails les langages supportés par le projet, consultez la catégorie [[/Langages]]

## Configuration

L'enseignant définit le scénario d'évaluation dans la classe [[Scenario.java|Scenario]], en choisissant :
- Quel type d'explorateur utiliser
- Quels évaluateurs exécuter et dans quel ordre
- Les paramètres spécifiques à chaque évaluation

Le point d'entrée principal est la classe [[Superviseur.java|Superviseur]], qui orchestre l'exécution parallèle des évaluations sur tous les projets découverts.

## Format de sortie

Les résultats sont produits au format TAP, un format standardisé et lisible par machine qui facilite l'intégration avec d'autres outils de [continuous integration](https://fr.wikipedia.org/wiki/Int%C3%A9gration_continue).