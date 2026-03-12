---
title: Utiliser Javadoc dans la Documentation
---

# Comment intégrer Javadoc dans vos pages

FRED utilise un plugin Quartz personnalisé qui permet d'intégrer automatiquement la documentation Javadoc directement dans les pages markdown.

## Syntaxe de base

Pour intégrer la documentation Javadoc d'une classe Java, utilisez la syntaxe suivante :

```markdown
{{javadoc:NomDeLaClasse}}
```

### Exemples

#### Classe simple (sans package)

```markdown
{{javadoc:Superviseur}}
```

Cela va intégrer la documentation Javadoc complète de la classe `Superviseur` directement dans votre page.

#### Classe avec package

```markdown
{{javadoc:configuration.Scenario}}
```

Cela va intégrer la documentation de la classe `Scenario` du package `configuration`.

#### Exemples concrets

- Pour le Superviseur principal :
  
  ```markdown
  {{javadoc:Superviseur}}
  ```

- Pour la classe Scenario de configuration :
  
  ```markdown
  {{javadoc:configuration.Scenario}}
  ```

- Pour un évaluateur :
  
  ```markdown
  {{javadoc:LibEvaluateur.Evaluateur}}
  ```

- Pour un explorateur :
  
  ```markdown
  {{javadoc:LibExplorateurs.Explorateur}}
  ```

## Comment ça fonctionne

1. **Génération automatique** : Lors du build de la documentation (via `build.sh`), le script génère automatiquement la documentation Javadoc de tous les fichiers Java du projet dans le dossier `static/javadoc/`.

2. **Transformation markdown** : Le plugin Quartz détecte la syntaxe `{{javadoc:...}}` dans vos fichiers markdown.

3. **Intégration HTML** : Le plugin remplace cette syntaxe par le contenu HTML de la documentation Javadoc correspondante.

4. **Affichage** : Le résultat est une page Quartz qui contient directement la documentation Javadoc inline, avec le même style que le reste de votre documentation.

## Configuration

Le plugin est configuré dans `quartz.config.ts` avec les options suivantes :

- `javadocDir`: Répertoire où se trouvent les fichiers Javadoc HTML (`static/javadoc`)
- `inlineHtml`: Si `true`, intègre le HTML directement ; si `false`, crée juste un lien
- `javadocBaseUrl`: URL de base pour les liens Javadoc (`/static/javadoc`)

## Résolution des problèmes

### La documentation Javadoc n'apparaît pas

1. Assurez-vous que vous avez exécuté `./build.sh` qui génère le Javadoc
2. Vérifiez que le nom de la classe est correct (sensible à la casse)
3. Vérifiez que le package est correctement spécifié si la classe est dans un package

### Le message "Documentation Javadoc non trouvée" apparaît

Cela signifie que le fichier HTML Javadoc n'a pas été trouvé. Vérifiez :

- Que la génération Javadoc n'a pas échoué lors du build
- Que le nom de classe correspond exactement au nom du fichier Java
- Que vous avez bien rebuild la documentation après avoir ajouté de nouvelles classes

## Exemple complet

Voici un exemple de page de documentation pour une classe :

```markdown
---
title: Superviseur
---

# Superviseur - Point d'entrée principal

Cette classe est le point d'entrée principal de FRED. Elle récupère la liste 
des dossiers à tester via l'explorateur configuré dans Scenario, puis lance 
l'évaluation de chaque dossier en parallèle.

## Utilisation

Le Superviseur utilise un pool de threads pour paralléliser les tests...

## Documentation Javadoc complète

{{javadoc:Superviseur}}
```

Ce code produira une page avec votre description personnalisée suivie de toute la documentation Javadoc de la classe Superviseur.
