# Valid player states to enable next screen button

| Group round state  | Gnr  | Player state      | Pnr  | Screen            | Button           | Button enabled     |
| ------------------ | ---: | ----------------- | ---: | ----------------- | ---------------- | ------------------ |
| ANY                | any  | ANY               | any  | login             | LOGIN            | always             |
| LOGIN              | 10   | LOGIN             | 10   | login-wait        | START GAME       | Gnr>Pnr (G >= 20)  |
| NEW_ROUND          | 20   | READ_BUDGET       | 20   | read-budget       | READ NEWS        | Gnr>Pnr (G >= 30)  |
| ANNOUNCE_NEWS      | 30   | READ_NEWS         | 30   | read-news         | VIEW HOUSES      | Gnr>Pnr (G >= 40)  |
| SHOW_HOUSES_SELL   | 40   | VIEW_SELL_HOUSE   | 40   | sell-house        | STAY / SELL      | Gnr>Pnr & APPROVE  |
| ALLOW_SELLING      | 50   | SELL_HOUSE_WAIT   | 50   | sell-house-wait   | MOVE OUT         | Gnr>Pnr (G >= 60)  |
| ALLOW_SELLING      | 50   | STAY_HOUSE_WAIT   | 55   | stay-house-wait   | STAY IN          | Gnr>Pnr (G >= 60)  |
| SHOW_HOUSES_BUY    | 60   | VIEW_BUY_HOUSE    | 60   | buy-house         | BUY HOUSE        | Gnr>Pnr (G >= 70)  |
| ALLOW_BUYING       | 70   | BUY_HOUSE_WAIT    | 70   | buy-house-wait    | MOVE IN          | Gnr>=Pnr & APPROVE |  
| BUYING_FINISHED    | 80   | BOUGHT_HOUSE      | 80   | bought-house      | VIEW TAXES       | Gnr>Pnr (G >= 90)  |
| BUYING_FINISHED    | 80   | STAYED_HOUSE      | 85   | stayed-house      | VIEW TAXES       | Gnr>Pnr (G >= 90)  |
| ALLOW_IMPROVEMENTS | 90   | VIEW_IMPROVEMENTS | 90   | view-improvem.    | BUY IMPROVEM.    | Gnr>Pnr (G >= 100) |
| SHOW_SURVEY        | 100  | ANSWER_SURVEY     | 100  | answer-survey     | WAIT FOR DICE    | always             |
| SURVEY_COMPLETED   | 110  | SURVEY_COMPLETED  | 110  | survey-completed  | VIEW DAMAGE      | Gnr>Pnr (G >= 120) |
| ROLLED_DICE        | 120  | VIEW_DAMAGE       | 120  | view-damage       | VIEW SUMMARY     | Gnr>Pnr (G >= 130) |
| SHOW_SUMMARY       | 130  | VIEW_SUMMARY      | 130  | view-summary      | NEXT ROUND       | G-round > P-round  |

