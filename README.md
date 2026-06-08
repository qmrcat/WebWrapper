# Web Wrapper Premium for Android

Benvingut a **Web Wrapper**, una aplicació de primer nivell dissenyada per transformar els teus llocs web preferits en aplicacions mòbils completament integrades, netes i lliures de distraccions.

Aquesta aplicació utilitza un disseny modern basat en **Material Design 3** i corre completament de manera nativa sota l'entorn d'Android amb les millors pràctiques de desenvolupament actuals.

---

## 🚀 Característiques Principals

### 1. **Navegació Web de Polida Eficiència (Browse)**
*   **Mode Pantalla Completa:** Opció per navegar de forma completament immersiva aprofitant cada píxel de la teva pantalla.
*   **Controls Intel·ligents i Ocultació Automàtica:** Els controls de navegació principal (enrere, endavant, recarregar, etc.) s'oculten automàticament un cop la pàgina s'ha carregat del tot per oferir una visió sense barreres. També pots activar per restaurar els controls lliscant cap avall (*pull-to-refresh*) o mitjançant un minúscul botó translúcid i flotant per minimitzar molèsties.
*   **Control del Motor JS:** Activa o desactiva de manera individualitzada l'execució de JavaScript per a cada lloc web, optimitzant la càrrega o la privacitat segons les teves preferències.

### 2. **Biblioteca de Llocs Guardats (Library)**
*   Organitza totes les teves aplicacions web en una llista unificada, de fàcil accés i visualment atractiva.
*   Afegeix, edita o elimina qualsevol lloc en qüestió de segons.

### 3. **Dreceres Personalitzables a l'Inici (Shortcuts)**
Crear un accés directe a la pantalla principal d'Android d'un lloc web mai ha estat tan interactiu. L'usuari disposa d'un diàleg complet amb vista prèvia en temps real que li permet escollir:
*   **Favicon Oficial:** L'aplicació descarrega automàticament i escala la icona oficial del lloc web desitjat com a icona de l'accés directe.
*   **Icona de l'Aplicació:** Utilitza la elegant línia gràfica corporativa nativa del nostre Web Wrapper.
*   **Emoji Personalitzat:** Permet seleccionar un dels múltiples dissenys integrats en una llista de presets (com ara un globus terraqüi, notes, videojocs, xats, compres, música, etc.) combinat amb fons de colors vius i professionals estil Material Design per a una millor identificació visual i personalització integral.

### 4. **Persistència i Robustesa**
*   L'aplicació guarda i gestiona totes les dades del lloc de forma 100% local, respectant la privacitat del teu dispositiu i assegurant una velocitat d'inici immillorable.

---

## 🛠️ Tecnologies Utilitzades

L'aplicació ha estat construïda emprant un conjunt de tecnologies modernes que garanteixen escalabilitat i alt rendiment:

*   **Llenguatge:** [Kotlin](https://kotlinlang.org/) – El llenguatge de programació estàndard i modern per a aplicacions d'Android.
*   **Interfície d'Usuari (UI):** [Jetpack Compose](https://developer.android.com/compose) – El motor declaratiu modern d'Android d'última generació sota els estàndards visuals de **Material Design 3 (M3)**.
*   **Persistència de Dades:** [Room Database](https://developer.android.com/training/data-storage/room) – Base de dades local SQLite de nivell de producció que emmagatzema de manera òptima i segura les teves drecere, preferències i llocs desats.
*   **Càrrega d'Imatges:** [Coil](https://coil-kt.github.io/coil/) – Motor eficient de càrrega d'imatges asíncrones per descarregar de forma nativa i fluida els favicons de qualsevol enllaç de la xarxa.
*   **Concurrència i Asincronia:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) i [StateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/) – Gestió fluida dels fils i fluxos de dades per a una navegació reactiva que evita bloqueigs de la pantalla.
*   **Gestió d'Icons d'Accés Directe:** Integració directa amb `ShortcutManagerCompat` d'Android per garantir compatibilitat del llançament de dreceres des de múltiples versions del sistema Android.

---

## 📱 Requisits i Funcionament

*   **Android SDK Mínim:** API 26 (Android 8.0) o superior (requerit per a les funcions adaptatives d'accés directe i icones d'escriptori de drecera dinàmiques).
*   **Navegació Immersiva:** Requereix compatibilitat de canvis d'orientació i barres del sistema en format Edge-to-Edge integrat nativament.

Fes de qualsevol pàgina web una aplicació nativa a la teva mida! 🌐
