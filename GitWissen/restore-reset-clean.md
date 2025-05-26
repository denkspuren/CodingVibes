# `restore [--staged] <Datei>`,<br> `reset --hard HEAD`,<br> `git clean -n|-f[d]`

Du hast ein paar Änderungen vorgenommen, die du nicht behalten möchtest, und möchtest dein Repository in den Zustand des letzten Commits zurücksetzen. Das ist ein häufiger Anwendungsfall in Git!

Hier sind die gängigsten und sichersten Methoden, je nachdem, ob du die Änderungen bereits zur Staging Area hinzugefügt hast (mit `git add`) oder nicht:

**Wichtiger Hinweis vorab:** Sei vorsichtig mit diesen Befehlen, insbesondere mit `git reset --hard`. Einmal verworfene Änderungen sind **in der Regel unwiederbringlich verloren**, wenn sie noch nicht committed wurden.

---

### Fall 1: Änderungen wurden noch NICHT zur Staging Area hinzugefügt (noch nicht `git add` ausgeführt)

Wenn du die Dateien nur geändert hast und sie noch als "Changes not staged for commit" in `git status` angezeigt werden, kannst du sie wie folgt verwerfen:

* **Für eine bestimmte Datei:**
    ```bash
    git restore <dateiname>
    ```
    oder (in älteren Git-Versionen):
    ```bash
    git checkout -- <dateiname>
    ```
    Ersetze `<dateiname>` durch den tatsächlichen Namen der Datei (z.B. `meine_datei.py`).

* **Für alle geänderten Dateien im aktuellen Verzeichnis und Unterverzeichnissen:**
    ```bash
    git restore .
    ```
    oder (in älteren Git-Versionen):
    ```bash
    git checkout -- .
    ```
    Der Punkt `.` bezieht sich auf das aktuelle Verzeichnis.

### Fall 2: Änderungen wurden bereits zur Staging Area hinzugefügt (mit `git add`)

Wenn du `git add` ausgeführt hast und die Dateien jetzt unter "Changes to be committed" in `git status` stehen, musst du sie zuerst aus der Staging Area entfernen (unstagen) und dann die Änderungen verwerfen.

1.  **Änderungen aus der Staging Area entfernen (unstagen):**
    * **Für eine bestimmte Datei:**
        ```bash
        git restore --staged <dateiname>
        ```
        oder (in älteren Git-Versionen):
        ```bash
        git reset HEAD <dateiname>
        ```
    * **Für alle Dateien in der Staging Area:**
        ```bash
        git restore --staged .
        ```
        oder (in älteren Git-Versionen):
        ```bash
        git reset HEAD .
        ```
    Nach diesem Schritt sind die Änderungen wieder im Zustand von "Changes not staged for commit".

2.  **Änderungen verwerfen (wie in Fall 1):**
    Nachdem du sie ungestaged hast, kannst du die Änderungen wie oben beschrieben (Fall 1) verwerfen:
    * **Für eine bestimmte Datei:** `git restore <dateiname>` oder `git checkout -- <dateiname>`
    * **Für alle Dateien:** `git restore .` oder `git checkout -- .`

### Fall 3: Alle nicht-committeten Änderungen komplett verwerfen (sowohl staged als auch unstaged)

Wenn du alle Änderungen, die seit dem letzten Commit vorgenommen wurden (egal ob gestaged oder ungestaged), komplett verwerfen und zum Zustand des letzten Commits zurückkehren möchtest, ist dies der schnellste Weg:

```bash
git reset --hard HEAD
```
**ACHTUNG:** Dieser Befehl ist sehr mächtig und **löscht unwiderruflich alle lokalen, nicht-committeten Änderungen** in deinem Arbeitsverzeichnis und in der Staging Area. Nutze ihn nur, wenn du absolut sicher bist, dass du nichts von diesen Änderungen behalten möchtest.

### Fall 4: Neue, nicht verfolgte Dateien entfernen (`untracked files`)

Wenn du neue Dateien erstellt hast, die noch nie zu Git hinzugefügt (`git add`) oder committed wurden, werden sie von den obigen Befehlen nicht beeinflusst. Um diese Dateien ebenfalls zu entfernen, kannst du `git clean` verwenden:

```bash
git clean -fd
```
* `f` oder `--force` ist notwendig, da `git clean` aus Sicherheitsgründen eine Bestätigung verlangt.
* `d` oder `--directories` entfernt auch leere (und nicht leere, sofern sie untracked sind) Verzeichnisse.

**Wichtiger Tipp:** Bevor du `git clean` ausführst, kannst du eine Vorschau der Dateien sehen, die gelöscht werden würden:
```bash
git clean -n
```
(`n` oder `--dry-run`)

---

**Zusammenfassung und Empfehlung:**

Der sicherste Weg, um den Überblick zu behalten, ist in der Regel:

1.  `git status` ausführen, um den aktuellen Zustand zu sehen.
2.  Wenn Änderungen staged sind, diese mit `git restore --staged <dateiname>` oder `git reset HEAD <dateiname>` unstagen.
3.  Die nicht-gestageden Änderungen mit `git restore <dateiname>` oder `git checkout -- <dateiname>` verwerfen.
4.  Wenn es neue, nicht verfolgte Dateien gibt, diese mit `git clean -fd` (ggf. nach `git clean -n`) entfernen.

Wenn du dir absolut sicher bist, dass du **ALLES** verwerfen möchtest, was seit dem letzten Commit passiert ist, ist `git reset --hard HEAD` der schnellste Weg.