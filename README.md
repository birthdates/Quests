# Quests

A simple quest system for Minecraft (tested on 1.20.4).

## Features

- Cross-server support
- Multiple language support
- Multiple active quests
- Quests can be completed in any order
- Quests with expirations
- API for other plugins to utilize
- Quests can be edited in-game

## Requirements

- PostgresSQL `9.6` or higher
- Redis server
- Java 17 (in path)

## Usage

### Installation

1. Clone [MockBukkit](https://github.com/MockBukkit/MockBukkit)
2. Run `gradle publishToMavenLocal` (requires [Gradle](https://gradle.org/install/))
3. Clone this repository
4. Run `gradle build` (requires [Gradle](https://gradle.org/install/))
5. Copy `build/libs/Quests-*.jar` to your server's `plugins` folder
6. Start the server
7. Configure the plugin
8. Restart the server

### Language

The plugin supports multiple languages. The default language is English. The language a player sees is based on their
client.

#### Editing

To edit the language, utilize `/language` (requires `quests.admin`)

### Quests

To edit quests utilize `/quests admin` (requires `quests.admin`)

To view quests utilize `/quests`