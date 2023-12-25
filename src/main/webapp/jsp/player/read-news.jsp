<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game News</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>
    
    <div class="hg-title">Round ${playerData.getPlayerRoundNumber() } news</div>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp">
         <jsp:param name="open" value="in" />
       </jsp:include>
    </div>

   <form action="/housinggame-player/read-news-done" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value='${playerData.getContentHtml("buy-or-sell") }' />
        <input type="submit" value="VIEW HOUSES" class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'read-news'},
        function(data, status) {
          if (data == "OK") {
            $("#hg-submit").removeAttr("disabled");
          } else {
            setTimeout(check, 5000);
          }
        });
    }
  </script>

</body>
</html>
