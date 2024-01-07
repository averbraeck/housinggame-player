package nl.tudelft.simulation.housinggame.player.readnews;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.NewsitemRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;

/**
 * NewsAccordion puts the html-code for the news item(s) in panel/news.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NewsAccordion
{

    public static void makeNewsAccordion(final PlayerData data)
    {
        // get the news record(s) for the current round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<NewsitemRecord> newsList = dslContext.selectFrom(Tables.NEWSITEM).where(Tables.NEWSITEM.ROUND_NUMBER
                .eq(data.getPlayerRoundNumber()).and(Tables.NEWSITEM.SCENARIO_ID.eq(data.getScenario().getId()))).fetch();
        StringBuilder s = new StringBuilder();
        for (NewsitemRecord news : newsList)
        {
            s.append("            <div class=\"hg-header1\">" + news.getName() + "</div>\n");
            s.append("            <p>\n");
            s.append(news.getSummary() + "\n");
            s.append("            </p>\n");
        }
        data.getContentHtml().put("panel/news", s.toString());
    }

}
