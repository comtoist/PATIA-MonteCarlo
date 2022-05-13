# PATIA-MonteCarlo

## Pré-requis : 
mvn install:install-file \
   -Dfile=<path_to_pddl4j_jar>libs/pddl4j-4.0.0.jar \
   -DgroupId=fr.uga.pddl4j \
   -DartifactId=pddl4j \
   -Dversion=4.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true

## Compilation tests :
javac -d classes -cp lib/pddl4j-4.0.0.jar src/fr/uga/pddl4j/examples/asp/*.java

## Exécution tests : 

java -cp classes:lib/pddl4j-4.0.0.jar fr.uga.pddl4j.examples.asp.ASP nom_domaine.pddl nom_probleme.pddl -e FAST_FORWARD -w 1.2 -t 300
Ou
python3 exec.py [nom_du_dossier_test_dans_le_dossier_pddl]

## Compilation Sokoban : 
mvn compile

## Execution Sokoban : 
java --add-opens java.base/java.lang=ALL-UNNAMED       -server -Xms2048m -Xmx2048m       -cp "$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/test-classes/:target/classes"       sokoban.SokobanMain
