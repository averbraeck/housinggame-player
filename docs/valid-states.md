# Valid player states to enable next screen button

The table below shows the group round state and the player state that are 'compatible'. The group can move forward in state, where the player stays behind, but the opposite is not possible (the player should never be ahead of the group in terms of state). The last column shows under what conditions the next screen button will be enabled. Note that player states 40, 55, 70, 110 and 140 are different from the standard conditions when to enable the next screen button. 

| Group round state  | Gnr  | Player state      | Pnr  | Screen            | Button           | Button enabled      |
| ------------------ | ---: | ----------------- | ---: | ----------------- | ---------------- | ------------------- |
| ANY                | any  | ANY               | any  | login             | LOGIN            | always              |
| LOGIN              | 10   | LOGIN             | 10   | login-wait        | START GAME       | Gnr>Pnr (G >= 20)   |
| NEW_ROUND          | 20   | READ_BUDGET       | 20   | read-budget       | READ NEWS        | Gnr>Pnr (G >= 30)   |
| ANNOUNCE_NEWS      | 30   | READ_NEWS         | 30   | read-news         | VIEW HOUSES      | Gnr>Pnr (G >= 40)   |
| SHOW_HOUSES_SELL   | 40   | VIEW_SELL_HOUSE   | 40   | sell-house        | STAY / SELL      | Gnr>Pnr & APPROVE   |
| ALLOW_SELLING      | 50   | SELL_HOUSE_WAIT   | 50   | sell-house-wait   | MOVE OUT         | Gnr>Pnr (G >= 60)   |
| ALLOW_SELLING      | 50   | STAY_HOUSE_WAIT   | 55   | stay-house-wait   | ENJOY STAY       | Gnr >= 70           |
| SHOW_HOUSES_BUY    | 60   | VIEW_BUY_HOUSE    | 60   | buy-house         | BUY HOUSE        | Gnr>Pnr (G >= 70)   |
| ALLOW_BUYING       | 70   | BUY_HOUSE_WAIT    | 70   | buy-house-wait    | MOVE IN          | Gnr >= 70 & APPROVE |  
| BUYING_FINISHED    | 80   | BOUGHT_HOUSE      | 80   | bought-house      | VIEW TAXES       | Gnr>Pnr (G >= 90)   |
| BUYING_FINISHED    | 80   | STAYED_HOUSE      | 85   | stayed-house      | VIEW TAXES       | Gnr>Pnr (G >= 90)   |
| SHOW_TAXES         | 90   | VIEW TAXES        | 90   | view-taxes        | SHOW IMPROVEM.   | Gnr>Pnr (G >= 100)  |
| ALLOW_IMPROVEMENTS | 100  | VIEW_IMPROVEMENTS | 100  | view-improvem.    | BUY IMPROVEM.    | Gnr>Pnr (G >= 110)  |
| SHOW_SURVEY        | 110  | ANSWER_SURVEY     | 110  | answer-survey     | WAIT FOR DICE    | always              |
| SURVEY_COMPLETED   | 120  | SURVEY_COMPLETED  | 120  | survey-completed  | VIEW DAMAGE      | Gnr>Pnr (G >= 130)  |
| ROLLED_DICE        | 130  | VIEW_DAMAGE       | 130  | view-damage       | VIEW SUMMARY     | Gnr>Pnr (G >= 140)  |
| SHOW_SUMMARY       | 140  | VIEW_SUMMARY      | 140  | view-summary      | NEXT ROUND       | G-round > P-round   |

