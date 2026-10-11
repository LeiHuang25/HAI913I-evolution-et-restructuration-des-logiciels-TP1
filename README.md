# HAI913I — TP1 Analyse statique

Ce projet réalise une analyse statique d'un projet Java à l'aide d'Eclipse JDT.

L'outil permet d'extraire :

- les classes, interfaces, énumérations et types imbriqués ;
- les attributs et les méthodes ;
- les relations d'héritage et d'implémentation ;
- les appels de méthodes et le type statique des receveurs ;
- les métriques demandées dans le TP ;
- le graphe d'appel interne du projet.

## 1. Prérequis

L'environnement suivant est nécessaire :

- JDK 17 ou supérieur ;
- Maven 3.9 ou supérieur ;
- Eclipse JDT Core : 3.46.0
- Spoon : 11.5.0
- JUnit Jupiter : 5.14.4
- Système : Windows
- Git, uniquement pour cloner le dépôt.

Pour vérifier l'installation :

    java -version
    mvn -version

## 2. Organisation du dépôt

    TP1/
    ├── starter/
    │   ├── pom.xml
    │   └── src/
    └── resources/
        ├── validation/
        └── robustness/
            ├── missing-dep/
            ├── no-java-files/
            └── syntax-error/

Le projet de l'analyseur se trouve dans le répertoire starter.

Les projets présents dans resources servent à valider le comportement de
l'analyseur.

## 3. Construction

Depuis la racine du dépôt :

    cd starter
    mvn clean package

La commande produit le JAR exécutable :

    target/hai913i-tp1-analyzer.jar

Le fichier original-hai913i-tp1-analyzer.jar ne contient pas les dépendances
et ne doit pas être utilisé directement.

## 4. Utilisation

La syntaxe générale est :

    java -jar target/hai913i-tp1-analyzer.jar CHEMIN_DU_PROJET MODE X

Les arguments sont :

- CHEMIN_DU_PROJET : chemin du projet Java à analyser ;
- MODE : type de résultat à afficher ;
- X : seuil entier utilisé par la question Q11 des métriques.

Les chemins contenant des espaces doivent être placés entre guillemets.

### Modes disponibles

| Mode | Résultat |
|---|---|
| structure | Types, attributs, méthodes et héritage |  :  java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" structure 5
| calls | Appels, receveurs et cibles | : java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" calls 5
| metrics | Métriques Q1 à Q13 |  : java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" metrics 5
| graph | Graphe d'appel interne | : java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" graph 5
| all | Toutes les sorties |

## 5. Exemple complet

Depuis le répertoire starter, sous Windows PowerShell :

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" all 5

Sous Linux ou macOS :

    java -jar target/hai913i-tp1-analyzer.jar "../resources/validation" all 5

Pour n'afficher que les métriques avec le seuil X = 5 :

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" metrics 5

## 6. Résultats attendus

Les principaux points de contrôle obtenus sur le projet de validation sont :

    Unités de compilation  : 14
    Erreurs de compilation : 0

    Types                   : 15
    Classes                 : 12
    Interfaces              : 2
    Énumérations            : 1
    Attributs               : 21
    Méthodes                : 59
    Constructeurs           : 9

    Nœuds du graphe         : 59
    Arcs du graphe          : 44
    Sites d'appel internes  : 46
    Appels externes         : 48
    Appels non résolus      : 0

Les libellés exacts peuvent légèrement varier, mais les valeurs doivent être
identiques.

## 7. Validation des cas robustes

### Projet sans fichier Java

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\robustness\no-java-files" all 5

L'outil affiche un message explicite et retourne un code de sortie non nul.

### Projet contenant une erreur de syntaxe

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\robustness\syntax-error" all 5

Les erreurs de Broken.java sont signalées, mais Helper.java reste analysé.
L'outil ne s'arrête pas à la première erreur.

### Projet avec une dépendance manquante

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\robustness\missing-dep" all 5

    $LASTEXITCODE

Les bindings non résolus sont signalés sans interrompre l'analyse.

### Seuil incorrect

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\validation" metrics abc


L'outil refuse la valeur et affiche un message d'utilisation explicite.

### Projet inexistant

    java -jar target\hai913i-tp1-analyzer.jar "..\resources\inexistant" all 5

L'outil affiche une erreur claire et retourne un code de sortie non nul.

Sous PowerShell, le dernier code de sortie peut être consulté avec :

    $LASTEXITCODE

Une analyse normale doit retourner 0. Une analyse impossible doit retourner
un code non nul.

## Analyse avec Spoon — C1

Pour exécuter la comparaison JDT/Spoon :

    mvn "-Dtest=SpoonParserTest" test

L'extraction Spoon utilise la version 11.5.0 et le mode `noClasspath`
pour le projet contenant une dépendance manquante.

## Export du graphe — C2

Le graphe d'appel peut être exporté au format DOT, puis converti avec Graphviz :

    dot -Tpng output/callgraph-service.dot -o output/callgraph-service.png

La vue filtrée sur `library.service` permet d'obtenir une image plus lisible.
Le répertoire `output` contient uniquement des fichiers engendrés et n'est
pas versionné.


## 8. Construction depuis une copie fraîche

Pour vérifier la reproductibilité :

    git clone https://github.com/LeiHuang25/HAI913I-evolution-et-restructuration-des-logiciels-TP1.git
    cd HAI913I-evolution-et-restructuration-des-logiciels-TP1/starter
    mvn clean package
    java -jar target/hai913i-tp1-analyzer.jar "../resources/validation" all 5

Ces commandes doivent suffire sans configuration manuelle dans Eclipse.

## 9. Nettoyage

Pour supprimer les fichiers produits par Maven :

    mvn clean

Les répertoires target, les fichiers .class et les JAR générés ne sont pas
versionnés.

## 10. Limites connues

- L'analyse dépend des bindings que JDT parvient à résoudre.
- Les appels utilisant la réflexion ne peuvent pas être déterminés
  statiquement de manière générale.
- Le graphe utilise les cibles statiques résolues par JDT et ne représente pas
  toutes les cibles dynamiques possibles.
- Une dépendance absente peut produire un appel non résolu, mais l'analyse des
  autres éléments continue.
