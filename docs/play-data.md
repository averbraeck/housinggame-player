# Storage of dynamic play data

The dynamic play data is stored in four main classes:
- `GroupRound` to store the group related information such as the playing round, the group state, and the dice rolls.
- `HouseGroup` to store all information related to owned and available houses.
- `PlayerRound` to store all player related choices and state variables.
- `HouseTransaction` to store the information about an ongoing house transaction temporarily.


## GroupRound
The `GroupRound` record stores the round number and the group state for the current round (as encoded by the `GroupState` class in the common project). Furthermore, it stores the dice roll values for the rain and river flooding. Every round has its own `GroupRound` record.


## HouseGroup
The `HouseGroup` record stores the information about the current state of the house. It does, therefore, not store round-based information. Round-based house information about owned houses has to be retrieved from the `PlayerRound` record.

> [!NOTE]
> In the future, we can choose to store round-based information on houses in a `HouseRound` record that maintains the house information per round outside of the `PlayerRound` record for the player.

The most important fields that might need some explanation are:
- `code` is the shore code of the house, such as N01.
- `address` is the longer address of the house, such as Steelstreet 1.
- `rating` and `originalPrice` are static and copied from the `house` record.
- `damageReduction` is the reduction in price as a result of a past flooding. 
- `marketValue` is the price at which the bank would buy or sell the house at this moment. This is currently the `originalPrice` minus the `damageReduction`. Later, some value of the implemented measures could be added to the market value of a house as well.
- `lastSoldPrice` is the price for which the house was last sold by the bank to a player, or the price for which the bank bought back the house. In other words, the last transaction price.
- `houseSatisfaction` is the sum of the satisfaction points of the implemented measures for the house. The house satisfaction is different from the player satisfaction, as the player loses or gains these satsifaction points when selling or buying a house.
- `status` is the house status as coded by `HouseGroupStatus`. The three values are `AVAILABLE`, `OCCUPIED`, or `NOT_AVAILABLE` (used in a round but currently not available for sale).
- `pluvialBaseProtection` and `fluvialBaseProtection` are static, and copied from the community to which the house belongs.
- `pluvialHouseProtection` and `fluvialHouseProtection` are the delta on the base protection based on the implemented measures.
- `lastRoundCommFluvial` and `lastRoundCommPluvial` contain the last round number when the area (community) was flooded.
- `lastRoundHouseFluvial` and `lastRoundHousePluvial` are the last round number when the house was flooded.


## PlayerRound
The `PlayerRound` record stores all information about a player per round. It contains a breakdown of the mortgage, costs, satisfaction, house ownership, and damage for the player. The data will be broken down in a number of categories.

### base info
The following fields contain the basic information for the player round data:
- `roundIncome` and `livingCosts` are imported from the `WelfareType`.
- `playerState` shows the progress of the player in the current round (what screen is active), as encoded by the `PlayerState` class in the common project.

### mortgage
The following fields relate to the mortgage for the player round data:
- `maximumMortgage` is a static field from the `WelfareType` containing the maximum mortgage allowed for this player by the bank.
- `mortgageHouseStart` is the value of the original loan from the bank for the current house at the start of the round (so, based on the house bought in an earlier round). The mortgage value does not change per round as long as the player stays in the same house.
- `mortgageHouseEnd` is the value of the original loan from the bank for the current house. This value is equal to `mortgageHouseStart` if the player stayed in the same house, and equal to the new mortgage if the player bought a new house in this round.
- `mortgagePayment` is the amount of money paid in this round to the bank as a redemption of the mortgage. The actual debt to the bank is reduced by this payment.
- `mortgageLeftStart` is the debt to the bank at the start of the round. It is equal to the original mortgage value for the house minus all mortgage payments made in previous rounds for that house.
- `mortgageLeftEnd` is the debt to the bank at the end of the round. If the player stayed in the same house, it is equal to the original mortgage value for the house minus all mortgage payments for that house up to and including the current round. If the player moved to a new house, it is the mortgage left after one payment to the bank.

### costs
The following fields encode cost information for the player round:
- `roundIncome` and `livingCosts` are fixed and copied from the `WelfareType`.
- `mortgagePayment` is based on a percentage of the original mortgage loan from the bank for the house. The mortgage payment is calculated **after** the sell-house and buy-house stages of the round, so it is always based on the mortgage of the new house (if any).
- `costXX` are the fields for the respective costs as calculated during the round; each cost is calculated during a specific state of the player round.
- `profitSoldHouse` and `spentSavingsForBuyingHouse` relate to the selling and buying of a house.
- `paidDebt` is purely an information value and indicates how much of the `roundIncome` has been used to pay off debt from the previous round at the start of the round.
The `spendableIncome` of a player is dynamically updated throughout a round. It is based on the following calculation:

```
spendableIncome in the new round =
  + spendableIncome of the previous round 
  + roundIncome
  - livingCosts
  - mortgagePayment
  + profitSoldHouse
  - spentSavingsForBuyingHouse
  - costTaxes
  - costMeasuresBought
  - costSatisfactionBought
  - costFluvialDamage
  - costPluvialDamage
```

### satisfaction


### flooding

