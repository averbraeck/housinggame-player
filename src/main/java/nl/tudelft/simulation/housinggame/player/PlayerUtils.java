package nl.tudelft.simulation.housinggame.player;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.SqlUtils;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;

public final class PlayerUtils extends SqlUtils
{

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
        newPr.setCostHouseMeasuresBought(0);
        newPr.setCostPersonalMeasuresBought(0);
        newPr.setCostFluvialDamage(0);
        newPr.setCostPluvialDamage(0);
        newPr.setSpendableIncome(welfareType.getInitialMoney());

        // satisfaction
        newPr.setSatisfactionTotal(welfareType.getInitialSatisfaction());
        newPr.setSatisfactionMovePenalty(0);
        newPr.setSatisfactionHouseRatingDelta(0);
        newPr.setSatisfactionHouseMeasures(0);
        newPr.setSatisfactionPersonalMeasures(0);
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
        newPr.setPluvialBaseProtection(0);
        newPr.setPluvialBaseProtection(0);
        newPr.setPluvialCommunityDelta(0);
        newPr.setFluvialCommunityDelta(0);
        newPr.setPluvialHouseDelta(0);
        newPr.setFluvialHouseDelta(0);

        // general
        newPr.setGrouproundId(groupRound0.getId());
        data.newPlayerState(newPr, PlayerState.LOGIN, "Round=0"); // including store

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
        newPr.setCostHouseMeasuresBought(0);
        newPr.setCostPersonalMeasuresBought(0);
        newPr.setCostFluvialDamage(0);
        newPr.setCostPluvialDamage(0);
        newPr.setSpendableIncome(oldPr.getSpendableIncome() + oldPr.getRoundIncome() - oldPr.getLivingCosts());
        if (oldPr.getSpendableIncome() < 0 && newPr.getSpendableIncome() > oldPr.getSpendableIncome())
            newPr.setPaidDebt(Math.max(0, Math.min(0, newPr.getSpendableIncome()) - oldPr.getSpendableIncome()));

        // satisfaction
        newPr.setSatisfactionTotal(oldPr.getSatisfactionTotal());
        newPr.setSatisfactionMovePenalty(0);
        newPr.setSatisfactionHouseRatingDelta(0);
        newPr.setSatisfactionHouseMeasures(0);
        newPr.setSatisfactionPersonalMeasures(0);
        newPr.setSatisfactionFluvialPenalty(0);
        newPr.setSatisfactionPluvialPenalty(0);
        if (oldPr.getSpendableIncome() < 0)
        {
            newPr.setSatisfactionDebtPenalty(data.getScenarioParameters().getSatisfactionDebtPenalty());
            newPr.setSatisfactionTotal(
                    newPr.getSatisfactionTotal() - data.getScenarioParameters().getSatisfactionDebtPenalty());
        }
        else
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

        // flood (copy for now; will be adapted in damage calculation)
        newPr.setPluvialBaseProtection(oldPr.getPluvialBaseProtection());
        newPr.setPluvialBaseProtection(oldPr.getFluvialBaseProtection());
        newPr.setPluvialCommunityDelta(oldPr.getPluvialCommunityDelta());
        newPr.setFluvialCommunityDelta(oldPr.getFluvialCommunityDelta());
        newPr.setPluvialHouseDelta(oldPr.getPluvialHouseDelta());
        newPr.setFluvialHouseDelta(oldPr.getFluvialHouseDelta());

        // normalize the satisfaction based on scenario parameters
        normalizeSatisfaction(data, newPr);

        // general
        newPr.setGrouproundId(groupRound.getId());
        newPr.store(); // otherwise no fk-relation can be made with PlayerRound
        data.newPlayerState(newPr, PlayerState.READ_BUDGET, ""); // including store

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
        groupRound.setGroupState(GroupState.LOGIN.toString());
        groupRound.store();
        return groupRound;
    }

    public static void normalizeSatisfaction(final PlayerData data, final PlayerroundRecord playerRound)
    {
        var params = data.getScenarioParameters();
        int hgSatisfaction = 0;
        if (playerRound.getFinalHousegroupId() != null)
        {
            var houseGroup = readRecordFromId(data, Tables.HOUSEGROUP, playerRound.getFinalHousegroupId());
            hgSatisfaction = houseGroup.getHouseSatisfaction();
        }
        // normalize the satisfaction scores if so dictated by the parameters
        if (params.getAllowPersonalSatisfactionNeg() == 0)
            playerRound.setSatisfactionTotal(Math.max(0, playerRound.getSatisfactionTotal()));
        if (params.getAllowTotalSatisfactionNeg() == 0)
            playerRound.setSatisfactionTotal(Math.max(-hgSatisfaction, playerRound.getSatisfactionTotal()));
    }

}
