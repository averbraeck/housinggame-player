# Calculation of flood damage

The flood damage for all players in a group is calculated by the facilitator app for that group. When the facilitator enters the dice rolls and presses "ROLL DICE, SHOW DAMAGE", the calculation takes place. At the same time, the players can open the "SHOW DAMAGE" menu on their mobile phone to get a detailed assessment of the damage to their house in the current round.


#### 1. Calculation of the protection level of the house
- Each house has a **community** fluvial and pluvial protection (`HouseGroup.pluvialBaseProtection` and `HouseGroup.fluvialBaseProtection`)
- Based on measures, the house can have a higher **house** fluvial and pluvial protection (`HouseGroup.pluvialHouseProtection` and `HouseGroup.fluvialHouseProtection`)
- Based on active `NewsEffects`, the pluvial and/or fluvial **community** protection can have been changed in a positive or negative way, from a certain round onward. This effect can be for a single or for multiple communities. This effect is the **delta** fluvial and pluvial protection.
- Summing community, house and delta values leads to a **total fluvial and pluvial house protection** (calculated).
- Summing community and delta values leads to a **total fluvial and pluvial community protection** (calculated).
- Since these are **dynamic** values, they are stored in the `PlayerRound` record to be able to reproduce the game flow.

> [!WARNING]  
> We need to decide whether `NewsEffects` are calculated when the news is effective (changing the values in the database for the game play) or when the values are used (looping over the `NewsEffects` till the current round, and applying the changes. Since most news effects do not have a field that can be updated in the dame (e.g., tax discounts, living bonus, moving changes, etc.), the news effect calculation takes place at the final calculations and is not changing any fields in the database.


#### 2. Calculation of the damage
- The rolled dice values for pluvial and fluvial damage are stored in the `GroupRound` record.
- The difference between the dice roll and the total fluvial or pluvial house protection is the pluvial or fluvial **house damage**. These values will be called `pluvialHouseDamage` and `fluvaialHouseDamage` in the subsequent items.
- The difference between the dice roll and the total fluvial or pluvial community protection is the pluvial or fluvial **community damage**. These values will be called `pluvialCommunityDamage` and `fluvaialCommunityDamage` in the subsequent items.
- If pluvial community damage > 0, the `HouseGroup.lastRoundCommPluvial` field is set to the current round number.
- If fluvial community damage > 0, the `HouseGroup.lastRoundCommFluvial` field is set to the current round number.
- If pluvial house damage > 0, the `HouseGroup.lastRoundHousePluvial` field is set to the current round number.
- If fluvial house damage > 0, the `HouseGroup.lastRoundHouseFluvial` field is set to the current round number.
- The damage values themselves do not have to be stored in the `PlayerRound` table, since they can be recalculated  (the protection levels for the current round and the current house are stored in the `PlayerRound` table). 
