package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.xml.bind.DatatypeConverter;
import nl.tudelft.simulation.housinggame.data.tables.records.UserRecord;

@WebServlet("/login")
public class UserLoginServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException
    {
        super.init();
        System.getProperties().setProperty("org.jooq.no-logo", "true");

        // determine the connection pool, and create one if it does not yet exist (first use after server restart)
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("com.mysql.cj.protocol.a.NullValueEncoder");
            Class.forName("com.mysql.cj.protocol.a.SqlDateValueEncoder");
        }
        catch (ClassNotFoundException e)
        {
            throw new ServletException(e);
        }

        try
        {
            Context ctx = new InitialContext();
            try
            {
                ctx.lookup("/housinggame-player_datasource");
            }
            catch (NamingException ne)
            {
                final HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:mysql://localhost:3306/housinggame");
                config.setUsername("housinggame");
                config.setPassword("tudHouse#4");
                config.setMaximumPoolSize(2);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                DataSource dataSource = new HikariDataSource(config);
                ctx.bind("/housinggame-player_datasource", dataSource);
            }
        }
        catch (NamingException e)
        {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        MessageDigest md;
        String hashedPassword;
        try
        {
            // https://www.baeldung.com/java-md5
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            hashedPassword = DatatypeConverter.printHexBinary(digest).toLowerCase();
        }
        catch (NoSuchAlgorithmException e1)
        {
            throw new ServletException(e1);
        }
        HttpSession session = request.getSession();

        PlayerData data = new PlayerData();
        session.setAttribute("playerData", data);
        try
        {
            data.setDataSource((DataSource) new InitialContext().lookup("/housinggame-player_datasource"));
        }
        catch (NamingException e)
        {
            throw new ServletException(e);
        }

        UserRecord user = SqlUtils.readUserFromUsername(data, username);
        String userPassword = user == null ? "" : user.getPassword() == null ? "" : user.getPassword();
        // TODO: hashedPassword
        if (user != null && userPassword.equals(password) && user.getPlayeristrator().intValue() == 1)
        {
            data.setUsername(user.getUsername());
            data.setUserId(user.getId().intValue());
            data.setUser(user);
            response.sendRedirect("jsp/player/player.jsp");
        }
        else
        {
            session.removeAttribute("playerData");
            response.sendRedirect("jsp/player/login.jsp");
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        response.sendRedirect("jsp/player/login.jsp");
    }

}
