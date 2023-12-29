# Valid player states to enable next screen button

The table below shows the group round state and the player state that are 'compatible'. The group can move forward in state, where the player stays behind, but the opposite is not possible (the player should never be ahead of the group in terms of state). The last column shows under what conditions the next screen button will be enabled. Note that player states 40, 55, 70, 110 and 140 are different from the standard conditions when to enable the next screen button. 

| Group round state  | Gnr  | Player state      | Pnr  | Screen            | Button           | Button enabled      |
| ------------------ | ---: | ----------------- | ---: | ----------------- | ---------------- | ------------------- |
| ANY                | any  | ANY               | any  | login             | LOGIN            | always              |
| LOGIN              | 10   | LOGIN             | 10   | welcome-wait      | START GAME       | Gnr>Pnr (G >= 20)   |
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


Legal combinations:

| Screen            | Player state      | Pnr  | Typical group state  | Valid group states | 
| ----------------- | ----------------- | ---: | -------------------- | ------------------ | 
| welcome-wait      | LOGIN             | 10   | LOGIN                | Gnr >= 10          | 
| read-budget       | READ_BUDGET       | 20   | NEW_ROUND            | Gnr >= 20          | 
| read-news         | READ_NEWS         | 30   | ANNOUNCE_NEWS        | Gnr >= 30          | 
| sell-house        | VIEW_SELL_HOUSE   | 40   | SHOW_HOUSES_SELL     | Gnr >= 40          | 
| sell-house-wait   | SELL_HOUSE_WAIT   | 50   | ALLOW_SELLING        | Gnr >= 50          | 
| stay-house-wait   | STAY_HOUSE_WAIT   | 55   | ALLOW_SELLING        | Gnr >= 50          | 
| buy-house         | VIEW_BUY_HOUSE    | 60   | SHOW_HOUSES_BUY      | Gnr >= 60          | 
| buy-house-wait    | BUY_HOUSE_WAIT    | 70   | ALLOW_BUYING         | Gnr >= 70          | 
| bought-house      | BOUGHT_HOUSE      | 80   | BUYING_FINISHED      | Gnr >= 70 (*)      | 
| stayed-house      | STAYED_HOUSE      | 85   | BUYING_FINISHED      | Gnr >= 70 (*)      | 
| view-taxes        | VIEW TAXES        | 90   | SHOW_TAXES           | Gnr >= 90          | 
| view-improvem.    | VIEW_IMPROVEMENTS | 100  | ALLOW_IMPROVEMENTS   | Gnr >= 100         | 
| answer-survey     | ANSWER_SURVEY     | 110  | SHOW_SURVEY          | Gnr >= 110         | 
| survey-completed  | SURVEY_COMPLETED  | 120  | SURVEY_COMPLETED     | Gnr >= 110 (*)     | 
| view-damage       | VIEW_DAMAGE       | 130  | ROLLED_DICE          | Gnr >= 130         | 
| view-summary      | VIEW_SUMMARY      | 140  | SHOW_SUMMARY         | Gnr >= 140         | 
