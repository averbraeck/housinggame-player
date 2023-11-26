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
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.RoundRecord;
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
        GrouproundRecord groupRound0 = data.getGroupRoundList().get(0);
        if (prList.isEmpty())
        {
            PlayerroundRecord newPr = dslContext.newRecord(Tables.PLAYERROUND);
            WelfaretypeRecord welfareType = dslContext.selectFrom(Tables.WELFARETYPE)
                    .where(Tables.WELFARETYPE.ID.eq(data.getPlayer().getWelfaretypeId())).fetchAny();
            newPr.setPlayerId(data.getPlayer().getId());
            newPr.setSatisfaction(welfareType.getInitialSatisfaction());
            newPr.setSavings(welfareType.getInitialMoney());
            newPr.setSpendableIncome(
                    welfareType.getIncomePerRound() + welfareType.getInitialMoney() - welfareType.getLivingCosts());
            newPr.setDebt(0);
            newPr.setFluvialDamage(0);
            newPr.setPluvialDamage(0);
            newPr.setGrouproundId(groupRound0.getId());
            newPr.setHouseId(null);
            newPr.setHousePriceBought(0);
            newPr.setHousePriceSold(0);
            newPr.setIncomePerRound(welfareType.getIncomePerRound());
            newPr.setLivingCosts(welfareType.getLivingCosts());
            newPr.setCostMeasureBought(0);
            newPr.setMortgage(0);
            newPr.setMovingReason("");
            newPr.setPaidOffDebt(0);
            newPr.setPreferredHouseRating(welfareType.getPreferredHouseRating());
            newPr.setRepairedDamage(null);
            newPr.setSatisfactionCostPerPoint(welfareType.getSatisfactionCostPerPoint());
            newPr.setSatisfactionPointBought(0);
            newPr.setSpentSavingsForBuyingHouse(0);
            newPr.setMaximumMortgage(welfareType.getMaximumMortgage());
            newPr.setPlayerState(PlayerState.LOGIN.toString());
            newPr.store();
            prList.add(newPr);
        }

        if (round.getRoundNumber() > 0)
        {
            PlayerroundRecord oldPlayerRound = null;
            for (PlayerroundRecord prr : prList)
            {
                GrouproundRecord gr = SqlUtils.readRecordFromId(data, Tables.GROUPROUND, prr.getGrouproundId());
                RoundRecord rr = SqlUtils.readRecordFromId(data, Tables.ROUND, gr.getRoundId());
                if (rr.getRoundNumber() == round.getRoundNumber() - 1)
                {
                    oldPlayerRound = prr;
                    break;
                }
            }
            PlayerroundRecord newPr = oldPlayerRound.copy();
            newPr.setGrouproundId(groupRound.getId());
            newPr.setSpendableIncome(newPr.getIncomePerRound() + newPr.getSavings() - newPr.getLivingCosts() - newPr.getDebt());
            newPr.setPlayerState(PlayerState.READ_BUDGET.toString());
            newPr.store();
        }

        PlayerroundRecord playerRound =
                dslContext.selectFrom(Tables.PLAYERROUND).where(Tables.PLAYERROUND.GROUPROUND_ID.eq(groupRound.getId()))
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetchOne();
        return playerRound;
    }

    public static GrouproundRecord makeGroupRound0(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        GrouproundRecord groupRound = dslContext.newRecord(Tables.GROUPROUND);
        groupRound.setGroupId(data.getGroup().getId());
        groupRound.setRoundId(data.getRoundList().get(0).getId());
        groupRound.setFluvialFloodIntensity(0);
        groupRound.setPluvialFloodIntensity(0);
        groupRound.setRoundState(RoundState.LOGIN.toString());
        groupRound.store();
        return groupRound;
    }
}
