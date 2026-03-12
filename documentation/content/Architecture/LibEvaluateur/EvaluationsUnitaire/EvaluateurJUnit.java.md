Cette classe hérite d'[[EvaluateurUnitaire.java|EvaluateurUnitaire]] et exécute des tests JUnit dans des classes de test compilées. Elle charge dynamiquement les classes de test, les exécute avec JUnitCore, collecte les résultats (réussis, échoués, avec détails des erreurs), et les convertit au format TAP pour un rapport standardisé.

Son objectif est de pouvoir laisser aux élèves la tâche de créer les tests, et de simplement exécuter ces derniers.

{{javadoc:LibEvaluateur.EvaluationsUnitaire.EvaluateurJUnit}}
