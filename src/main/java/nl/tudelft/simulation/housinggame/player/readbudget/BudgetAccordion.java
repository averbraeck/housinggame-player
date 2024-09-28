package nl.tudelft.simulation.housinggame.player.readbudget;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.player.PlayerData;

/**
 * BudgetAccordion puts the html-code for the budget in panel/budget, and progressively increases the amount of information
 * during the round.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class BudgetAccordion
{

    public static void makeBudgetAccordion(final PlayerData data)
    {
        int startSavings = Math.max(data.getPrevPlayerRound().getSpendableIncome(), 0);
        int startDebt = -Math.min(data.getPrevPlayerRound().getSpendableIncome(), 0);
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div class=\"hg-header1\">Your mortgage</div>\n");
        s.append("            <div class=\"hg-box-grey\">\n");
        s.append("              Maximum mortgage: " + data.k(data.getPlayerRound().getMaximumMortgage()) + " <br/>\n");
        if (data.getPlayerRound().getFinalHousegroupId() != null)
        {
            s.append("              House mortgage: " + data.k(data.getPlayerRound().getMortgageHouseEnd()) + " <br/>\n");
            s.append("              Left mortgage: " + data.k(data.getPlayerRound().getMortgageLeftEnd()) + " <br/>\n");
        }
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">House expectations</div>\n");
        s.append("            <div class=\"hg-box-grey\">\n");
        s.append("              Preferred house rating: " + data.getPlayerRound().getPreferredHouseRating() + " <br/>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Spendable income</div>\n");
        s.append("            <div class=\"hg-box-grey\" " +
                                  "style=\"display: flex; flex-direction: row; justify-content: flex-start; column-gap: 20px;\">\n");
        s.append("              <div>\n");
        s.append("                  Start savings / debt <br/>\n");
        s.append("                  Round income <br/>\n");
        s.append("                  Round living costs <br />\n");
        if (data.getPlayerRound().getFinalHousegroupId() != null && !data.getPlayerRound().getFinalHousegroupId().equals(
                data.getPlayerRound().getStartHousegroupId()))
        {
            s.append("                  Profit sold house <br />\n");
            s.append("                  Spent savings to buy house <br />\n");
        }
        if (data.getPlayerRound().getFinalHousegroupId() != null)
        {
            if (data.geState(PlayerState.BOUGHT_HOUSE))
            {
                s.append("                  Actual mortgage payment <br />\n");
            }
            if (data.geState(PlayerState.VIEW_TAXES))
            {
                s.append("                  Actual taxes <br />\n");
            }
            if (data.geState(PlayerState.VIEW_IMPROVEMENTS))
            {
                s.append("                  Personal improvements <br />\n");
                s.append("                  House improvements <br />\n");
            }
            if (data.geState(PlayerState.VIEW_DAMAGE))
            {
                s.append("                  House damage <br />\n");
            }
        }
        s.append("                  <br />\n");
        s.append("              </div>\n");

        s.append("              <div>\n");
        if (startSavings > 0)
            s.append("                + " + data.k(startSavings) + " <br/>\n");
        else if (startDebt > 0)
            s.append("                - " + data.k(startDebt) + " <br/>\n");
        else
            s.append("                +0 <br/>\n");
        s.append("                + " + data.k(data.getPlayerRound().getRoundIncome()) + " <br/>\n");
        s.append("                - " + data.k(data.getPlayerRound().getLivingCosts()) + " <br />\n");
        if (data.getPlayerRound().getFinalHousegroupId() != null && !data.getPlayerRound().getFinalHousegroupId().equals(
                data.getPlayerRound().getStartHousegroupId()))
        {
            s.append("                + " + data.k(data.getPlayerRound().getProfitSoldHouse()) + " <br />\n");
            s.append("                - " + data.k(data.getPlayerRound().getSpentSavingsForBuyingHouse()) + " <br />\n");
        }
        if (data.getPlayerRound().getFinalHousegroupId() != null)
        {
            if (data.geState(PlayerState.BOUGHT_HOUSE))
            {
                s.append("                - " + data.k(data.getPlayerRound().getMortgagePayment()) + " <br />\n");
            }
            if (data.geState(PlayerState.VIEW_TAXES))
            {
                s.append("                - " + data.k(data.getPlayerRound().getCostTaxes()) + " <br />\n");
            }
            if (data.geState(PlayerState.VIEW_IMPROVEMENTS))
            {
                s.append("                - " + data.k(data.getPlayerRound().getCostPersonalMeasuresBought()) + " <br />\n");
                s.append("                - " + data.k(data.getPlayerRound().getCostHouseMeasuresBought()) + " <br />\n");
            }
            if (data.geState(PlayerState.VIEW_DAMAGE))
            {
                s.append("                - " + data.k(data.getPlayerRound().getCostPluvialDamage()
                                              + data.getPlayerRound().getCostFluvialDamage()) + " <br />\n");
            }
        }
        s.append("                = " + data.k(data.getPlayerRound().getSpendableIncome()) + " \n");
        s.append("              </div>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Satisfaction points</div>\n");
        s.append("            <div class=\"hg-box-grey\" " +
                                "style=\"display: flex; flex-direction: row; justify-content: flex-start; column-gap: 20px;\">\n");
        s.append("              <div>\n");
        s.append("                  Start satisfaction<br/>\n");
        s.append("                  House measures satisfaction<br/>\n");
        s.append("                  House rating penalty<br/>\n");
        s.append("                  Flood damage penalty<br/>\n");
        s.append("                  Moving penalty<br/>\n");
        s.append("                  Debt penalty<br/>\n");
        s.append("                  Personal satisfaction<br/>\n");
        s.append("                  TOTAL satisfaction<br/>\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        int damage = data.getPlayerRound().getSatisfactionFluvialPenalty() + data.getPlayerRound().getSatisfactionPluvialPenalty();
        s.append("                + " + data.getPrevPlayerRound().getSatisfactionTotal() + " <br/>\n");
        s.append("                + " + data.getPlayerRound().getSatisfactionHouseMeasures() + " <br/>\n");
        if (data.getPlayerRound().getSatisfactionHouseRatingDelta() < 0)
            s.append("                - " + Math.abs(data.getPlayerRound().getSatisfactionHouseRatingDelta()) + " <br/>\n");
        else
            s.append("                + " + data.getPlayerRound().getSatisfactionHouseRatingDelta() + " <br/>\n");
        s.append("                - " + damage + " <br/>\n");
        s.append("                - " + Math.abs(data.getPlayerRound().getSatisfactionMovePenalty()) + " <br/>\n");
        s.append("                - " + Math.abs(data.getPlayerRound().getSatisfactionDebtPenalty()) + " <br/>\n");
        s.append("                + " + Math.abs(data.getPlayerRound().getSatisfactionPersonalMeasures()) + " <br/>\n");
        s.append("                = " + data.getPlayerRound().getSatisfactionTotal() + " \n");
        s.append("              </div>\n");
        s.append("            </div>\n");
        // @formatter:on
        data.getContentHtml().put("panel/budget", s.toString());
    }

}
