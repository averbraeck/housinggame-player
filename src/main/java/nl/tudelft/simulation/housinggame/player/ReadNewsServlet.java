package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.NewsitemRecord;

@WebServlet("/read-news")
@MultipartConfig
public class ReadNewsServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = SessionUtils.getData(session);
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // get the news record(s)
        data.getContentHtml().clear();
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<NewsitemRecord> newsList =
                dslContext.selectFrom(Tables.NEWSITEM).where(Tables.NEWSITEM.ROUND_ID.eq(data.getRound().getId())).fetch();
        int nr = 1;
        for (NewsitemRecord news : newsList)
        {
            data.getContentHtml().put("news/name/" + nr, news.getName());
            data.getContentHtml().put("news/summary/" + nr, news.getSummary());
            data.getContentHtml().put("news/content/" + nr, news.getContent());
            nr++;
        }

        response.sendRedirect("jsp/player/news.jsp");
    }

}
