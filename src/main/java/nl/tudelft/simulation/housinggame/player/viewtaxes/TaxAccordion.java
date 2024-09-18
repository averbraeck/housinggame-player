package nl.tudelft.simulation.housinggame.player.viewtaxes;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.CumulativeNewsEffects;
import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.CommunityRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.TaxRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

/**
 * TaxAccordion.java.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TaxAccordion
{

    public static void makeTaxAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("You live in house " + data.getHouse().getCode() + "<br/>\n");
        CommunityRecord community = PlayerUtils.readRecordFromId(data, Tables.COMMUNITY, data.getHouse().getCommunityId());
        s.append("This house is in the community " + community.getName() + "<br/><br/>\n");
        s.append("The taxes for this community are as follows:<br/>\n");
        List<TaxRecord> taxList = dslContext.selectFrom(Tables.TAX).where(Tables.TAX.COMMUNITY_ID.eq(community.getId())).fetch()
                .sortAsc(Tables.TAX.MINIMUM_INHABITANTS);
        for (TaxRecord tax : taxList)
        {
            s.append(tax.getMinimumInhabitants() + " - " + tax.getMaximumInhabitants() + " inhabitants: "
                    + data.k(tax.getTaxCost().intValue()) + "/round<br/>\n");
        }
        List<HousegroupRecord> houseGroupList =
                dslContext.selectFrom(Tables.HOUSEGROUP).where(Tables.HOUSEGROUP.GROUP_ID.eq(data.getGroup().getId())).fetch();
        int nrCommunity = 0;
        for (HousegroupRecord houseGroup : houseGroupList)
        {
            HouseRecord hgHouse = PlayerUtils.readRecordFromId(data, Tables.HOUSE, houseGroup.getHouseId());
            if (hgHouse.getCommunityId().equals(community.getId()) && houseGroup.getStatus().equals(HouseGroupStatus.OCCUPIED))
                nrCommunity++;
        }

        // see if there are tax changes
        var houseGroup = data.getHouseGroup();
        HouseRecord house = PlayerUtils.readRecordFromId(data, Tables.HOUSE, houseGroup.getHouseId());
        var cumulativeNewsEffects = CumulativeNewsEffects.readCumulativeNewsEffects(data.getDataSource(), data.getScenario(),
                data.getPlayerRoundNumber());
        var txc = (int) cumulativeNewsEffects.get(house.getCommunityId()).getTaxChange();
        if (txc != 0)
        {
            s.append("<div class=\"hg-header1\">Tax change for your community</div>\n");
            if (txc > 0)
                s.append("Your community has a tax increase of +" + txc + "\n");
            else
                s.append("Your community has a tax decrease of " + txc + "\n");
        }

        s.append("<div class=\"hg-header1\">Tax payment</div>\n");
        s.append("Your community has " + nrCommunity + " inhabitants<sup>(*)</sup><br/>\n");
        s.append("Your paid taxes are: " + data.k(data.getPlayerRound().getCostTaxes()) + "<br/><br/>\n");
        s.append("<span style=\"color:grey;\">" + "(*) The number of inhabitants in the community "
                + "may be different if another player joined late.</span><br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("panel/tax", s.toString());
    }

}
