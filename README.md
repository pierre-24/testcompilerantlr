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
        <antlr4.visitor>true</antlr4.visitor> <!--- si visitors -->
        <antlr4.listener>true</antlr4.listener> <!-- si listener -->
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

[Voir ICI](https://github.com/antlr/antlr4/blob/master/doc/index.md)

En résumé, on a deux manières de travailler:

1. Un seul fichier principal (`Truc.g4`) débuté par `grammar Truc;` pour définir de quelle grammaire il s'agit. Il semble important que le nom de fichier et ce qui est défini à cet endroit correspondent.
2. Un seul fichier principal, et un fichier de *tokens* séparé, situé dans `/src/main/antlr4/imports/`, qui doit le débuter par `lexer grammar TrucTokens` (ne **pas** utiliser `Lexer` dans le nom ... Et encore une fois, le nom du fichier doit correspondre). Pour l'importer, on utiliser `import TrucTokens;` dans le fichier principal. Problème, l'extention InteliJ ne le reconnait alors pas ... Donc il faut faire des liens symboliques dans un dossier qui n'a rien à voir. C'est moche.

À part ça,

+ Les règles de grammaire commencent **obligatoirement** par une minuscule.
+ On peut *labeliser* une règle de grammaire ([voir ici](https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#alternative-labels)) en précisant un `# nom` derrière. Par exemple,
  ```antlrv4
  grammar X;
  e: e '*' e # mult
   | e '+' e # add
   | INT     # int
   ;
  ```
  
  ... Ce qui aide pour créer l'éventuel *listener*.
  
+ De même, on peut *labeliser* les éléments d'une règle ([voir ici](https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels)). Par exemple, 
  
  ```antlrv4
  grammar X;
  e: left=e '*' right=e;
  ```
  
  Ce qui change le nom dans l'arbre abstrait.
  
[*To be continued*](https://www.youtube.com/watch?v=I2PmwSgkHUI).
