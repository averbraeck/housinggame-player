package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionRecord;
import nl.tudelft.simulation.housinggame.player.house.HouseAccordion;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;
import nl.tudelft.simulation.housinggame.player.viewimprovements.ImprovementsAccordion;
import nl.tudelft.simulation.housinggame.player.viewtaxes.TaxAccordion;

@WebServlet("/answer-survey")
public class AnswerSurveyServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        data.getContentHtml().clear();
        BudgetAccordion.makeBudgetAccordion(data);
        NewsAccordion.makeNewsAccordion(data);
        HouseAccordion.makeHouseConfirmationAccordion(data);
        TaxAccordion.makeTaxAccordion(data);
        ImprovementsAccordion.makeBoughtImprovementsAccordion(data);
        makeSurveyAccordion(data);

        response.sendRedirect("jsp/player/answer-survey.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    public static void makeSurveyAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<QuestionRecord> questionList = dslContext.selectFrom(Tables.QUESTION)
                .where(Tables.QUESTION.SCENARIO_ID.eq(data.getScenario().getId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p>Please answer the following questions:</p>\n");
        for (QuestionRecord question : questionList)
        {
            s.append("Question " + question.getQuestionNumber() + ".<br/>" + question.getDescription() + "<br/>\n");
            s.append("<br/>\n");
        }
        s.append("<br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("house/survey", s.toString());
    }

}
