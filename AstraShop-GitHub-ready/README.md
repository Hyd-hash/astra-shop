# AstraShop

A custom-branded, pink/purple GUI shop plugin for **Astra Network**, built for Paper 1.21.x.
Replicates the core Donut SMP shop experience — click-to-buy/sell categorized GUIs — with a
hybrid pricing engine (per-item toggle between live market pricing and fixed admin prices),
full Vault integration, and no dependency on any other shop plugin.

## Features

- **Main menu + category GUIs**, pink accent border / purple interior, fully re-themeable via MiniMessage/hex/legacy colors in `config.yml`.
- **Hybrid pricing**: each item in `shops.yml` is `dynamic: true` (price drifts with supply & demand, like Donut SMP) or `dynamic: false` (fixed admin price).
- **All buying/selling happens in the GUI** — no `/sell hand` commands, matching your requested flow:
  - Left-click: buy 1
  - Shift-left-click: buy 64
  - Right-click: sell 1 (from your inventory)
  - Shift-right-click: sell **all** matching items in your inventory
- **Price regen**: dynamic prices slowly drift back toward their base value over time so the market recovers after spikes.
- **Vault + EssentialsX**: hooks Vault's economy API directly, so it works with EssentialsX (or any other Vault-compatible economy) automatically. No direct Essentials dependency needed.
- **Fully data-driven**: categories/items/prices live in `shops.yml`, all text in `messages.yml`, all behaviour/tuning in `config.yml` — no code changes needed for normal customization.
- **Admin tools**: `/astrashop reload|save|setprice|resetprice|give`.
- **Persistence**: live market multipliers are saved to `data.yml` on an interval and on shutdown, and restored on startup/reload.

## Requirements

- Java 21
- Maven 3.9+
- A Paper 1.21.x server
- [Vault](https://www.spigotmc.org/resources/vault.34315/) + an economy plugin (EssentialsX recommended)

## Building

```bash
cd astra-shop
mvn clean package
```

The compiled jar will be at `target/AstraShop-1.0.0.jar`. A convenience `build.sh` is included
that does the same thing with a friendlier summary at the end.

> **Note on the Paper version:** `pom.xml` targets `paper-api` version `1.21.11-R0.1-SNAPSHOT`
> to match "Paper 1.21.11". Paper's Maven repo (`repo.papermc.io`) usually only has artifacts
> for versions that have actually been released as a Paper build. If `mvn package` fails with a
> "could not find artifact" error for `paper-api`, open `pom.xml` and change the `<paper.version>`
> property to the closest version that Paper has published (check
> https://repo.papermc.io/#browse/browse:maven-public:io%2Fpapermc%2Fpaper%2Fpaper-api ), e.g.
> `1.21.4-R0.1-SNAPSHOT`. The plugin code itself doesn't use any version-specific API, so this is
> just a matter of picking an artifact that exists — it will still run fine on your actual 1.21.11 server.

## Installation

1. Build the jar (see above).
2. Drop `AstraShop-1.0.0.jar`, `Vault.jar`, and `EssentialsX.jar` into your server's `plugins/` folder.
3. Start the server once to generate `plugins/AstraShop/config.yml`, `messages.yml`, and `shops.yml`.
4. Edit those files to taste (see below), then `/astrashop reload`.

## Configuring your shop

### Adding/editing items — `shops.yml`

```yaml
categories:
  ores:
    display-name: "<gradient:#ff6ec7:#a260ff>Ores & Valuables</gradient>"
    icon: DIAMOND_ORE
    slot: 11          # position in the main menu (optional)
    items:
      diamond:
        material: DIAMOND
        dynamic: true        # market price, moves as people trade
        base-buy: 180.0       # equilibrium buy price
        min-multiplier: 0.4   # optional per-item override of the global bounds
        max-multiplier: 3.0
      nether_star:
        material: NETHER_STAR
        dynamic: false        # fixed price, never moves
        base-buy: 5000.0
        base-sell: 3500.0
        sellable: true
        buyable: true
```

Every `Material` enum name from the Paper/Bukkit API is valid. Set `buyable: false` or
`sellable: false` on any item to make it one-directional (e.g. Elytra: buyable but not sellable).

### Tuning the market — `config.yml`

- `dynamic-pricing.step-per-unit` — how much the price shifts per item traded (0.001 = 0.1%).
- `dynamic-pricing.min-multiplier` / `max-multiplier` — how far prices can swing from base (default 0.25x–4x).
- `dynamic-pricing.sell-margin` — sell price as a fraction of the live buy price (0.75 = 75%), this is
  what keeps the shop from being a free money farm.
- `dynamic-pricing.regen` — how fast prices drift back to normal when nobody's trading.
- `transaction-cooldown-ms` — anti-spam-click cooldown between purchases/sales.

### Colors & text — `messages.yml` / `config.yml`

Every string supports both legacy `&`-codes and MiniMessage tags (including gradients and hex),
so you can freely mix `&d`, `<light_purple>`, and `<gradient:#ff6ec7:#a260ff>` in the same string.

## Admin commands

| Command | Description |
|---|---|
| `/shop` | Opens the shop GUI (also `/ashop`) |
| `/astrashop reload` | Reloads config.yml/messages.yml/shops.yml, keeps current market prices |
| `/astrashop save` | Force-saves current market prices to data.yml |
| `/astrashop setprice <category> <item> <buy\|sell> <amount>` | Overrides the live price of a dynamic item |
| `/astrashop resetprice <category> <item>` | Resets a dynamic item's price back to its base value |
| `/astrashop give <player> <category> <item> <amount>` | Gives a player a shop item directly, for testing |

Permissions: `astrashop.use` (default: everyone), `astrashop.admin` (default: op),
`astrashop.bypasscooldown` (default: op).

## Balancing tips for your economy

- Start `step-per-unit` small (0.0005–0.002). Higher values make prices swing hard with bulk buying/selling.
- Tighten `min-multiplier`/`max-multiplier` on early/common items (cobblestone, dirt) so they can't crash to near-zero and get spammed.
- Loosen the bounds on rare/late-game items (diamonds, netherite, nether star) so scarcity actually matters.
- Keep `sell-margin` around 0.7–0.85; lower values discourage buy-then-immediately-sell farming loops.
- Use `dynamic: false` for anything you want to act as a fixed money sink (e.g. totems, elytra, spawners) rather than a farmable commodity.
