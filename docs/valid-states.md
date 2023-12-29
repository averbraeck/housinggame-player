# Valid player states to enable next screen button

! Group round state | Gnr  | Player state    | Pnr  | Screen      | Button      | When is button enabled  |
| ----------------- | ---: | --------------- | ---: | ----------- | ----------- | ----------------------- |
| ANY               | any  | ANY             | any  | login       | LOGIN       |                         |
| LOGIN             | 10   | LOGIN           | 10   | login-wait  | START GAME  | Enabled if Gnr > Pnr    |
| NEW_ROUND         | 20   | READ_BUDGET     | 20   | read-budget | READ NEWS   | Enabled if Gnr > Pnr    |
| ANNOUNCE_NEWS     | 30   | READ_NEWS       | 30   | read-news   | VIEW HOUSES | Enabled if Gnr > Pnr    |
| SHOW_HOUSES_SELL  | 40   | VIEW_SELL_HOUSE | 40   | sell-house  | STAY/SELL   | Gnr>Pnr, APPROVE/REJECT |