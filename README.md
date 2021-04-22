# JavaParser_Project

## Building

Comando per lanciare il programma: 
    - mvn clean compile install && cd target && java -jar JavaParser_Project-1.0-SNAPSHOT-jar-with-dependencies.jar && cd ..

## Lista obiettivi
    1) Gestione file (lettura, scrittura) [Fatto]
    2) Analisi metodi della classe utente [Fatto]
    3) Matching tra algoritmo utente e algoritmo applicativo
    4) Sostituzione metodi ricorsivi utente con corrispondenti versioni iterative [Fatto]
    
    NOTA 1:(riflettere sulla possibilità di inalterare il file utente iniziale e creare una copia con le dovute modifiche)

## NOTE STRUTTURA PROGETTO
	- Il codice della tesi che sta sotto src/main/java.
		Si occupa di analizzare file utenti/applicativo ed eventualmente di apportare modifiche al file utente/crea un nuovo file per l'utente finale
		
		
NOTA IMPORTANTE: Invertire i nomi delle variabili dove "user" o "recursive" non compaiono all'inizio!		