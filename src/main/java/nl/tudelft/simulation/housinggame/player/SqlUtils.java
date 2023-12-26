package nl.tudelft.simulation.housinggame.player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.RoundState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.UserRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;

public final class SqlUtils
{

    private SqlUtils()
    {
        // utility class
    }

    public static Connection dbConnection() throws SQLException, ClassNotFoundException
    {
        String jdbcURL = "jdbc:mysql://localhost:3306/housinggame";
        String dbUser = "housinggame";
        String dbPassword = "tudHouse#4";

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
    }

    public static UserRecord readUserFromUserId(final PlayerData data, final int userId)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.USER).where(Tables.USER.ID.eq(userId)).fetchAny();
    }

    public static UserRecord readUserFromUsername(final PlayerData data, final String username)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.USER).where(Tables.USER.USERNAME.eq(username)).fetchAny();
    }

    @SuppressWarnings("unchecked")
    public static <R extends org.jooq.UpdatableRecord<R>> R readRecordFromId(final PlayerData data, final Table<R> table,
            final int recordId)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(table).where(((TableField<R, Integer>) table.field("id")).eq(recordId)).fetchOne();
    }

    /**
     * Return or create the PlayerRound for the given GroupRound. When there is an earlier PlayerRound, copy the latest. When
     * this is the first PlayerRound record created for this player, initialize a blank one. make sure the playerState is
     * "INIT".
     * @param data PlayerData; session data
     * @param group GrouproundRecord; the GroupRound for which we want to get/create the PlayerRound
     * @return PlayerroundRecord; the PlayerRound or a newly created PlayerRound for the GroupRound
     */
    public static PlayerroundRecord makePlayerRound(final PlayerData data, final GrouproundRecord groupRound)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        // is there a playerRound belonging to the current groupRound?
        List<PlayerroundRecord> prList = dslContext.selectFrom(Tables.PLAYERROUND)
                .where(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetch();

        // make round 0 data
        if (prList.isEmpty())
        {
            GrouproundRecord groupRound0 = data.getGroupRoundList().get(0);
            PlayerroundRecord newPr = makePlayerRound0(data, groupRound0);
            prList.add(newPr);
        }

        // make data up to current round of the game (fill missing rounds if necessary)
        for (int roundNumber = 1; roundNumber <= groupRound.getRoundNumber(); roundNumber++)
        {
            if (roundNumber >= prList.size())
            {
                PlayerroundRecord newPr = copyPlayerRound(data, prList.get(roundNumber - 1), roundNumber);
                prList.add(newPr);
            }
        }

        PlayerroundRecord playerRound =
                dslContext.selectFrom(Tables.PLAYERROUND).where(Tables.PLAYERROUND.GROUPROUND_ID.eq(groupRound.getId()))
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetchOne();
        return playerRound;
    }

    public static PlayerroundRecord makePlayerRound0(final PlayerData data, final GrouproundRecord groupRound0)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        PlayerroundRecord newPr = dslContext.newRecord(Tables.PLAYERROUND);
        WelfaretypeRecord welfareType = dslContext.selectFrom(Tables.WELFARETYPE)
                .where(Tables.WELFARETYPE.ID.eq(data.getPlayer().getWelfaretypeId())).fetchAny();
        newPr.setPlayerId(data.getPlayer().getId());

        // finances
        newPr.setRoundIncome(welfareType.getRoundIncome());
        newPr.setLivingCosts(welfareType.getLivingCosts());
        newPr.setPaidDebt(0);
        newPr.setMortgagePayment(0);
        newPr.setProfitSoldHouse(0);
        newPr.setSpentSavingsForBuyingHouse(0);
        newPr.setCostTaxes(0);
        newPr.setCostMeasuresBought(0);
        newPr.setSatisfactionBought(0);
        newPr.setCostFluvialDamage(0);
        newPr.setCostPluvialDamage(0);
        newPr.setSpendableIncome(welfareType.getInitialMoney());

        // satisfaction
        newPr.setPersonalSatisfaction(welfareType.getInitialSatisfaction());
        newPr.setSatisfactionMovePenalty(0);
        newPr.setSatisfactionHouseRatingDelta(0);
        newPr.setSatisfactionHouseMeasures(0);
        newPr.setSatisfactionBought(0);
        newPr.setSatisfactionFluvialPenalty(0);
        newPr.setSatisfactionPluvialPenalty(0);
        newPr.setSatisfactionDebtPenalty(0);

        // house
        newPr.setStartHousegroupId(null);
        newPr.setMortgageHouseStart(0);
        newPr.setMaximumMortgage(welfareType.getMaximumMortgage());
        newPr.setPreferredHouseRating(welfareType.getPreferredHouseRating());
        newPr.setMortgageLeftStart(0);
        newPr.setHousePriceSold(0);
        newPr.setHousePriceBought(0);
        newPr.setFinalHousegroupId(null);
        newPr.setMovingreasonId(null);
        newPr.setMovingReasonOther("");
        newPr.setMortgageHouseEnd(0);
        newPr.setMortgageLeftEnd(0);
        newPr.setActiveTransactionId(null);

        // flood
        newPr.setFluvialDamage(0);
        newPr.setPluvialDamage(0);

        // general
        newPr.setGrouproundId(groupRound0.getId());
        newPr.setPlayerState(PlayerState.LOGIN.toString());

        newPr.store();
        return newPr;
    }

    public static PlayerroundRecord copyPlayerRound(final PlayerData data, final PlayerroundRecord oldPr, final int roundNumber)
    {
        GrouproundRecord groupRound = data.getGroupRoundList().get(roundNumber);
        PlayerroundRecord newPr = oldPr.copy();

        // finance
        newPr.setPaidDebt(0);
        newPr.setMortgagePayment(0);
        newPr.setProfitSoldHouse(0);
        newPr.setSpentSavingsForBuyingHouse(0);
        newPr.setCostTaxes(0);
        newPr.setCostMeasuresBought(0);
        newPr.setSatisfactionBought(0);
        newPr.setCostFluvialDamage(0);
        newPr.setCostPluvialDamage(0);
        newPr.setSpendableIncome(oldPr.getSpendableIncome() + oldPr.getRoundIncome() - oldPr.getLivingCosts());
        if (oldPr.getSpendableIncome() < 0 && newPr.getSpendableIncome() > oldPr.getSpendableIncome())
            newPr.setPaidDebt(newPr.getSpendableIncome() - oldPr.getSpendableIncome());

        // satisfaction
        newPr.setPersonalSatisfaction(oldPr.getPersonalSatisfaction());
        newPr.setSatisfactionMovePenalty(0);
        newPr.setSatisfactionHouseRatingDelta(0);
        newPr.setSatisfactionHouseMeasures(0);
        newPr.setSatisfactionBought(0);
        newPr.setSatisfactionFluvialPenalty(0);
        newPr.setSatisfactionPluvialPenalty(0);
        newPr.setSatisfactionDebtPenalty(0);

        // house
        newPr.setStartHousegroupId(oldPr.getFinalHousegroupId());
        newPr.setMortgageHouseStart(oldPr.getMortgageHouseEnd());
        newPr.setMortgageLeftStart(oldPr.getMortgageLeftEnd());
        newPr.setHousePriceSold(0);
        newPr.setHousePriceBought(0);
        newPr.setFinalHousegroupId(oldPr.getFinalHousegroupId());
        newPr.setMovingreasonId(null);
        newPr.setMovingReasonOther("");
        newPr.setMortgageHouseEnd(oldPr.getMortgageHouseEnd());
        newPr.setMortgageLeftEnd(oldPr.getMortgageLeftEnd());
        newPr.setActiveTransactionId(null);

        // flood
        newPr.setFluvialDamage(0);
        newPr.setPluvialDamage(0);

        // general
        newPr.setGrouproundId(groupRound.getId());
        newPr.setPlayerState(PlayerState.READ_BUDGET.toString());
        newPr.store();

        return newPr;
    }

    public static GrouproundRecord makeGroupRound0(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        GrouproundRecord groupRound = dslContext.newRecord(Tables.GROUPROUND);
        groupRound.setGroupId(data.getGroup().getId());
        groupRound.setRoundNumber(0);
        groupRound.setFluvialFloodIntensity(0);
        groupRound.setPluvialFloodIntensity(0);
        groupRound.setRoundState(RoundState.LOGIN.toString());
        groupRound.store();
        return groupRound;
    }

}
