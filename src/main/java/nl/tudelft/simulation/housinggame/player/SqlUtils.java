package nl.tudelft.simulation.housinggame.player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
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
     * Return the latest GroupRound for the given group, or create a GroupRound for round 0.
     * @param data PlayerData; session data
     * @param group GroupRecord; the group for which we want to know the latest GroupRound
     * @return GrouproundRecord; the latest GroupRound or a newly created GroupRound for round 0
     */
    public static GrouproundRecord getOrMakeLatestGroupRound(final PlayerData data, final GroupRecord group)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        // is there a current (highest) groupRound? Execute this code with a database lock around it (!)
        List<GrouproundRecord> grList = new ArrayList<>();
        try
        {
            dslContext.execute("LOCK TABLES groupround WRITE, round WRITE WAIT 10;");
            grList = dslContext.selectFrom(Tables.GROUPROUND).where(Tables.GROUPROUND.GROUP_ID.eq(group.getId())).fetch();
            if (grList.isEmpty())
            {
                GrouproundRecord newGr = dslContext.newRecord(Tables.GROUPROUND);
                newGr.setGroupId(group.getId());
                // find the round with the lowest number: SELECT * from round WHERE round.scenario_id=3
                // AND round_number=(SELECT MIN(round_number) FROM round WHERE round.scenario_id=3 );
                int minRoundNumber = dslContext
                        .execute("SELECT MIN(round_number) FROM round WHERE round.scenario_id=" + group.getScenarioId());
                RoundRecord lowestRound =
                        dslContext.selectFrom(Tables.ROUND).where(Tables.ROUND.SCENARIO_ID.eq(group.getScenarioId()))
                                .and(Tables.ROUND.ROUND_NUMBER.eq(minRoundNumber)).fetchOne();
                newGr.setRoundId(lowestRound.getId());
                newGr.setFluvialFloodIntensity(0);
                newGr.setPluvialFloodIntensity(0);
                // newGr.setStartTime(LocalDateTime.now());
                newGr.store();
                grList.add(newGr);
            }
        }
        finally
        {
            dslContext.execute("UNLOCK TABLES;");
        }

        // find the GrouproundRecord with the highest associated round number
        GrouproundRecord groupRound = null;
        int currentRound = -1;
        for (int i = 0; i < grList.size(); i++)
        {
            UInteger roundId = grList.get(i).getRoundId();
            RoundRecord roundRecord = SqlUtils.readRecordFromId(data, Tables.ROUND, roundId);
            if (roundRecord.getRoundNumber().intValue() > currentRound)
            {
                currentRound = roundRecord.getRoundNumber().intValue();
                groupRound = grList.get(i);
            }
        }
        return groupRound;
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
            newPr.setCurrentWealth(welfareType.getInitialMoney().intValue());
            newPr.setDebt(UInteger.valueOf(0));
            newPr.setFluvialDamage(UInteger.valueOf(0));
            newPr.setPluvialDamage(UInteger.valueOf(0));
            newPr.setGrouproundId(groupRound.getId());
            newPr.setHouseId(null);
            newPr.setHousePriceBought(UInteger.valueOf(0));
            newPr.setHousePriceSold(UInteger.valueOf(0));
            newPr.setIncome(welfareType.getIncome());
            newPr.setLivingCosts(welfareType.getLivingCosts());
            newPr.setMeasureBought(UInteger.valueOf(0));
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
