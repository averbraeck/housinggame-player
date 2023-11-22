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
import org.jooq.types.UInteger;

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

    public static RoundRecord readRoundFromRoundId(final PlayerData data, final Integer roundId)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.ROUND).where(Tables.ROUND.ID.eq(UInteger.valueOf(roundId))).fetchAny();
    }

    public static UserRecord readUserFromUserId(final PlayerData data, final Integer userId)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.USER).where(Tables.USER.ID.eq(UInteger.valueOf(userId))).fetchAny();
    }

    public static UserRecord readUserFromUsername(final PlayerData data, final String username)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.USER).where(Tables.USER.USERNAME.eq(username)).fetchAny();
    }

    public static <R extends org.jooq.UpdatableRecord<R>> R readRecordFromId(final PlayerData data, final Table<R> table,
            final int recordId)
    {
        return readRecordFromId(data, table, UInteger.valueOf(recordId));
    }

    @SuppressWarnings("unchecked")
    public static <R extends org.jooq.UpdatableRecord<R>> R readRecordFromId(final PlayerData data, final Table<R> table,
            final UInteger recordId)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(table).where(((TableField<R, UInteger>) table.field("id")).eq(recordId)).fetchOne();
    }

    /**
     * Return or create the PlayerRound for the given GroupRound. When there is an earlier PlayerRound, copy the latest. When
     * this is the first PlayerRound record created for this player, initialize a blank one. make sure the playerState is
     * "INIT".
     * @param data PlayerData; session data
     * @param group GrouproundRecord; the GroupRound for which we want to get/create the PlayerRound
     * @return PlayerroundRecord; the PlayerRound or a newly created PlayerRound for the GroupRound
     */
    public static PlayerroundRecord getOrMakePlayerRound(final PlayerData data, final GrouproundRecord groupRound)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        // is there a playerRound belonging to the current groupRound?
        List<PlayerroundRecord> prList = dslContext.selectFrom(Tables.PLAYERROUND)
                .where(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetch();
        if (prList.isEmpty())
        {
            PlayerroundRecord newPr = dslContext.newRecord(Tables.PLAYERROUND);
            WelfaretypeRecord welfareType = dslContext.selectFrom(Tables.WELFARETYPE)
                    .where(Tables.WELFARETYPE.ID.eq(data.getPlayer().getWelfaretypeId())).fetchAny();
            newPr.setPlayerId(data.getPlayer().getId());
            newPr.setSatisfaction(welfareType.getInitialSatisfaction());
            newPr.setSavings(welfareType.getInitialMoney());
            newPr.setSpendableIncome(welfareType.getIncomePerRound().intValue() + welfareType.getInitialMoney().intValue() -
                    welfareType.getLivingCosts().intValue());
            newPr.setDebt(UInteger.valueOf(0));
            newPr.setFluvialDamage(UInteger.valueOf(0));
            newPr.setPluvialDamage(UInteger.valueOf(0));
            newPr.setGrouproundId(groupRound.getId());
            newPr.setHouseId(null);
            newPr.setHousePriceBought(UInteger.valueOf(0));
            newPr.setHousePriceSold(UInteger.valueOf(0));
            newPr.setIncomePerRound(welfareType.getIncomePerRound());
            newPr.setLivingCosts(welfareType.getLivingCosts());
            newPr.setCostMeasureBought(UInteger.valueOf(0));
            newPr.setMortgage(UInteger.valueOf(0));
            newPr.setMovingReason("");
            newPr.setPaidOffDebt(UInteger.valueOf(0));
            newPr.setPreferredHouseRating(welfareType.getPreferredHouseRating());
            newPr.setRepairedDamage(null);
            newPr.setSatisfactionCostPerPoint(welfareType.getSatisfactionCostPerPoint());
            newPr.setSatisfactionPointBought(UInteger.valueOf(0));
            newPr.setSpentSavingsForBuyingHouse(UInteger.valueOf(0));
            newPr.setMaximumMortgage(welfareType.getMaximumMortgage());
            newPr.store();
            prList.add(newPr);
        }

        PlayerroundRecord playerRound =
                dslContext.selectFrom(Tables.PLAYERROUND).where(Tables.PLAYERROUND.GROUPROUND_ID.eq(groupRound.getId()))
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetchOne();
        return playerRound;
    }

}
