# Intégration Javadoc dans la Documentation Quartz

Ce document explique comment le plugin Javadoc personnalisé fonctionne dans FRED et comment l'utiliser.

## Vue d'ensemble

Le système d'intégration Javadoc permet d'inclure automatiquement la documentation Javadoc générée directement dans les pages markdown de la documentation Quartz. Cela se fait via une syntaxe simple : `{{javadoc:ClassName}}`.

## Architecture

### Composants

1. **build.sh** - Script de build qui :
   - Génère la documentation Javadoc à partir des sources Java
   - Place les fichiers HTML dans `static/javadoc/`
   - Build ensuite la documentation Quartz

2. **javadoc.ts** - Plugin Quartz Transformer qui :
   - Détecte la syntaxe `{{javadoc:...}}` dans les fichiers markdown
   - Lit les fichiers HTML Javadoc correspondants
   - Extrait le contenu principal
   - Injecte le HTML dans la page markdown

3. **javadoc.scss** - Styles CSS pour :
   - Formater correctement le HTML Javadoc injecté
   - Adapter le style au thème Quartz
   - Support du mode sombre

4. **quartz.config.ts** - Configuration qui :
   - Enregistre le plugin Javadoc dans la chaîne de transformation
   - Configure les options du plugin

## Utilisation

### Syntaxe de base

Dans n'importe quel fichier markdown de `content/` :

```markdown
{{javadoc:ClassName}}
```

### Exemples

#### Classe sans package
```markdown
{{javadoc:Superviseur}}
```

#### Classe avec package
```markdown
{{javadoc:configuration.Scenario}}
{{javadoc:LibEvaluateur.Evaluateur}}
{{javadoc:LibExplorateurs.ExplorateurGit}}
```

## Workflow de Build

### Local

```bash
cd documentation
./build.sh
```

Le script :
1. Vérifie les dépendances (npm, npx, javadoc)
2. Génère le Javadoc dans `static/javadoc/`
3. Build Quartz avec le plugin actif
4. Output dans `public/`

### GitHub Actions

Le workflow `.github/workflows/deploy.yml` :
1. Clone le repo
2. Setup Node.js et Java
3. Installe les dépendances npm
4. Exécute `build.sh`
5. Upload et déploie sur GitHub Pages

## Configuration

### Options du plugin (quartz.config.ts)

```typescript
Plugin.Javadoc({
  javadocDir: "static/javadoc",    // Où chercher les fichiers Javadoc
  inlineHtml: true,                 // true = intégration inline, false = liens
  javadocBaseUrl: "/static/javadoc", // URL de base pour les liens
}),
```

### Configuration Javadoc (build.sh)

Les options importantes :
- `-d documentation/static/javadoc` : Répertoire de sortie
- `-subpackages configuration:LibEvaluateur:LibExplorateurs` : Packages à documenter
- `-private` : Inclut les membres privés dans la doc
- `-quiet` : Réduit la verbosité

## Gestion des erreurs

### Javadoc non trouvé

Si vous voyez : `⚠️ Documentation Javadoc non trouvée pour <code>ClassName</code>`

**Solutions :**
1. Vérifiez l'orthographe du nom de classe (sensible à la casse)
2. Vérifiez que le package est correct
3. Rebuild la documentation : `./build.sh`
4. Vérifiez que le fichier existe : `ls static/javadoc/path/to/Class.html`

### Erreurs de génération Javadoc

Si la génération Javadoc échoue :
1. Vérifiez que Java JDK est installé : `javadoc -version`
2. Vérifiez les erreurs de syntaxe dans les commentaires Javadoc
3. Vérifiez les dépendances dans `libs/`

### Erreurs TypeScript

Si le plugin TypeScript ne compile pas :
1. Vérifiez la syntaxe dans `javadoc.ts`
2. Rebuild : `npx quartz build`
3. Vérifiez les logs de build

## Personnalisation

### Modifier le style

Éditez `quartz/styles/javadoc.scss` pour :
- Changer les couleurs
- Modifier les espacements
- Adapter au thème custom

### Modifier l'extraction de contenu

Éditez la fonction `extractJavadocContent()` dans `javadoc.ts` pour :
- Changer ce qui est extrait du HTML Javadoc
- Filtrer certains éléments
- Ajouter des transformations

### Changer le mode d'affichage

Dans `quartz.config.ts`, changez `inlineHtml: false` pour créer des liens au lieu d'intégrer le HTML.

## Bonnes pratiques

1. **Documentation Javadoc** : Écrivez des commentaires Javadoc complets dans votre code Java
2. **Rebuild régulier** : Relancez `build.sh` après avoir modifié le code Java
3. **Test local** : Testez l'intégration localement avant de commit
4. **Liens relatifs** : Utilisez des chemins relatifs dans vos liens Javadoc

## Dépannage

### Le plugin ne fonctionne pas

```bash
# Vérifier que le plugin est enregistré
grep "Javadoc" quartz.config.ts

# Vérifier que le plugin est exporté
grep "Javadoc" quartz/plugins/transformers/index.ts

# Rebuild complet
rm -rf .quartz-cache node_modules
npm install
./build.sh
```

### Performance

Si le build est lent :
- Réduisez la taille des Javadoc générés avec des options javadoc
- Utilisez `inlineHtml: false` pour juste créer des liens
- Excluez certains packages de la génération Javadoc

## Ressources

- [Documentation Quartz](https://quartz.jzhao.xyz/)
- [Documentation Javadoc Oracle](https://docs.oracle.com/en/java/javase/17/javadoc/)
- [Guide du plugin](content/Comment%20utiliser%20FRED%20?/Javadoc.md)
