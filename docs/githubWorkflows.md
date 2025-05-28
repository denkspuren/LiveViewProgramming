# GitHub Workflows

In diesem Repository kommen zwei GitHub Workflows zum Einsatz, die den Build- und Release-Prozess automatisieren.
Der erste Workflow wird bei jedem Push auf einen beliebigen Branch ausgeführt, mit Ausnahme des main-Branches. Dabei wird der aktuelle Stand des Projekts gebaut, eine lauffähige JAR-Datei erstellt und als Build-Artefakt hochgeladen. So steht zu jedem Zeitpunkt eine funktionierende Version für Tests oder weitere Verarbeitung bereit.

Der zweite Workflow wird nur bei einem Push auf den main-Branch ausgelöst. Er kümmert sich automatisch um die Versionsverwaltung: Basierend auf der Commit-Message wird entweder die Major-, Minor- oder Patch-Version erhöht. Anschließend wird ein entsprechender Git-Tag gesetzt und ein GitHub Release erstellt.

## Build-Workflow (`build.yml`)

Dieser Workflow sorgt dafür, dass bei jedem Push auf einen beliebigen Branch (außer main) sowie bei manueller Auslösung eine lauffähige JAR erstellt und als Artefakt gespeichert wird.

### Trigger
- **push (branches-ignore: "main")**: Der Workflow wird bei jedem Push auf einen Branch ausgeführt, außer auf main. Damit werden nur Entwicklungs-Branches automatisch gebaut.
- **workflow_dispatch**: Der Workflow kann zusätzlich manuell über die GitHub-Oberfläche gestartet werden.

### Job

Der Workflow definiert einen Job namens build, der auf einer frischen Ubuntu-Umgebung (ubuntu-latest) läuft.

- **Checkout code**: Der aktuelle Stand des Repositorys wird in den GitHub-Runner geladen.

- **Set up Java**: Es wird Java 24 (Temurin-Distribution) installiert.

- **Build JAR**: Mit Maven (`mvn clean package`) wird das Projekt gebaut.

- **Upload JAR as artifact**: Die JAR-Datei (`target/lvp-*.jar`) wird als Artefakt hochgeladen. Wird keine Datei gefunden werden, schlägt der Workflow fehl. Das Artefakt bleibt für 14 Tage gespeichert.

## Release-Workflow (`release.yml`)
Dieser Workflow sorgt dafür, dass beim Push auf den main-Branch automatisch die Version angepasst wird, ein neuer Git-Tag erstellt wird und ein GitHub Release inklusive JAR-File generiert wird.

### Trigger

- **push (branches: "main")**: Der Workflow wird nur dann ausgelöst, wenn ein Push auf den main-Branch erfolgt.

### Job 1: bump
Dieser Job kümmert sich um das Erhöhen der Maven-Version und das Erstellen eines Git-Tags.

- **Checkout code**: Der aktuelle Stand des Repositorys wird in den GitHub-Runner geladen.

- **Set up Java**: Es wird Java 24 (Temurin-Distribution) installiert.

- **Determine version bump**: Liest die aktuelle Version aus der `pom.xml` aus und entscheidet basierend auf der Commit-Message (#major, #minor, oder Standard), welche Version erhöht wird.
Die neue Version wird als Output version für nachfolgende Jobs gespeichert.

- **Set new version in pom.xml**: Setzt die neue Version in der `pom.xml` mittels `mvn versions:set` und `mvn versions:commit`.

- **Commit version bump and tag**:
  - Konfiguriert Git für Commits.
  - Committet die neue Version und pusht den Commit.
  - Erstellt und pusht ein neues Git-Tag.

- **Build JAR with new version**: Baut das Projekt mit der neuen Version.

- **Upload JAR as artifact**: Lädt das erstellte JAR-File als Artefakt hoch. Dieses wird später für das Release benötigt und bleibt für einen Tag gespeichert.

### Job 2: release
Dieser Job erstellt ein neues GitHub Release basierend auf dem Tag und nutzt das gebaute Artefakt.
Der Release-Job startet erst, nachdem der Bump-Job erfolgreich abgeschlossen ist.

- **Checkout code**: Der aktuelle Stand des Repositorys wird in den GitHub-Runner geladen, diesmal mit allen Tags (fetch-depth: 0).

- **Download built artifact**: Lädt das zuvor hochgeladene JAR-Artefakt herunter und speichert es unter `target/`.

- **Create GitHub Release**:
    - Erstellt einen neuen Release auf GitHub basierend auf dem neuen Tag.
    - Setzt den Release auf Draft, damit er manuell überprüft werden kann.
    - Der Changelog wird in den Beschreibungstext des Releases eingefügt.
    - Die JAR-Datei wird als Anhang hinzugefügt.


### Erklärung der Versionserhöhung
#### 1. Ermitteln der aktuellen Version
```bash
old_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
```
Dieser Befehl ruft die aktuelle Version des Maven-Projekts aus der `pom.xml` ab. Diese wird in der Variable `old_version` gespeichert.

#### 2. Zerlegen der Version in Bestandteile
```bash
IFS='.' read -r major minor patch <<< "$old_version"
```
Dieser Befehl liest die Version ein und teilt sie mit dem Punkt als Trenner in major, minor und patch auf und speichert sie in den entsprechenden Variablen ab.

#### 3. Lesen der Commit-Nachricht
```bash
msg=$(git log -1)
```
Dieser Befehl liest die letzte Commit-Nachricht und speichert sie in der Variable `msg`.

#### 4. Entscheidung der Versionserhöhung
```bash
if echo "$msg" | grep -iq "#major"; then
    new_version="$((major + 1)).0.0"
elif echo "$msg" | grep -iq "#minor"; then
    new_version="$major.$((minor + 1)).0"
else
    new_version="$major.$minor.$((patch + 1))"
fi
```
Dieser Abschnitt prüft die Commit-Nachricht auf #major oder #minor. Wird #major gefunden, wird die Major-Version erhöht und Minor sowie Patch auf 0 gesetzt. Bei #minor wird nur die Minor-Version erhöht und die Patch-Version auf 0 gesetzt. Fehlen beide Tags, wird nur die Patch-Version um 1 erhöht.