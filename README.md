# Questing Reputation

An addon mod for Better Questing that reimplements reputation tasks and rewards (better than the half-baked scoreboard system in BQStandard) as well as the death tasks from HQM.
See a video of the reputation tasks and rewards [here](https://youtu.be/BlM5f1umOGk).

### Features

 - Configurable "factions" system for use with quest tasks and rewards that need and give players/parties reputation with them.
 - Quest tasks that require the player/party has a certain reputation level with a specific faction.
 - Quest tasks that require a certain number of deaths (counted among the entire party if applicable).
 - Quest rewards that change the reputation for the player/party.

### Getting Started

Creating factions for reputation:
The faction config is located in config/questingreputation/factions.json. Run the mod once to generate a default config if that file is not present.
That config is a JSON file with the following format:
```json
{
  "factionID": {
    "name": "A String with the display name of your faction (defaults to the factionID). Can be a translation key.",
    "item": "A String specifying an item to represent this faction when shown in quests, in the format \"modid:itemname(:meta optional)\"",
    "deathChange": "An int that sets how much reputation will be lost on death (overrides the default config in questrep.cfg)",
    "defaultReputation": "An int that sets the reputation new players will start at for this faction (overrides the default config in questrep.cfg)",
    "tiers": [
    {
      "name": "A string with the tier name. Can be a translation key.",
      "value": "The reputation value for this tier. Applies to a player if this tier is the one closest to 0 that the player's reputation is in."
    }, ...
    ]
  },
  "factionID2":  { ... }
}
```

Creating Reputation Requirement Tasks:
1. Create a new quest as normal.
2. Select questrep:reputation in the Tasks selection for a Reputation Task.
3. Configure the chosen task in the editor. Select a faction, set lower and/or upper bounds, and choose if the range should be inverted. A preview of the task's requirement will be available at the bottom of the editor.
4. Set any other tasks.
5. Set any rewards.
6. The quest is ready!

Creating Death Tasks:
1. Create a new quest as normal.
2. Select questrep:deaths in the Tasks selection for a Number of Deaths Task.
3. Set the number of deaths in the editor.
4. Set any other tasks.
5. Set any rewards.
6. The quest is done!

Creating Reputation Change Rewards:
1. Create a new quest as normal.
2. Set any number of tasks, such as item retrievals or reputation requirements.
3. Choose questrep:reputation in the Rewards selection for a Reputation Reward.
4. Select the faction and set the amount that reputation will change by.
5. Set any other rewards.
6. The quest is done!

### Coming Soon
This mod should be fully functional in single and multiplayer (although it hasn't really been tested yet). The following features are planned for the future:

 - A visual editor for the different factions, including tiers, display names, the death change amount, and players' default reputation.
 - Commands to modify players' reputation.
 - A central location in BQ for players to see their standing with all factions and all factions' tiers.
 - An HQM quest importer which can convert its reputation and death tasks and reputation rewards.
 - A way for a party to only claim reputation from a Reputation Change reward once.
 - Anti-cheese for players creating, leaving, and rejoining parties to game the reputation system.

Special thanks to [ExampleMod for 1.7.10!](https://github.com/GTNewHorizons/ExampleMod1.7.10)
