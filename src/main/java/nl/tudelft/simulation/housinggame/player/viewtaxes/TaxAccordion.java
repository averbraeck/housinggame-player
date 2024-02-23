package nl.tudelft.simulation.housinggame.player.viewtaxes;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.CommunityRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.TaxRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

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
        CommunityRecord community = SqlUtils.readRecordFromId(data, Tables.COMMUNITY, data.getHouse().getCommunityId());
        s.append("This house is in community " + community.getName() + "<br/><br/>\n");
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
            HouseRecord house = SqlUtils.readRecordFromId(data, Tables.HOUSE, houseGroup.getHouseId());
            if (house.getCommunityId().equals(community.getId()) && houseGroup.getStatus().equals(HouseGroupStatus.OCCUPIED))
                nrCommunity++;
        }

        // TODO: tax increases based on measures

        s.append("<br/>Your community has " + nrCommunity + " inhabitants<br/>\n");
        s.append("Your paid taxes are: " + data.k(data.getPlayerRound().getCostTaxes()) + "<br/>\n");
        s.append("<span style=\"color:grey;\">" + "(note that the taxes can be based on an earlier count "
                + "of the number of inhabitants in the community)</span><br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("panel/tax", s.toString());
    }

}
