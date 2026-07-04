# PearlControl

Egy saját, **nyílt forráskódú** ender pearl plugin **Paper 1.21.8**-ra (Java 21).
A VortexPearls fő funkcióit valósítja meg a nulláról írt, tiszta kóddal — nem
tartalmaz visszafejtett vagy másolt kódot semmilyen kereskedelmi pluginból.

## Funkciók

- **Ender pearl cooldown** – állítható másodpercben (`cooldown`).
- **Bypass jog** – a `pearlcontrol.bypass` joggal rendelkezők nincsenek cooldownolva.
- **Cooldown megjelenítés** – `CHAT`, `ACTIONBAR`, `TITLE` vagy `NONE`.
- **Anti-glitch** – megakadályozza a klasszikus HCF "falba pearlözés" glitchet
  (csak teli, tömör blokkba ragadást tilt, a lapokat/lépcsőket nem).
- **Refund** – blokkolt teleport / tiltott világ esetén visszaadja a pearlt,
  opcionálisan törli a cooldownt is.
- **Cross-world tiltás** – opcionális (`block-cross-world`).
- **Tiltott világok** – `disabled-worlds` listával.
- **Hangok** – deny/throw hang (verziófüggetlen, névvel megadva).
- **PlaceholderAPI** – ha jelen van, elérhető:
  - `%pearlcontrol_cooldown%` – hátralévő másodperc (1 tizedes)
  - `%pearlcontrol_cooldown_int%` – felfelé kerekítve
  - `%pearlcontrol_on_cooldown%` – `true` / `false`

## Parancsok

| Parancs | Leírás | Jog |
| --- | --- | --- |
| `/pearlcontrol reload` | Config újratöltése | `pearlcontrol.admin` |
| `/pearlcontrol help` | Súgó | `pearlcontrol.admin` |

Aliasok: `/pc`, `/pearl`.

## Jogosultságok

- `pearlcontrol.admin` – admin parancsok (alap: OP)
- `pearlcontrol.bypass` – cooldown megkerülése (alap: false)

## Fordítás

Java 21 és Maven szükséges. A Paper API a hivatalos PaperMC Maven-repóból jön
(`repo.papermc.io`), a PlaceholderAPI pedig az `repo.extendedclip.com`-ról.

```bash
mvn clean package
```

A kész jar a `target/PearlControl-1.0.0.jar` lesz — ezt kell a szerver
`plugins/` mappájába másolni.

> Megjegyzés: a build a fenti két Maven-repóból tölt le függőségeket. Olyan
> környezetben kell fordítani, ahol ezek elérhetők (a legtöbb fejlesztői gépen
> és CI-n igen).

## Konfiguráció

Az összes beállítás a `src/main/resources/config.yml`-ben van dokumentálva; a
plugin első indításkor kimásolja a szerver `plugins/PearlControl/config.yml`
helyére. Módosítás után `/pearlcontrol reload`.

## Kompatibilitás

- **Cél verzió:** Paper 1.21.8 (`api-version: '1.21'`).
- A hangkezelés a `playSound(Location, String, float, float)` változatot
  használja, mert az `org.bukkit.Sound` 1.21.3 óta enumból Registry-interfésszé
  vált — így a plugin a teljes 1.21.x sávban működik.
