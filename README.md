# Que ?

Juste quelques tests pour essayer de piger comment fonctionne [ANTLR](https://github.com/antlr/antlr4/), un truc qui a l'air sympa, mais dont la documentation est encore moins explicite que celle de mes projets personnels (et c'est pas peu dire).

## Un projet ANTLR avec MAVEN ?

Pour pouvoir utiliser ANTLR, le `pom.xml` doit au moins ressembler à ça:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>test-compiler-antlr</groupId>
    <artifactId>test-compiler-antlr</artifactId>
    <version>0.1</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <antlr4.visitor>true</antlr4.visitor> <!--- si besoint d'un visitor -->
        <antlr4.listener>true</antlr4.listener> <!-- si besoin d'un listener -->
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-runtime</artifactId>
            <version>4.7.1</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.antlr</groupId>
                <artifactId>antlr4-maven-plugin</artifactId>
                <version>4.7.1</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>antlr4</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

Quand à la structure du projet (d'après [ici](http://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) pour maven et [ici](https://www.antlr.org/api/maven-plugin/latest/usage.html) pour ANTLR),

```
/src/main/antlr4/{qualified-name}/*.g4   <-- fichier g4 pour la grammaire
/src/main/antlr4/imports/*.g4            <-- fichier g4 partagés entre toutes les grammaires (aucun intérêt)

/src/main/java/{qualified-name}/*.java   <-- fichiers source
/src/main/ressources/*                   <-- ressources

/src/test/java/{qualified-name}/*.java   <-- fichiers test
/src/test/ressources/*                   <-- ressources

pom.xml                                  <-- fichier maven
```

où `{qualified-name}` est le nom du projet (ici `be.unamur.b314.compiler`).

## Grammaire et *tokens*

[Voir ICI](https://github.com/antlr/antlr4/blob/master/doc/index.md) pour un début d'explication (la doc de projet la plus mauvaise de l'univers, parce qu'il manque des bouts) et [là](http://lms.ui.ac.ir/public/group/90/59/01/15738_ce57.pdf) pour le fameux bouquin (qui est pas une documentation spécialement plus efficace, de mon avis).

En résumé, on a deux manières de travailler:

1. Un seul fichier principal (`Truc.g4`) débuté par `grammar Truc;` pour définir de quelle grammaire il s'agit. Il semble important que le nom de fichier et ce qui est défini à cet endroit correspondent.
2. Un seul fichier principal, et un fichier de *tokens* séparé, situé dans `/src/main/antlr4/imports/`, qui doit le débuter par `lexer grammar TrucTokens` (ne **pas** utiliser `Lexer` dans le nom ... Et encore une fois, le nom du fichier doit correspondre). Pour l'importer, on utiliser `import TrucTokens;` dans le fichier principal. Problème, l'extention InteliJ ne le reconnait alors pas ... Donc il faut faire des liens symboliques dans un dossier qui n'a rien à voir. C'est moche.

À part ça,

+ Les règles de grammaire (dixit [là](https://github.com/antlr/antlr4/blob/master/doc/lexicon.md#identifiers)) commencent **obligatoirement** par une minuscule, tandis que les *tokens* commencent obligatoirement par une majuscule (et donc, on met tout en majuscule, en général).
+ On peut *labeliser* une règle de grammaire ([voir ici](https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#alternative-labels)) en précisant un `# nom` derrière. Par exemple,
  ```antlrv4
  grammar X;
  e : e '*' e # mult
    | e '+' e # add
    | INT     # int
    ;
  ```
  
  ... Ce qui aide pour créer l'éventuel *listener*.

+ Pour le *lexer* (voir [ici](https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md)), on peut suivre les définitions de commandes, comme suit:

  ```antlrv4
  lexer grammar X;
  TOK: 'w' -> command;
  ```
  
  La commande la plus utile est `skip`, qui dit de "jeter" le *token* (de l'ignorer). 
  
  On peut également utiliser des types pour rassembler plusieurs *tokens* sous le même nom (défini par `tokens {...}`):
  
  ```antlrv4
  lexer grammar SetType;
  tokens { STRING }
  DOUBLE : '"' .*? '"'   -> type(STRING) ;
  SINGLE : '\'' .*? '\'' -> type(STRING) ;
  WS     : [ \r\t\n]+    -> skip ;
  ```
  On peut également définir des modes (??).
  
+ De même, on peut *labeliser* les *tokens* d'une règle ([voir ici](https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels)). Par exemple, 
  
  ```antlrv4
  grammar X;
  e: left=e '*' right=e;
  ```
  
  Ce qui change le nom de ces noeuds dans l'arbre abstrait (et aide a écrire des règles d'attribution).
 
+ Pour définir des attributs aux règles, on utilise la syntaxe suivante:

  ```antlrv4
  grammar X;
  nomRegle[args] returns [vals] locals [locals]: BIDULE {action};
  ```
  
  où `[...]` sont des définitions de variables, écrites dans le langage cible du compilateur (Java, donc).
  
  `{action}` est un bloc de code (toujours écrit dans le langage cible) qui est ajouté au compilateur et exécuté durant le *parsing*.
  D'après [ici](https://github.com/antlr/antlr4/blob/master/doc/actions.md), dans la partie `{action}`, `$x` remplace le *token* `x=SOMETHING` correspondant (d'où l'intérêt de les labéliser).
  Le défaut de cette approche est qu'on écrit du code spécifique directement dans la grammaire: avec ANTLR4, il est **fortement** suggéré d'utiliser un visiteur où un *listener* pour faire ça ! 
  
  Pour définir un attribut local (donc attribuer la grammaire), c'est le bloc `[locals]` qui doit être utilisé.
  
  On peut pousser un peu plus loin le délire (d'après [ici](https://github.com/antlr/antlr4/blob/master/doc/actions.md#dynamically-scoped-attributes)) en appellant des attribut de règles parentes via la syntaxe `$r::x`, où `r` est un nom de règle et `x` un de ces attributs (défini dans la partie `locals`).
  
+ On peut également utiliser les actions sur les règles du *lexer* ([voir ici](https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md#lexer-rule-actions)). Mais vu qu'il ne permet pas de définir d'attribut à ce niveau, c'est moins intéréssant:

  ```antlrv4
  lexer grammar X;
  END : ('endif'|'end') {System.out.println("found an end");} ;
  ```
  
Ce qui est réellement intéréssant dans tout ça, c'est qu'on peut labeliser les règles et les *tokens* de celle-ci, parce que ça aide à s'y retrouver pour la suite.
Le reste, c'est du détail.
  
## Utiliser ce qui est généré

Les classes correspondantes sont générées via la commande `package` de Maven, et elles le sont dans un dossier `/target`, par défaut.
En particulier, on a le code source des classes générées dans `/target/generated-sources/antlr4` (toujours utile pour la suite).

Pour un langage `XX` (déclaré via `grammar XX;` dans le fichier `XX.g4`), les classes suivantes sont générées:

+ `XXLexer`, la classe représentant le *lexer*,
+ `XXParser`, la classe représentant le *parser*,
+ `XXVisitor` et `XXListener` (générés si on le demande gentiment, voir `pom.xml` ci-dessus), qui sont les **interface** du visiteur/*listener*.
+ `XXBaseVisitor` et `XXBaseListener`, des implémentations des interfaces en questions qui ne font absolument rien (mais dont on peut hériter et faire de l'*override*).

**Note**: La documentation des classes *runtime* est disponible [ici](https://www.antlr.org/api/Java/index.html). C'est parfois utile.

Par exemple, prenons la grammaire [`TestL`](src/main/antlr4/TestL.g4). 
De manière assez logique, le *lexer* est nommé `TestLLexer` et le parser est nommé `TestLParser`
Le code minimal pour l'utiliser est

```java
/* + eventual `package` */

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {

	public static void main(String[] args) {
		String tmp = "point at 4,4";
		Main main = new Main();
		main.parse(tmp);
	}

	Main() { /* nothing */ }

	private void parse(String c) {
		// 1. create token stream from lexer
		CommonTokenStream ts = new CommonTokenStream(new TestLLexer(CharStreams.fromString(c)));

		// 2. create the parser
		TestLParser parser = new TestLParser(ts);

		// 3. parse, by requesting the root node of the grammar
		ParseTree tree = parser.file();
        
		// bonus: print a "LISP-style" parser tree
		System.out.println(tree.toStringTree(parser));
	}
}
```

Le tout, c'est de bien appeller la règle de départ (ici, `file()`).
On notera qu'une fonction (dont les paramètres d'entrée sont définis par `[args]` ci dessus) est générée par règle de la grammaire (d'où `file()`, mais si on regarde les sources, on a également `point()` et `command()`).

## *Visitor*, *Listener*, et l'art de se promener

Un visiteur (*visitor*) et un *listener* permetent tout deux de visiter chaque noeud de l'arbre une fois généré, donc après la phase de *parsing*.
Il agissent donc sur un un `ParseTree`. Et, pour résumer, il s'agit du moyen le plus efficace pour faire une grammaire attribuée.


### *Visitor* (visiteur)

Cette manière de procéder est basée sur le [*visitor pattern*](https://fr.wikipedia.org/wiki/Visiteur_(patron_de_conception)).

Le plus simple: un visiteur doit hériter de la classe `XXXBaseVisitor` (elle même dérivée de [`AbstractParseTreeVisitor`](https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor.html)). 
À noter qu'il s'agit d'une *template*, dont l'argument est `T`, qui est le type de retour des méthodes de celle-ci.

Dans celle-ci, on a des méthodes `visitYY()` (qui renvoit un argument de type `T`, donc) où `YY` est une règle de la grammaire ou un *label* dans celle-ci (on doit labeliser toutes les alternatives d'une règle le cas échéant).
Par exemple, soit la grammaire suivante,

```antlrv4
grammar XX;

ruleX : ruleY T
      | U ruleY
      ;

ruleY : A B  # ruleYA
      | B C  # ruleYB
      ;
```

on aura une fonction `visitRuleX()`, qui visitera génériquement les noeuds de type `ruleX`, mais également des fonctions `visitRuleYA()` et `visitRuleYB()`, explorant spécificement les noeuds labelisé. Par contre, pas de fonction `visitRuleY()` (puisqu'on a déjà les fonctions pour visiter les noeuds labelisés).

Chacune de ces fonctions `visitYY()` prend pour paramètre un contexte, `ctx`, qui hérite de [`ParserRuleContext`](https://www.antlr.org/api/Java/org/antlr/v4/runtime/ParserRuleContext.html) qui reprend, en gros, le noeud parent et les noeuds enfants de cette règle.
Si on labélise les *tokens* d'une règle, ces labels sont également disponibles sous forme d'attributs publics de ce contexte. On y retrouve également les attributs définis dans le bloc `locals [locals]` d'une règle.
Par exemple, pour la grammaire suivante,

```antlrv4
grammar X;
rule: X in=dest out=dest;
dest locals [int usage=0]: A | B;
```

on aura la fonction `visitRule(XParser.RuleContext ctx)`, avec `ctx.in` et `ctx.out` qui sont défini dans la classe `XParser.RuleContext` (qui hérite bien de `ParserRuleContext`). 
On aura également `ctx.dest()`, qui renvoit la liste des deux `XParser.DestContext`.
Quand au contexte `XParser.DestContext`, on y retrouvera `ctx.usage`, initialisé à 0.

Lorsqu'on implémente ces fonctions `visitYY()`, il faut ABSOLUMENT visiter les enfants.
Pour ce faire, 

+ Soit on appelle la fonction `visit()` générique sur chacun de ceux-ci (on peut également utiliser les fonctions spécifiques `visitYY()` sur ceux-ci, ça revient au même).
+ On peut visiter tout les enfants en utilisant la fonction `visitChildren()`, prenant en paramètre le contexte.

L'intérêt d'un *visitor* est qu'on peut choisir (grâce à la première manière de faire) de ne PAS visiter certains enfants. 

On peut voir un exemple (idiot) de visiteur dans [`CountPointsVisitor`](src/main/java/CountPointsVisitor.java).
Et pour l'utiliser, deux manières de faire:
```java
CountPointsVisitor cv = new CountPointsVisitor();

// option 1
Integer n = cv.visit(tree);

// option 2
Integer n = tree.accept(cv);
```

Vu qu'on a le contrôle sur la visite, il est légèrement plus simple d'utiliser un *visitor* pour implémenter la partie "traduction dans le langage cible".

### *Listener* ("écouteur")

Pour un début d'explications: [voir ici](https://github.com/antlr/antlr4/blob/master/doc/listeners.md).

Un *listener* fonctionne sur le même principe, mais cette fois, tout les noeuds sont visités, car il ne faut pas explicitement demander à ce qu'ils le soient.
En effet avec cette manière de travailler, un *walker* visite l'arbre: on est juste informé de quand ce *walker* "entre" dans un noeud de type YY (car il appelle alors `enterYY()`) et quand il en sort (car il appelle alors `exitYY()`).
Bien entendu, ces méthodes prennent en paramètre le contexte, `ctx`, qui est le même que pour les visiteurs.

Le principe est donc le même, mais force explictement à utiliser des attributs de classe (vu que les méthodes ne peuvent pas renvoyer quelque chose).
C'est intéréssant aussi :)

Pour le fun, on réimplémente exactement la même chose avec un *listener* dans [`CountPointsListener`](src/main/java/CountPointsVisitor.java).
Et pour l'utiliser, on défini d'abors un *walker*, qu'on utilise avec le *listener* sur l'arbre:
```java
ParseTreeWalker walker = new ParseTreeWalker();
CountPointsListener li = new CountPointsListener();
walker.walk(li, tree);
System.out.println(li.n); // on est donc obligé d'utiliser un attribut pour avoir le résultat.
```

Le résultat est absolument le même qu'avec le *visitor*, mais c'est légèrement plus court à implémenter, ce qui en fait un bon choix pour tout ce qui est analyse sémantique.

## Se plaindre !

Au niveau du *parser*, les fonctions lancent une [`RecognitionException`](https://www.antlr.org/api/Java/org/antlr/v4/runtime/RecognitionException.html) (qui hérite forcément de `RuntimeExpression`). 
On peut y retrouver, entre autres, le *token* attendu et le contexte.

Avec un *visitor* ou un *listener*, ce n'est pas possible (il n'y a pas de `throw` dans le code généré). Deux "solutions" à ça:

1. Lancer des `RuntimeException`, parce qu'on peut toujours faire ça. C'est assez efficace pour stopper un *walker* associé à un *listener* en pleine marche.
2. Construire une classe spécialement dédiée à la récupération des erreurs, et utiliser cet objet pour laisser les erreurs s'accumuler lors du passage du *lsitener*/*visitor* sur l'arbre. 
   C'est utile pour avoir **toutes** les erreurs, puisqu'une exception permettrait de récupérer juste la première.
   Notez que c'est également la stratégie utilisée par le *lexer*/*parser*.
   
## Bonus: construire un AST ?

Évidement, ANTLR, sur base de la grammaire, construit un CST (*concrete syntax tree*).
Il peut être intéréssant de se débarasser d'une partie, inutile, de l'information et de construire un AST (*abstract syntax tree*), puis seulement de faire la validation sémantique dessus.

Le problème, c'est qu'on ne peut pas (plus, en fait) faire des transformations sur le CST généré par ANTLR, il faut donc écrire un visiteur qui génère un arbre *custom*, pour lequel il faudra également écrire un visiteur (et/ou un *listener*) *custom*. Ça perd donc un peu son intérêt.
En plus, on ne peut pas simplement demander à ANTLR de générer les classes pour une "sous-grammaire": on ne peut pas créer simplement de `RuleContext`, car les constructeurs contiennent des informations qu'on ne peut pas avoir si on génère un arbre *from scratch*.

Donc, **c'est pas forcément intéréssant**.


-------

[*To be continued*](https://www.youtube.com/watch?v=I2PmwSgkHUI) ?
