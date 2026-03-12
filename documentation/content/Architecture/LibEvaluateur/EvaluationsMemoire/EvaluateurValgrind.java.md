Cette classe hérite d'[[EvaluateurMemoire.java|EvaluateurMemoire]] et utilise l'outil [Valgrind](https://valgrind.org/) pour détecter les erreurs de gestion mémoire dans les programmes binaires. Elle exécute Valgrind sur un fichier exécutable, capture la sortie d'erreur, identifie les fuites mémoire et les erreurs d'accès invalides, puis convertit les résultats au format TAP.

{{javadoc:LibEvaluateur.EvaluationsMemoire.EvaluateurValgrind}}
