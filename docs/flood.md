# Calculation of flood damage

The flood damage for all players in a group is calculated by the facilitator app for that group. When the facilitator enters the dice rolls and presses "ROLL DICE, SHOW DAMAGE", the calculation takes place. At the same time, the players can open the "SHOW DAMAGE" menu on their mobile phone to get a detailed assessment of the damage to their house in the current round.


#### 1. Calculation of the protection level of the house
- Each house has a **community** fluvial and pluvial protection (`HouseGroup.pluvialBaseProtection` and `HouseGroup.fluvialBaseProtection`).
- Based on measures, the house can have a higher **house** fluvial and pluvial protection (`HouseGroup.pluvialHouseProtection` and `HouseGroup.fluvialHouseProtection`). Do **not** count measures whose `valid_till_round` is filled with a round number, since they have already been consumed (e.g., sandbags).
- Based on active `NewsEffects`, the pluvial and/or fluvial **community** protection can have been changed in a positive or negative way, from a certain round onward. This effect can be for a single or for multiple communities. This effect is the **delta** fluvial and pluvial protection.
- Summing community, house and delta values leads to a **total fluvial and pluvial house protection** (calculated).
- Summing community and delta values leads to a **total fluvial and pluvial community protection** (calculated).
- Since these are **dynamic** values, they are stored in the `PlayerRound` record to be able to reproduce the game flow.

> [!WARNING]  
> We need to decide whether `NewsEffects` are calculated when the news is effective (changing the values in the database for the game play) or when the values are used (looping over the `NewsEffects` till the current round, and applying the changes. Since most news effects do not have a field that can be updated in the database (e.g., tax discounts, living bonus, moving changes, etc.), the news effect calculation takes place where it is applied, and news effects are not changing any fields in the database.


#### 2. Calculation of the damage
- The rolled dice values for pluvial and fluvial damage are stored in the `GroupRound` record.
- The difference between the dice roll and the total fluvial or pluvial house protection is the pluvial or fluvial **house damage**. These values will be called `pluvialHouseDamage` and `fluvaialHouseDamage` in the subsequent items.
- The difference between the dice roll and the total fluvial or pluvial community protection is the pluvial or fluvial **community damage**. These values will be called `pluvialCommunityDamage` and `fluvaialCommunityDamage` in the subsequent items.
- If `(pluvialCommunityDamage > 0)`, the `HouseGroup.lastRoundCommPluvial` field is set to the current round number.
- If `(fluvialCommunityDamage > 0)`, the `HouseGroup.lastRoundCommFluvial` field is set to the current round number.
- If `(pluvialHouseDamage > 0)`, the `HouseGroup.lastRoundHousePluvial` field is set to the current round number.
- If `(fluvialHouseDamage > 0)`, the `HouseGroup.lastRoundHouseFluvial` field is set to the current round number.
- The damage values themselves do not have to be stored in the `PlayerRound` table, since they can be recalculated  (the protection levels for the current round and the current house are stored in the `PlayerRound` table). 


#### 3. See if one-time-only measures are used
- One time only measures are, e.g., sandbags. They are denoted with the `validTillUsage` value being true in `MeasureType`. 
- If `(pluvialHouseDamage < 0)` (note: when == 0, all measures were needed), see if there were one or more measures that are valid till usage, and that can be left out to still have a damage <= 0. Watch for side-effects where the fluvial damage might become > 0.
- If `(fluvialHouseDamage < 0)` (note: when == 0, all measures were needed), see if there were one or more measures that are valid till usage, and that can be left out to still have a damage <= 0. Watch for side-effects where the pluvial damage might become > 0.
- If a one-time measure needs to be used, set its `consumedInRound` value to the current round number.


#### 4. Calculate the cost and satisfaction penalty for the user
- The `ScenarioParameters` table has all the costs and penalties as either fixed or per point, and split between pluvial and fluvial damage and between house and community.
- The `PlayerRound` table has entries for `costFluvialDamage` and `costPluvialDamage`. The calculation has a fixed component (if damage > 0 add cost) and a dynamic component (add damage * cost). The costs need to be subtracted from the `spendableIncome`.
- The `PlayerRound` table has entries for `satisfactionFluvialPenalty` and `satisfactionPluvialPenalty`. The calculation has a fixed component if the area is flooded (if communityDamage > 0 add penalty), a fixed component if the house is flooded (if houseDamage > 0, add penalty) and a dynamic component if the house if flooded (add penalty * house damage). The penalties need to be subtracted from the `personalSatisfaction`. 

> [!WARNING]
> Satisfaction penalties can lead to negative satisfaction. The `ScenarioParameters` indicate whether negative satisfaction for the player is allowed or not. This has to be taken into account in the calculation.

> [!NOTE]
> Flood damage impacts the personal satisfaction. It is not something that is bought or sold with the house, such as the positive psychological effect of having applied measures to the house.


#### 5. Apply damage reduction to the house
- Depending on the active `NewsEffects`, the house can lose value if it has been flooded, also in subsequent rounds. The `damageReduction` field has to be calculated for the house, based on news effects, independent of whether it was flooded in this round or in previous rounds.
- The discount is also processed in the `marketValue` of the house `(lastSoldPrice - damageReduction)` or if `lastSoldPrice` is null, `(originalPrice - damageReduction)`.

> [!WARNING]
> Damage reductions also need to be calculated for houses that have not yet been sold. This is difficult for houses that are not yet on the market. We have to see how to proceed here...
