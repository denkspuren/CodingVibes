## Problemstellung: Divergierende Git-Historien

**Ausgangslage:** Du arbeitest mit einem Git-Repository. Du hast eine Datei direkt über die GitHub-Webseite hinzugefügt und committed. Währenddessen hast du lokal, ohne vorherige Synchronisierung (`git pull`), eine andere neue Datei hinzugefügt und ebenfalls committed.

**Das Problem:** Dein lokaler Git-Branch (`main`) und der Remote-Branch (`origin/main`) haben sich "auseinanderentwickelt" (diverged). Beide Branches enthielten nun jeweils einen Commit, der im anderen nicht vorhanden war. Beim Versuch eines einfachen `git pull` meldete Git einen Fehler (`fatal: Need to specify how to reconcile divergent branches.`), da unklar war, wie die unterschiedlichen Historien zusammengeführt werden sollten. Dein `git status` zeigte an: "Your branch and 'origin/main' have diverged, and have 1 and 1 different commits each, respectively."

## Lösung: Merge divergierender Branches

Die Lösung bestand darin, die divergierenden Historien beider Branches mittels eines **Merge-Vorgangs** zusammenzuführen.

**Schritt 1: Änderungen vom Remote holen und mergen anstoßen**

Um die neuen Commits von GitHub zu integrieren und gleichzeitig deine lokalen Commits zu erhalten, wurde der Befehl `git pull origin main --no-rebase` verwendet.

* `git pull`: Holt die Änderungen vom Remote-Repository und versucht, sie in den aktuellen lokalen Branch zu integrieren.
* `origin main`: Spezifiziert, dass die Änderungen vom Remote "origin" und dessen "main"-Branch geholt werden sollen.
* `--no-rebase`: Weist Git explizit an, die Integration durch einen **Merge** zu versuchen und nicht durch ein Rebase. Ein Merge erstellt einen neuen Commit (den sogenannten Merge-Commit), der die Historien beider Branches zusammenführt, ohne die bestehende Historie umzuschreiben. Dies ist der empfohlene Standardweg bei divergierenden Branches.

**Schritt 2: Merge-Commit erstellen**

Nach Ausführung des `git pull`-Befehls forderte Git eine Commit-Nachricht an. Dies bestätigte, dass ein **neuer Merge-Commit** erstellt wurde. Dieser Commit vereinte die Änderungen von GitHub (die dort hinzugefügte Datei) mit deinen lokalen Änderungen (deine lokal hinzugefügte Datei). Da es sich um zwei *neue, unterschiedliche* Dateien handelte, kam es zu **keinen manuellen Merge-Konflikten**, da Git die Dateien problemlos nebeneinander platzieren konnte.

**Schritt 3: Änderungen auf den Remote pushen**

Nachdem der Merge-Vorgang erfolgreich war und der Merge-Commit lokal erstellt wurde, konntest du deine gesamten Änderungen (den neuen Merge-Commit und deinen vorherigen lokalen Commit) auf das Remote-Repository hochladen:

`git push origin main`

**Ergebnis:** Dein lokales und das Remote-Repository sind nun wieder vollständig synchronisiert. Alle gewünschten Dateien sind sowohl lokal als auch auf GitHub vorhanden, und die gesamte Projekthistorie bleibt intakt.